package com.garethahealy.githubstats.commands;

import com.garethahealy.githubstats.rest.client.CreateWhoAreYouIssueService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import picocli.CommandLine;

import java.io.IOException;

@Dependent
@CommandLine.Command(name = "create-who-are-you-issues", mixinStandardHelpOptions = true, description = "Creates an issue per user in the org config that isn't in a CVS")
public class CreateWhoAreYouIssueCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-repo", "--issue-repo"}, description = "Repo where the issues should be created, i.e.: 'org'", required = true)
    String orgRepo;

    @CommandLine.Option(names = {"-dry", "--dry-run"}, description = "Dry-run aka don't actually create the GitHub issues", required = true)
    boolean dryRun;

    @CommandLine.Option(names = {"-i", "--members-csv"}, description = "CSV container current known members", required = true)
    String membersCsv;

    @CommandLine.Option(names = {"-vpn", "--fail-if-no-vpn"}, description = "Throw an exception if can't connect to LDAP")
    boolean failNoVpn;

    @Inject
    CreateWhoAreYouIssueService createWhoAreYouIssueService;

    @Override
    public void run() {
        try {
            //TODO: for time being, always dry-run
            dryRun = true;
            createWhoAreYouIssueService.run(organization, orgRepo, dryRun, membersCsv, failNoVpn);
        } catch (IOException | LdapException e) {
            throw new RuntimeException(e);
        }
    }
}