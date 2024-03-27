package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CollectRedHatLdapSupplementaryListService {

    @Inject
    Logger logger;

    private final GitHubService gitHubService;
    private final LdapService ldapService;
    private final CsvService csvService;

    @Inject
    public CollectRedHatLdapSupplementaryListService(GitHubService gitHubService, CsvService csvService, LdapService ldapService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapService = ldapService;
    }

    public void run(String organization, String output, String supplementaryCsv, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        List<GHUser> members = gitHubService.listMembers(org);

        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);
        List<GHUser> attemptToGuess = writeCsv(output, members, supplementaryMembers, failNoVpn);
        attemptToGuess(attemptToGuess, shouldGuess, failNoVpn);

        logger.info("Finished.");
    }

    private List<GHUser> writeCsv(String output, List<GHUser> members, Map<String, Members> supplementaryMembers, boolean failNoVpn) throws IOException, LdapException {
        List<GHUser> attemptToGuess = new ArrayList<>();

        CSVFormat.Builder csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT);
        if (supplementaryMembers.isEmpty()) {
            csvFormat.setHeader(Members.Headers.class);
        }

        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output), StandardOpenOption.APPEND), csvFormat.build())) {
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            if (!supplementaryMembers.containsKey("redhat-cop-ci-bot")) {
                // Hard code the bot user to be ignored
                csvPrinter.printRecord(new Members(date, "ablock@redhat.com", "redhat-cop-ci-bot").toArray());
            }

            if (ldapService.canConnect()) {
                try (LdapConnection connection = ldapService.open()) {
                    for (GHUser user : members) {
                        if (!supplementaryMembers.containsKey(user.getLogin())) {
                            String rhEmail = ldapService.searchOnGitHubSocial(connection, user.getLogin());
                            if (rhEmail.isEmpty()) {
                                attemptToGuess.add(user);
                            } else {
                                csvPrinter.printRecord(new Members(date, rhEmail, user.getLogin()).toArray());
                            }
                        }
                    }
                }
            } else {
                if (failNoVpn) {
                    throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
                }
            }
        }

        logger.info("--> Write CSV DONE");

        return attemptToGuess;
    }

    private void attemptToGuess(List<GHUser> members, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException {
        if (shouldGuess) {
            Map<String, GHUser> guessed = new HashMap<>();
            List<GHUser> unknown = new ArrayList<>();

            if (ldapService.canConnect()) {
                try (LdapConnection connection = ldapService.open()) {
                    for (GHUser user : members) {
                        Pair<String, GHUser> guess = guessViaGithubProfileEmail(connection, user);
                        if (guess == null) {
                            guess = guessViaGithubLogin(connection, user);
                        }

                        if (guess == null) {
                            guess = guessViaGithubProfileName(connection, user);
                        }

                        if (guess == null) {
                            unknown.add(user);
                        } else {
                            guessed.put(guess.getKey(), guess.getValue());
                        }
                    }
                }
            } else {
                if (failNoVpn) {
                    throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
                }
            }

            for (GHUser current : unknown) {
                logger.infof("Unable to work out: %s", current.getLogin());
            }

            StringBuilder emailList = new StringBuilder();
            for (Map.Entry<String, GHUser> current : guessed.entrySet()) {
                logger.infof("Think %s is %s", current.getValue().getLogin(), current.getKey());
                emailList.append(current.getKey()).append(",");
            }

            logger.infof("%s", emailList.toString());
        }

        logger.info("--> Attempt to guess DONE");
    }

    private Pair<String, GHUser> guessViaGithubProfileEmail(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (user.getEmail().contains("@redhat.com")) {
                String rhEmail = ldapService.searchOnEmail(connection, user.getEmail());
                if (!rhEmail.isEmpty()) {
                    answer = Pair.of(rhEmail, user);
                }
            }
        }

        return answer;
    }

    private Pair<String, GHUser> guessViaGithubLogin(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        String rhEmail = ldapService.searchOnGitHubLogin(connection, user.getName());
        if (!rhEmail.isEmpty()) {
            answer = Pair.of(rhEmail, user);
        }

        return answer;
    }

    private Pair<String, GHUser> guessViaGithubProfileName(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        if (user.getName() != null && !user.getName().isEmpty()) {
            String rhEmail = ldapService.searchOnName(connection, user.getName());
            if (!rhEmail.isEmpty()) {
                answer = Pair.of(rhEmail, user);
            }
        }

        return answer;
    }
}
