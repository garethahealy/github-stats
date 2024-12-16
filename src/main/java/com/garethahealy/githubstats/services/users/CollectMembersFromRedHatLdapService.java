package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapGuessService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import java.util.Optional;
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

    public void run(String organization, String output, String ldapMembersCsv, String supplementaryCsv, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException, TemplateException, ExecutionException, InterruptedException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);

        logger.infof("Looking up %s", org.getName());

        List<GHUser> githubMembers = gitHubService.listMembers(org);
        Map<String, Members> ldapMembers = csvService.getKnownMembers(ldapMembersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        List<GHUser> attemptToGuess = collectMembersAndBuildGuessList(githubMembers, ldapMembers, failNoVpn);

        removeFromLdapOrSupplementaryIfNotGitHubMember(githubMembers, ldapMembers, supplementaryMembers);
        removeLdapFromSupplementary(ldapMembers, supplementaryMembers);

        csvService.writeLdapMembersCsv(output, new ArrayList<>(ldapMembers.values()));
        csvService.writeSupplementaryMembersCsv(supplementaryCsv, new ArrayList<>(supplementaryMembers.values()));

        ldapGuessService.attemptToGuess(ldapMembers, attemptToGuess, shouldGuess, failNoVpn, org);

        logger.info("Finished.");
    }

    private List<GHUser> collectMembersAndBuildGuessList(List<GHUser> githubMembers, Map<String, Members> ldapMembers, boolean failNoVpn) throws IOException, LdapException {
        List<GHUser> attemptToGuess = new ArrayList<>();

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                if (!ldapMembers.containsKey("redhat-cop-ci-bot")) {
                    ldapMembers.put("redhat-cop-ci-bot", new Members(date, "ablock@redhat.com", "redhat-cop-ci-bot"));
                }

                for (GHUser user : githubMembers) {
                    if (!ldapMembers.containsKey(user.getLogin())) {
                        String rhEmail = ldapService.searchOnGitHubSocial(connection, user.getLogin());
                        if (rhEmail.isEmpty()) {
                            attemptToGuess.add(user);
                        } else {
                            ldapMembers.put(user.getLogin(), new Members(date, rhEmail, user.getLogin()));
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

        return attemptToGuess;
    }

    private void removeFromLdapOrSupplementaryIfNotGitHubMember(List<GHUser> githubMembers, Map<String, Members> ldapMembers, Map<String, Members> supplementaryMembers) {
        removeFromIfNotGitHubMember(githubMembers, ldapMembers);
        removeFromIfNotGitHubMember(githubMembers, supplementaryMembers);
    }

    private void removeFromIfNotGitHubMember(List<GHUser> githubMembers, Map<String, Members> foundMembers) {
        List<String> ldapKeysToRemove = new ArrayList<>();
        for (Map.Entry<String, Members> member : foundMembers.entrySet()) {
            Optional<GHUser> found = githubMembers.stream().filter((e) -> e.getLogin().equals(member.getKey())).findFirst();
            if (found.isEmpty()) {
                ldapKeysToRemove.add(member.getKey());
            }
        }

        for (String remove : ldapKeysToRemove) {
            logger.infof("%s is in a CSV but no-longer a GitHub member", remove);

            foundMembers.remove(remove);
        }
    }

    private void removeLdapFromSupplementary(Map<String, Members> ldapMembers, Map<String, Members> supplementaryMembers) {
        for (Members current : ldapMembers.values()) {
            if (supplementaryMembers.containsKey(current.getWhatIsYourGitHubUsername())) {
                logger.infof("%s is in LDAP and Supplementary CSV, removing from Supplementary", current.getEmailAddress());

                supplementaryMembers.remove(current.getWhatIsYourGitHubUsername());
            }
        }
    }
}
