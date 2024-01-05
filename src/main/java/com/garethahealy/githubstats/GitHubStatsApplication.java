package com.garethahealy.githubstats;

import com.garethahealy.githubstats.commands.CollectStatsCommand;
import com.garethahealy.githubstats.commands.CreateWhoAreYouIssueCommand;
import com.garethahealy.githubstats.commands.QuarkusCommand;
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

    public static void main(String[] args) {
        Quarkus.run(GitHubStatsApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(new QuarkusCommand())
                .addSubcommand(collectStatsCommand)
                .addSubcommand(createWhoAreYouIssueCommand)
                .execute(args);
    }
}
