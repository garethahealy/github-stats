package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.services.users.CollectRedHatLdapSupplementaryListService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import picocli.CommandLine;

import java.io.IOException;

@Dependent
@CommandLine.Command(name = "collect-members-from-ldap", mixinStandardHelpOptions = true, description = "Creates a supplementary CSV containing members who have added their GitHub ID to LDAP")
public class CollectRedHatLdapSupplementaryListCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @CommandLine.Option(names = {"-i", "--members-csv"}, description = "CSV of current known members", required = true)
    String membersCsv;

    @CommandLine.Option(names = {"-vpn", "--fail-if-no-vpn"}, description = "Throw an exception if can't connect to LDAP")
    boolean failNoVpn;

    @Inject
    CollectRedHatLdapSupplementaryListService collectRedHatLdapSupplementaryListService;

    @Override
    public void run() {
        try {
            collectRedHatLdapSupplementaryListService.run(organization, output, membersCsv, failNoVpn);
        } catch (IOException | LdapException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}