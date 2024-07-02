package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@ApplicationScoped
public class GitHubMemberInRedHatLdapService {

    @Inject
    Logger logger;

    @Inject
    @TemplatePath("GitHubMemberInRedHatLdap.ftl")
    Template issue;

    private final GitHubService gitHubService;
    private final LdapService ldapService;
    private final CsvService csvService;

    @Inject
    public GitHubMemberInRedHatLdapService(GitHubService gitHubService, CsvService csvService, LdapService ldapService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapService = ldapService;
    }

    public void run(String organization, String issueRepo, boolean isDryRun, String ldapMembersCsv, String supplementaryCsv, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        GHRepository orgRepo = gitHubService.getRepository(org, issueRepo);
        List<GHUser> members = gitHubService.listMembers(org);

        List<Members> ldapCheck = collectMembersToCheck(members, ldapMembersCsv, supplementaryCsv);
        List<Members> usersToRemove = searchViaLdapFor(ldapCheck, failNoVpn);

        createIssue(usersToRemove, orgRepo, isDryRun);

        logger.info("Finished.");
    }

    private List<Members> collectMembersToCheck(List<GHUser> members, String ldapMembersCsv, String supplementaryCsv) throws IOException {
        List<Members> answer = new ArrayList<>();

        Map<String, Members> ldapMembers = csvService.getKnownMembers(ldapMembersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        logger.infof("There are %s GitHub members", members.size());
        logger.infof("There are %s known members and %s supplementary members in the CSVs, total %s", ldapMembers.size(), supplementaryMembers.size(), (ldapMembers.size() + supplementaryMembers.size()));

        for (GHUser current : members) {
            if (ldapMembers.containsKey(current.getLogin())) {
                logger.debugf("Adding %s to LDAP check list from known members", current.getLogin());

                answer.add(ldapMembers.get(current.getLogin()));
            } else {
                if (supplementaryMembers.containsKey(current.getLogin())) {
                    logger.debugf("Adding %s to LDAP check list from supplementary", current.getLogin());

                    answer.add(supplementaryMembers.get(current.getLogin()));
                }
            }
        }

        Collections.sort(answer);

        logger.info("--> User Lookup DONE");
        return answer;
    }

    private List<Members> searchViaLdapFor(List<Members> ldapCheck, boolean failNoVpn) throws IOException, LdapException {
        List<Members> answer = new ArrayList<>();

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                for (Members current : ldapCheck) {
                    boolean found = ldapService.searchOnUser(connection, current.getRedHatUserId());
                    if (!found) {
                        logger.warnf("Did not find %s in LDAP", current.getRedHatUserId());
                        answer.add(current);
                    }
                }
            }
        } else {
            if (failNoVpn) {
                throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
            }
        }

        logger.info("--> LDAP Lookup DONE");
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
