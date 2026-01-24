package com.garethahealy.githubstats.commands;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
    name = "github-stats",
    description = "GitHub helper utility",
    subcommands = {StatsCommand.class, UsersCommand.class, CommandLine.HelpCommand.class})
public class GitHubStatsCommand {
}
