package com.garethahealy.githubstats.commands;

import com.garethahealy.githubstats.commands.users.CollectMembersFromRedHatLdapCommand;
import com.garethahealy.githubstats.commands.users.CreateWhoAreYouIssueCommand;
import com.garethahealy.githubstats.commands.users.ListenToIssuesCommand;
import com.garethahealy.githubstats.commands.users.ListenToPullRequestsCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "users",
    description = "Users operations",
    subcommands = {CollectMembersFromRedHatLdapCommand.class,
        CreateWhoAreYouIssueCommand.class,
        ListenToIssuesCommand.class,
        ListenToPullRequestsCommand.class, CommandLine.HelpCommand.class})
public class UsersCommand {
}
