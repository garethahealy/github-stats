package com.garethahealy.githubstats.clients;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GitHubClientIT {

    @Inject
    GitHubClient gitHubClient;

    @Test
    void canGetClient() throws IOException {
        assertNotNull(gitHubClient.getClient());
    }

    @Test
    void canGetWriteClient() throws IOException {
        assertNotNull(gitHubClient.getWriteClient());
    }
}
