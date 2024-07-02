package com.garethahealy.githubstats.commands.stats;

import picocli.CommandLine;

@CommandLine.Command(
        name = "stats",
        description = "Stats collection",
        subcommands = {CommandLine.HelpCommand.class})
public class StatsCommand {
}