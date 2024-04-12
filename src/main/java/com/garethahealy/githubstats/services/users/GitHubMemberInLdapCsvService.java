package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@ApplicationScoped
public class GitHubMemberInLdapCsvService {

    @Inject
    Logger logger;

    @Inject
    @TemplatePath("GitHubMemberInRedHatLdap.ftl")
    Template issue;

    private final GitHubService gitHubService;
    private final CsvService csvService;

    @Inject
    public GitHubMemberInLdapCsvService(GitHubService gitHubService, CsvService csvService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
    }

    public void run(String organization, String issueRepo, boolean isDryRun, String membersCsv, String supplementaryCsv) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        GHRepository orgRepo = gitHubService.getRepository(org, issueRepo);
        List<GHUser> members = gitHubService.listMembers(org);

        String configContent = gitHubService.getOrgConfigYaml(orgRepo);
        Set<String> membersInConfig = gitHubService.getOrgMembers(configContent);

        List<Members> ldapCheck = collectMembersToCheck(members, membersCsv, supplementaryCsv);
        List<Members> usersToRemove = searchViaLocalFor(ldapCheck, membersInConfig);

        createIssue(usersToRemove, orgRepo, isDryRun);

        logger.info("Finished.");
    }

    private List<Members> collectMembersToCheck(List<GHUser> members, String membersCsv, String supplementaryCsv) throws IOException {
        List<Members> answer = new ArrayList<>();

        Map<String, Members> knownMembers = csvService.getKnownMembers(membersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        logger.infof("There are %s GitHub members", members.size());
        logger.infof("There are %s known members and %s supplementary members in the CSVs", knownMembers.size(), supplementaryMembers.size());

        for (GHUser current : members) {
            if (knownMembers.containsKey(current.getLogin())) {
                logger.debugf("Adding %s to LDAP check list from known members", current.getLogin());

                answer.add(knownMembers.get(current.getLogin()));
            } else {
                if (supplementaryMembers.containsKey(current.getLogin())) {
                    logger.debugf("Adding %s to LDAP check list from supplementary", current.getLogin());

                    answer.add(supplementaryMembers.get(current.getLogin()));
                }
            }
        }

        logger.info("--> User Lookup DONE");
        return answer;
    }

    private List<Members> searchViaLocalFor(List<Members> ldapCheck, Set<String> membersInConfig) {
        List<Members> answer = new ArrayList<>();

        for (String current : membersInConfig) {
            boolean found = false;
            for (Members member : ldapCheck) {
                if (member.getWhatIsYourGitHubUsername().equalsIgnoreCase(current)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.warnf("Did not find %s in org/config.yaml", current);
                answer.add(new Members(null, "unknown", current));
            }
        }

        return answer;
    }

    private void createIssue(List<Members> usersToRemove, GHRepository orgRepo, boolean isDryRun) throws TemplateException, IOException {
        if (!usersToRemove.isEmpty()) {
            Map<String, Object> root = new HashMap<>();
            root.put("users", usersToRemove);

            StringWriter stringWriter = new StringWriter();
            issue.process(root, stringWriter);

            if (isDryRun) {
                logger.warnf("DRY-RUN: Would have created issue in %s", orgRepo.getName());
                logger.warnf(stringWriter.toString());
            } else {
                GHIssue createdIssue = orgRepo.createIssue("Remove users - Not in RH LDAP")
                        .label("admin")
                        .body(stringWriter.toString())
                        .create();

                logger.infof("Created issue: %s", createdIssue.getUrl());
            }
        }

        logger.info("--> Issue creation DONE");
    }
}
