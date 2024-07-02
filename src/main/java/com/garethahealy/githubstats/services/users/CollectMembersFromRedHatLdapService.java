package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapGuessService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class CollectMembersFromRedHatLdapService {

    @Inject
    Logger logger;

    private final GitHubService gitHubService;
    private final LdapGuessService ldapGuessService;
    private final LdapService ldapService;
    private final CsvService csvService;

    @Inject
    public CollectMembersFromRedHatLdapService(GitHubService gitHubService, CsvService csvService, LdapService ldapService, LdapGuessService ldapGuessService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapService = ldapService;
        this.ldapGuessService = ldapGuessService;
    }

    public void run(String organization, String output, String ldapMembersCsv, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException, TemplateException, ExecutionException, InterruptedException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        List<GHUser> githubMembers = gitHubService.listMembers(org);

        Map<String, Members> ldapMembers = csvService.getKnownMembers(ldapMembersCsv);

        Pair<List<Members>, List<GHUser>> membersPair = collectMembers(githubMembers, ldapMembers, failNoVpn);
        List<Members> foundMembers = membersPair.getLeft();
        List<GHUser> attemptToGuess = membersPair.getRight();

        csvService.writeLdapMembersCsv(output, foundMembers, ldapMembers.isEmpty());
        ldapGuessService.attemptToGuess(ldapMembers, attemptToGuess, shouldGuess, failNoVpn, org);

        logger.info("Finished.");
    }

    private Pair<List<Members>, List<GHUser>> collectMembers(List<GHUser> githubMembers, Map<String, Members> ldapMembers, boolean failNoVpn) throws IOException, LdapException {
        List<Members> foundMembers = new ArrayList<>();
        List<GHUser> attemptToGuess = new ArrayList<>();

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                if (!ldapMembers.containsKey("redhat-cop-ci-bot")) {
                    foundMembers.add(new Members(date, "ablock@redhat.com", "redhat-cop-ci-bot"));
                }

                for (GHUser user : githubMembers) {
                    if (!ldapMembers.containsKey(user.getLogin())) {
                        String rhEmail = ldapService.searchOnGitHubSocial(connection, user.getLogin());
                        if (rhEmail.isEmpty()) {
                            attemptToGuess.add(user);
                        } else {
                            foundMembers.add(new Members(date, rhEmail, user.getLogin()));
                        }
                    }
                }
            }
        } else {
            if (failNoVpn) {
                throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
            }
        }

        logger.info("--> Collect DONE");

        return Pair.of(foundMembers, attemptToGuess);
    }
}
