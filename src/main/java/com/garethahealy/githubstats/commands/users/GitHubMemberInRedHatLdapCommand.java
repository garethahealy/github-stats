package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.services.users.GitHubMemberInRedHatLdapService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import picocli.CommandLine;

import java.io.IOException;

@Dependent
@CommandLine.Command(name = "github-member-in-ldap", mixinStandardHelpOptions = true, description = "Creates a single issue containing any users that are part of the GitHub Org but not in LDAP")
public class GitHubMemberInRedHatLdapCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-repo", "--issue-repo"}, description = "Repo where the issues should be created, i.e.: 'org'", required = true)
    String orgRepo;

    @CommandLine.Option(names = {"-dry", "--dry-run"}, description = "Dry-run aka don't actually create the GitHub issue", required = true)
    boolean dryRun;

    @CommandLine.Option(names = {"-i", "--members-csv"}, description = "CSV of current known members", required = true)
    String membersCsv;

    @CommandLine.Option(names = {"-s", "--supplementary-csv"}, description = "CSV of current known members, generated by 'collect-members-from-ldap'", required = true)
    String supplementaryCsv;

    @CommandLine.Option(names = {"-vpn", "--fail-if-no-vpn"}, description = "Throw an exception if can't connect to LDAP")
    boolean failNoVpn;

    @Inject
    GitHubMemberInRedHatLdapService gitHubMemberInRedHatLdapService;

    @Override
    public void run() {
        try {
            gitHubMemberInRedHatLdapService.run(organization, orgRepo, dryRun, membersCsv, supplementaryCsv, failNoVpn);
        } catch (IOException | LdapException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}