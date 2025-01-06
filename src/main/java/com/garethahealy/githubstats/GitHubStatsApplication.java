package com.garethahealy.githubstats;

import com.garethahealy.githubstats.commands.GitHubStatsCommand;
import com.garethahealy.githubstats.commands.ListenCommand;
import com.garethahealy.githubstats.commands.StatsCommand;
import com.garethahealy.githubstats.commands.UsersCommand;
import com.garethahealy.githubstats.commands.stats.CollectStatsCommand;
import com.garethahealy.githubstats.commands.users.*;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;

@QuarkusMain
public class GitHubStatsApplication implements QuarkusApplication {

    @Inject
    ListenCommand listenCommand;

    @Inject
    CollectStatsCommand collectStatsCommand;

    @Inject
    CreateWhoAreYouIssueCommand createWhoAreYouIssueCommand;

    @Inject
    GitHubMemberInRedHatLdapCommand gitHubMemberInRedHatLdapCommand;

    @Inject
    CollectMembersFromRedHatLdapCommand collectMembersFromRedHatLdapCommand;

    @Inject
    ConfigYamlMemberInRedHatLdapCommand configYamlMemberInRedHatLdapCommand;

    @Inject
    LabelPullRequestForNewMembersCommand labelPullRequestForNewMembersCommand;

    @Inject
    QuayStillCorrectCommand quayStillCorrectCommand;

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
                        .addSubcommand(collectMembersFromRedHatLdapCommand)
                        .addSubcommand(configYamlMemberInRedHatLdapCommand)
                        .addSubcommand(labelPullRequestForNewMembersCommand)
                        .addSubcommand(quayStillCorrectCommand))
                .addSubcommand(listenCommand)
                .execute(args);
    }
}
