package com.garethahealy.githubstats.clients;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GitHubRestClientConfigTest {

    @Inject
    GitHub client;

    @Test
    void getClient() throws IOException {
        assertNotNull(client);
        assertNotNull(client.getRateLimit());
    }
}
