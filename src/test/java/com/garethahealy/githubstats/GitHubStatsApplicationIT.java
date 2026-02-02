package com.garethahealy.githubstats;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusMainIntegrationTest
public class GitHubStatsApplicationIT {

    @Test
    @Launch("help")
    void test(LaunchResult result) {
        // Dummy test to just start up the app
    }
}
