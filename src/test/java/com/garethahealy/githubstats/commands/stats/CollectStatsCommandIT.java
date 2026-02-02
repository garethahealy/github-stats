package com.garethahealy.githubstats.commands.stats;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusMainIntegrationTest
class CollectStatsCommandIT {

    @Test
    @Launch(value = {"stats", "collect-stats", "--organization=redhat-cop", "--csv-output=target/redhat-cop-collect-stats.csv", "--validate-org-config=true", "--repository-limit=5", "--api-limit=400"})
    void run(LaunchResult result) {
        result.echoSystemOut();

        assertNotNull(result.getErrorOutput());
        assertEquals(0, result.exitCode());
    }
}
