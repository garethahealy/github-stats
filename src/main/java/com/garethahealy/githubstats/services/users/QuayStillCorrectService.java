package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
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
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@ApplicationScoped
public class QuayStillCorrectService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(QuayStillCorrectService.class);
    @Inject
    Logger logger;

    @Inject
    @TemplatePath("AnsibleUserNotInConfig.ftl")
    Template issue;

    private final GitHubService gitHubService;
    private final LdapService ldapService;

    @Inject
    public QuayStillCorrectService(GitHubService gitHubService, LdapService ldapService) {
        this.gitHubService = gitHubService;
        this.ldapService = ldapService;
    }

    public void run(String organization, String issueRepo, boolean isDryRun, boolean failNoVpn) throws IOException, TemplateException, LdapException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        GHRepository orgRepo = gitHubService.getRepository(org, issueRepo);

        logger.infof("Looking up %s", org.getName());

        String ansibleGroupYaml = gitHubService.getOrgAnsibleGroupVarsYaml(orgRepo);
        if (ansibleGroupYaml == null || ansibleGroupYaml.isEmpty()) {
            return;
        }

        Set<String> ansibleMembers = gitHubService.getAnsibleMembers(ansibleGroupYaml);

        List<String> unknownMembers = collectUnknownMembers(ansibleMembers, failNoVpn);
        createIssue(unknownMembers, orgRepo, isDryRun);

        logger.info("Finished.");
    }

    private List<String> collectUnknownMembers(Set<String> ansibleMembers, boolean failNoVpn) throws IOException, LdapException {
        List<String> answer = new ArrayList<>();

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                for (String user : ansibleMembers) {
                    String rhEmail = ldapService.searchOnQuaySocial(connection, user);
                    if (rhEmail.isEmpty()) {
                        answer.add(user);
                    }
                }
            }
        } else {
            if (failNoVpn) {
                throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
            }
        }

        Collections.sort(answer);

        logger.info("--> Collect unknown users DONE");
        return answer;
    }

    private void createIssue(List<String> usersToInform, GHRepository orgRepo, boolean isDryRun) throws TemplateException, IOException {
        if (!usersToInform.isEmpty()) {
            Map<String, Object> root = new HashMap<>();
            root.put("users", usersToInform);

            StringWriter stringWriter = new StringWriter();
            issue.process(root, stringWriter);

            if (isDryRun) {
                logger.warnf("DRY-RUN: Would have created issue in %s", orgRepo.getName());

                logger.warnf(stringWriter.toString());
            } else {
                GHIssue createdIssue = orgRepo.createIssue("ansible/inventory/group_vars/all.yml contains unknown users")
                        .label("admin")
                        .body(stringWriter.toString())
                        .create();

                logger.infof("Created issue: %s", createdIssue.getUrl());
            }
        }

        logger.info("--> Issue creation DONE");
    }
}
