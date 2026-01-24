package com.garethahealy.githubstats.commands;

import com.garethahealy.githubstats.commands.stats.CollectStatsCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "stats",
    description = "Stats collection",
    subcommands = {CollectStatsCommand.class, CommandLine.HelpCommand.class})
public class StatsCommand {
}
