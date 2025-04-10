package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.services.ldap.NoopLdapGuessService;
import com.garethahealy.githubstats.services.users.CreateWhoAreYouIssueService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.kohsuke.github.GHPermissionType;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Dependent
@CommandLine.Command(name = "create-who-are-you-issues", mixinStandardHelpOptions = true, description = "Creates a single issue in the org config for users that are not known")
public class CreateWhoAreYouIssueCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-repo", "--issue-repo"}, description = "Repo where the issues should be created, i.e.: 'org'")
    String orgRepo;

    @CommandLine.Option(names = {"-i", "--ldap-members-csv"}, description = "CSV of known members, generated by 'collect-members-from-ldap'", defaultValue = "ldap-members.csv")
    String ldapMembersCsv;

    @CommandLine.Option(names = {"-s", "--supplementary-csv"}, description = "CSV of known members collected manually or via the google form", defaultValue = "supplementary.csv")
    String supplementaryCsv;

    @CommandLine.Option(names = {"-p", "--permission"}, description = "Permission to search against; ADMIN, WRITE, READ", required = true)
    String permission;

    @CommandLine.Option(names = {"-l", "--team-limit"}, description = "Number of GitHub teams to check", defaultValue = "0")
    int limit;

    @CommandLine.Option(names = {"-g", "--guess"}, description = "Attempt to guess users we cant look up via InfoSec LDAP", defaultValue = "false")
    boolean shouldGuess;

    @CommandLine.Option(names = {"-dry", "--dry-run"}, description = "Dry-run aka don't actually create the GitHub issues", required = true)
    boolean dryRun;

    @CommandLine.Option(names = {"-vpn", "--fail-if-no-vpn"}, description = "Throw an exception if can't connect to LDAP", defaultValue = "true")
    boolean failNoVpn;

    @Inject
    CreateWhoAreYouIssueService createWhoAreYouIssueService;

    @Inject
    NoopLdapGuessService noopLdapGuessService;

    @Override
    public void run() {
        try {
            if (!Files.exists(Path.of(ldapMembersCsv))) {
                throw new FileNotFoundException("--ldap-members-csv=" + ldapMembersCsv + " not found.");
            }

            if (!Files.exists(Path.of(supplementaryCsv))) {
                throw new FileNotFoundException("--supplementary-csv=" + supplementaryCsv + " not found.");
            }

            if (!dryRun && (orgRepo == null || orgRepo.isEmpty())) {
                throw new IllegalArgumentException("--dry-run=" + dryRun + " but --issue-repo=" + orgRepo + " - 'issue-repo' cant be empty if 'dry-run' is false");
            }

            if (!shouldGuess) {
                createWhoAreYouIssueService.setLdapGuessService(noopLdapGuessService);
            }

            createWhoAreYouIssueService.run(organization, orgRepo, new File(ldapMembersCsv), new File(supplementaryCsv), convert(permission), limit, dryRun, failNoVpn);
        } catch (IOException | TemplateException | ExecutionException | InterruptedException | LdapException e) {
            throw new RuntimeException(e);
        }
    }

    private GHPermissionType convert(String permissions) {
        GHPermissionType answer;
        if (permissions.equalsIgnoreCase("ADMIN")) {
            answer = GHPermissionType.ADMIN;
        } else if (permissions.equalsIgnoreCase("WRITE")) {
            answer = GHPermissionType.WRITE;
        } else if (permissions.equalsIgnoreCase("READ")) {
            answer = GHPermissionType.READ;
        } else {
            throw new IllegalArgumentException("--permission=" + permissions + " is invalid.");
        }

        return answer;
    }
}
