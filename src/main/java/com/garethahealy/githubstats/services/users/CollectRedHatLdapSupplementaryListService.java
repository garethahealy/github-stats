package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import com.garethahealy.githubstats.services.LdapGuessService;
import com.garethahealy.githubstats.services.LdapService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CollectRedHatLdapSupplementaryListService {

    @Inject
    Logger logger;

    private final GitHubService gitHubService;
    private final LdapGuessService ldapGuessService;
    private final LdapService ldapService;
    private final CsvService csvService;

    @Inject
    public CollectRedHatLdapSupplementaryListService(GitHubService gitHubService, CsvService csvService, LdapService ldapService, LdapGuessService ldapGuessService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
        this.ldapService = ldapService;
        this.ldapGuessService = ldapGuessService;
    }

    public void run(String organization, String output, String supplementaryCsv, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        List<GHUser> members = gitHubService.listMembers(org);

        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);
        List<GHUser> attemptToGuess = writeCsv(output, members, supplementaryMembers, failNoVpn);
        ldapGuessService.attemptToGuess(attemptToGuess, shouldGuess, failNoVpn);

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
}
