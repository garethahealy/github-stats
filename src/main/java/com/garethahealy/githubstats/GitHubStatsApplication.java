package com.garethahealy.githubstats;

import com.garethahealy.githubstats.commands.GitHubStatsCommand;
import com.garethahealy.githubstats.commands.stats.CollectStatsCommand;
import com.garethahealy.githubstats.commands.users.CollectRedHatLdapSupplementaryListCommand;
import com.garethahealy.githubstats.commands.users.CreateWhoAreYouIssueCommand;
import com.garethahealy.githubstats.commands.users.GitHubMemberInRedHatLdapCommand;
import com.garethahealy.githubstats.commands.users.MembersToSupplementaryDiffCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;

@QuarkusMain
public class GitHubStatsApplication implements QuarkusApplication {

    @Inject
    CollectStatsCommand collectStatsCommand;

    @Inject
    CreateWhoAreYouIssueCommand createWhoAreYouIssueCommand;

    @Inject
    GitHubMemberInRedHatLdapCommand gitHubMemberInRedHatLdapCommand;

    @Inject
    MembersToSupplementaryDiffCommand membersToSupplementaryDiffCommand;

    @Inject
    CollectRedHatLdapSupplementaryListCommand collectRedHatLdapSupplementaryListCommand;

    public static void main(String[] args) {
        Quarkus.run(GitHubStatsApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(new GitHubStatsCommand())
                .addSubcommand(collectStatsCommand)
                .addSubcommand(createWhoAreYouIssueCommand)
                .addSubcommand(gitHubMemberInRedHatLdapCommand)
                .addSubcommand(collectRedHatLdapSupplementaryListCommand)
                .addSubcommand(membersToSupplementaryDiffCommand)
                .execute(args);
    }
}
