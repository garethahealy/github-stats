package com.garethahealy.githubstats;

import com.garethahealy.githubstats.commands.GitHubStatsCommand;
import com.garethahealy.githubstats.commands.stats.CollectStatsCommand;
import com.garethahealy.githubstats.commands.stats.StatsCommand;
import com.garethahealy.githubstats.commands.users.CollectMembersFromRedHatLdapCommand;
import com.garethahealy.githubstats.commands.users.CreateWhoAreYouIssueCommand;
import com.garethahealy.githubstats.commands.users.GitHubMemberInRedHatLdapCommand;
import com.garethahealy.githubstats.commands.users.UsersCommand;
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
    CollectMembersFromRedHatLdapCommand collectMembersFromRedHatLdapCommand;

    public static void main(String[] args) {
        Quarkus.run(GitHubStatsApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(new GitHubStatsCommand())
                .addSubcommand(new CommandLine(new StatsCommand())
                        .addSubcommand(collectStatsCommand))
                .addSubcommand(new CommandLine(new UsersCommand())
                        .addSubcommand(createWhoAreYouIssueCommand)
                        .addSubcommand(gitHubMemberInRedHatLdapCommand)
                        .addSubcommand(collectMembersFromRedHatLdapCommand))
                .execute(args);
    }
}
