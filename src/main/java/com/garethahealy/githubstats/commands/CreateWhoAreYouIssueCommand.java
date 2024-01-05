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

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", defaultValue = "redhat-cop")
    String organization;

    @CommandLine.Option(names = {"-repo", "--org-repo"}, description = "Repo name for 'org'", defaultValue = "org")
    String orgRepo;

    @CommandLine.Option(names = {"-dry", "--dry-run"}, description = "Dry-run aka dont actually create the GitHub issues", defaultValue = "true")
    Boolean dryRun;

    @Inject
    CreateWhoAreYouIssueService createWhoAreYouIssueService;

    @Override
    public void run() {
        try {
            //TODO: for time being, always dry-run
            dryRun = true;
            createWhoAreYouIssueService.run(organization, orgRepo, dryRun);
        } catch (IOException | LdapException e) {
            throw new RuntimeException(e);
        }
    }
}