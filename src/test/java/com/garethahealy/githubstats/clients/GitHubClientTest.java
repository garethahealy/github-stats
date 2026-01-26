package com.garethahealy.githubstats.clients;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GitHubClientTest {

    @Inject
    GitHubClient client;

    @Test
    void getWriteClient() throws IOException {
        GitHub github = client.getWriteClient();

        assertNotNull(github);
        assertNotNull(github.getRateLimit());
    }

    @Test
    void getClient() throws IOException {
        GitHub github = client.getClient();

        assertNotNull(github);
        assertNotNull(github.getRateLimit());
    }
}
