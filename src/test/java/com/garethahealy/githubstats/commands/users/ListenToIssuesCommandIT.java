package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.commands.BaseCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListenToIssuesCommandIT extends BaseCommand {

    @Test
    @EnabledIf(value = "isRunnerSet")
    void run() throws IOException, InterruptedException, TimeoutException {
        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "listen-to-issues", "--dry-run=true", "--organization=redhat-cop", "--issue-repo=org", "--processors=AddMeAsMember", "--ldap-members-csv=ldap-members.csv", "--supplementary-csv=supplementary.csv", "--fail-if-no-vpn=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());
    }
}
