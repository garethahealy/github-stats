package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.WhoAreYou;
import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapGuessService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;

@ApplicationScoped
public class CreateWhoAreYouIssueService {

    @Inject
    Logger logger;

    @Inject
    @TemplatePath("CreateWhoAreYouIssue.ftl")
    Template issue;

    @Inject
    @TemplatePath("CreateWhoAreYouIssueRead.ftl")
    Template issueRead;

    private final GitHubService gitHubService;
    private final CsvService csvService;
    private final LdapGuessService ldapGuessService;

    @Inject
    public CreateWhoAreYouIssueService(GitHubService gitHubService, CsvService csvService, LdapGuessService ldapGuessService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapGuessService = ldapGuessService;
    }

    public void run(String organization, String issueRepo, boolean isDryRun, String membersCsv, String supplementaryCsv, GHPermissionType perms, boolean shouldGuess, boolean failNoVpn) throws IOException, ExecutionException, InterruptedException, TemplateException, LdapException {
        GitHub gitHub = gitHubService.getGitHub();
        GHOrganization org = gitHubService.getOrganization(gitHub, organization);
        GHRepository orgRepo = gitHubService.getRepository(org, issueRepo);

        List<WhoAreYou> usersToInform = collectUnknownUsers(gitHub, org, membersCsv, supplementaryCsv, perms, orgRepo);
        ldapGuessService.attemptToGuess(usersToInform.stream().map(WhoAreYou::ghUser).toList(), shouldGuess, failNoVpn, org);
        createIssue(usersToInform, orgRepo, perms, isDryRun);

        logger.info("Finished.");
    }

    private List<WhoAreYou> collectUnknownUsers(GitHub gitHub, GHOrganization org, String membersCsv, String supplementaryCsv, GHPermissionType perms, GHRepository orgRepo) throws IOException, ExecutionException, InterruptedException {
        List<WhoAreYou> usersToInform;

        Map<String, Members> ldapMembers = csvService.getKnownMembers(membersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        logger.infof("There are %s GitHub members", gitHubService.listMembers(org).size());
        logger.infof("There are %s known members and %s supplementary members in the CSVs, total %s", ldapMembers.size(), supplementaryMembers.size(), (ldapMembers.size() + supplementaryMembers.size()));

        if (GHPermissionType.READ == perms) {
            usersToInform = collectUnknownUsersWithRead(org, ldapMembers, supplementaryMembers);
        } else {
            usersToInform = collectUnknownUsersWithAdminOrWrite(gitHub, org, ldapMembers, supplementaryMembers, perms);
        }

        Collections.sort(usersToInform);

        logger.info("--> Members collected DONE");
        return usersToInform;
    }

    private List<WhoAreYou> collectUnknownUsersWithRead(GHOrganization org, Map<String, Members> ldapMembers, Map<String, Members> supplementaryMembers) throws IOException {
        List<WhoAreYou> usersToInform = new ArrayList<>();

        List<GHUser> members = gitHubService.listMembers(org);
        for (GHUser member : members) {
            if (ldapMembers.containsKey(member.getLogin()) || supplementaryMembers.containsKey(member.getLogin())) {
                logger.debugf("Ignoring: %s", member.getLogin());
            } else {
                usersToInform.add(new WhoAreYou(member.getName(), member.getLogin(), "https://github.com/redhat-cop", member));
            }
        }

        return usersToInform;
    }

    private List<WhoAreYou> collectUnknownUsersWithAdminOrWrite(GitHub gitHub, GHOrganization org, Map<String, Members> knownMembers, Map<String, Members> supplementaryMembers, GHPermissionType perms) throws IOException, ExecutionException, InterruptedException {
        Map<String, WhoAreYou> usersToInform = new ConcurrentHashMap<>();

        List<Future<Integer>> futures = new ArrayList<>();
        int cores = Runtime.getRuntime().availableProcessors() * 2;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (GHTeam team : gitHubService.listTeams(org)) {
                futures.add(executor.submit(() -> {
                    logger.infof("Working on team %s", team.getName());

                    int changed = 0;
                    for (GHUser member : team.getMembers()) {
                        if (knownMembers.containsKey(member.getLogin()) || supplementaryMembers.containsKey(member.getLogin()) || usersToInform.containsKey(member.getLogin())) {
                            logger.debugf("Ignoring: %s", member.getLogin());
                        } else {
                            for (GHRepository repository : team.listRepositories()) {
                                //Another thread added this user as they are in multiple teams, so break out
                                if (usersToInform.containsKey(member.getLogin())) {
                                    break;
                                }

                                boolean hasPermission = repository.hasPermission(member, perms);
                                if (hasPermission) {
                                    logger.warnf("Member %s has %s on %s - but we don't know who they are", member.getLogin(), perms, repository.getName());

                                    usersToInform.put(member.getLogin(), new WhoAreYou(member.getName(), member.getLogin(), repository.getHtmlUrl().toString(), member));
                                    changed++;

                                    break;
                                }
                            }
                        }
                    }

                    return changed;
                }));

                if (futures.size() == cores) {
                    for (Future<Integer> future : futures) {
                        future.get();
                    }

                    futures.clear();

                    gitHubService.logRateLimit(gitHub);
                }
            }

            //Incase there are any leftover calls to be made
            for (Future<Integer> future : futures) {
                future.get();
            }
        }

        return new ArrayList<>(usersToInform.values());
    }

    private void createIssue(List<WhoAreYou> usersToInform, GHRepository orgRepo, GHPermissionType permissions, boolean isDryRun) throws TemplateException, IOException {
        if (!usersToInform.isEmpty()) {
            Map<String, Object> root = new HashMap<>();
            root.put("users", usersToInform);
            root.put("permissions", permissions.toString());

            StringWriter stringWriter = new StringWriter();
            if (permissions == GHPermissionType.READ) {
                issueRead.process(root, stringWriter);
            } else {
                issue.process(root, stringWriter);
            }

            if (isDryRun) {
                logger.warnf("DRY-RUN: Would have created issue in %s", orgRepo.getName());
                logger.warnf(stringWriter.toString());
            } else {
                GHIssue createdIssue = orgRepo.createIssue("Request GitHub to Red Hat ID linkage for users with " + permissions)
                        .label("admin")
                        .body(stringWriter.toString())
                        .create();

                logger.infof("Created issue: %s", createdIssue.getUrl());
            }
        }

        logger.info("--> Issue creation DONE");
    }
}
