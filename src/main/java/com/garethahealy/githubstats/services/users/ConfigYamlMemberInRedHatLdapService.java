package com.garethahealy.githubstats.services.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@ApplicationScoped
public class ConfigYamlMemberInRedHatLdapService {

    @Inject
    Logger logger;

    @Inject
    @TemplatePath("GitHubMemberInRedHatLdap.ftl")
    Template issue;

    private final GitHubService gitHubService;
    private final LdapService ldapService;
    private final CsvService csvService;

    @Inject
    public ConfigYamlMemberInRedHatLdapService(GitHubService gitHubService, CsvService csvService, LdapService ldapService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapService = ldapService;
    }

    public List<Members> run(String sourceOrg, String sourceRepo, String sourceBranch, String ldapMembersCsv, String supplementaryCsv, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHRepository orgRepo = gitHubService.getRepository(sourceOrg, sourceRepo);

        String configContent = gitHubService.getOrgConfigYaml(orgRepo, sourceBranch);
        List<String> members = gitHubService.getConfigMembers(configContent);

        List<String> ldapCheck = collectMembersToCheck(members, ldapMembersCsv, supplementaryCsv);
        List<Members> usersFoundOrNot = searchViaLdapFor(ldapCheck, failNoVpn);

        logger.info("Finished.");
        return usersFoundOrNot;
    }

    private List<String> collectMembersToCheck(List<String> members, String ldapMembersCsv, String supplementaryCsv) throws IOException {
        List<String> answer = new ArrayList<>();

        Map<String, Members> ldapMembers = csvService.getKnownMembers(ldapMembersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        logger.infof("There are %s config.yaml members", members.size());
        logger.infof("There are %s known members and %s supplementary members in the CSVs, total %s", ldapMembers.size(), supplementaryMembers.size(), (ldapMembers.size() + supplementaryMembers.size()));

        for (String member : members) {
            if (!ldapMembers.containsKey(member) && !supplementaryMembers.containsKey(member)) {
                logger.infof("Adding %s to LDAP check list", member);

                answer.add(member);
            }
        }

        Collections.sort(answer);

        logger.info("--> User Lookup DONE");
        return answer;
    }

    private List<Members> searchViaLdapFor(List<String> ldapCheck, boolean failNoVpn) throws IOException, LdapException {
        List<Members> answer = new ArrayList<>();

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                for (String current : ldapCheck) {
                    String rhEmail = ldapService.searchOnGitHubSocial(connection, current);
                    answer.add(new Members(null, rhEmail, current));
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
}
