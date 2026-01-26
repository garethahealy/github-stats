package com.garethahealy.githubstats.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GitHubClientConfigTest {

    @Inject
    GitHubClientConfig config;

    @Test
    void getWriteClient() throws IOException {
        GitHub github = config.getWriteClient();

        assertNotNull(github);
        assertNotNull(github.getRateLimit());
    }

    @Test
    void getClient() throws IOException {
        GitHub github = config.getClient();

        assertNotNull(github);
        assertNotNull(github.getRateLimit());
    }
}
