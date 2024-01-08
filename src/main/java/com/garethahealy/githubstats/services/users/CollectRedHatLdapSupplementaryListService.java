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
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public void run(String organization, String output, String membersCsv, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        List<GHUser> members = gitHubService.listMembers(org);

        Map<String, Members> knownMembers = csvService.getKnownMembers(membersCsv);

        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader((Members.Headers.class))
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output)), csvFormat)) {
            // Hard code the bot user to be ignored
            csvPrinter.printRecord(new Members("", "ablock@redhat.com", "redhat-cop-ci-bot").toArray());

            if (ldapService.canConnect()) {
                try (LdapConnection connection = ldapService.open()) {
                    for (GHUser user : members) {
                        if (!knownMembers.containsKey(user.getLogin())) {
                            String email = ldapService.searchOnGitHub(connection, user.getLogin());
                            if (!email.isEmpty()) {
                                csvPrinter.printRecord(new Members("", email, user.getLogin()).toArray());
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

        logger.info("Finished.");
    }
}
