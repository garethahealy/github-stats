package com.garethahealy.githubstats.commands;

import picocli.CommandLine;

@CommandLine.Command(
        name = "github-stats",
        description = "GitHub helper utility",
        subcommands = {CommandLine.HelpCommand.class})
public class GitHubStatsCommand {
}
