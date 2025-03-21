package com.garethahealy.githubstats.commands.stats;

import com.garethahealy.githubstats.commands.BaseCommand;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectStatsCommandIT extends BaseCommand {

    @Test
    void run() throws IOException, InterruptedException, TimeoutException {
        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "stats", "collect-stats", "--organization=redhat-cop", "--csv-output=target/redhat-cop-collect-stats.csv", "--validate-org-config=true", "--repository-limit=5", "--api-limit=400")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());
    }
}
