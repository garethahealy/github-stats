package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.services.users.LabelPullRequestForNewMembersService;
import com.garethahealy.githubstats.services.users.QuayStillCorrectService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Dependent
@CommandLine.Command(name = "quay-still-correct", mixinStandardHelpOptions = true, description = "Checks any open pull requests that have config.yaml changes and validates the users.")
public class QuayStillCorrectCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-repo", "--issue-repo"}, description = "Repo where the issues should be created, i.e.: 'org'")
    String orgRepo;

    @CommandLine.Option(names = {"-dry", "--dry-run"}, description = "Dry-run aka don't actually create the GitHub issue", required = true)
    boolean dryRun;

    @CommandLine.Option(names = {"-vpn", "--fail-if-no-vpn"}, description = "Throw an exception if can't connect to LDAP", defaultValue = "true")
    boolean failNoVpn;

    @Inject
    QuayStillCorrectService quayStillCorrectService;

    @Override
    public void run() {
        try {
            quayStillCorrectService.run(organization, orgRepo, dryRun, failNoVpn);
        } catch (IOException | TemplateException | LdapException e) {
            throw new RuntimeException(e);
        }
    }
}