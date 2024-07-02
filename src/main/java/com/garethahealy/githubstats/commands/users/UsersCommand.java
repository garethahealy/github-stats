package com.garethahealy.githubstats.commands.users;

import picocli.CommandLine;

@CommandLine.Command(
        name = "users",
        description = "Users operations",
        subcommands = {CommandLine.HelpCommand.class})
public class UsersCommand {
}