package com.garethahealy.githubstats.commands;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusMainIntegrationTest
class CollectStatsCommandIT {

    @Test
    @Launch(value = {"collect-stats", "--organization=redhat-cop", "--csv-output=target/redhat-cop-collect-stats-limit5.csv", "--repository-limit=5"})
    void runWithLimit(LaunchResult result) {
        result.echoSystemOut();

        assertNotNull(result.getErrorOutput());
        assertEquals(0, result.exitCode());
        assertTrue(result.getOutput().contains("Output written to target/redhat-cop-collect-stats-limit5.csv"));
    }

    @Test
    @Launch(value = {"collect-stats", "--organization=redhat-cop", "--csv-output=target/redhat-cop-collect-stats.csv"})
    void run(LaunchResult result) {
        result.echoSystemOut();

        assertNotNull(result.getErrorOutput());
        assertEquals(0, result.exitCode());
        assertTrue(result.getOutput().contains("Output written to target/redhat-cop-collect-stats.csv"));
    }
}
