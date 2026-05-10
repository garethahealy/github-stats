package com.garethahealy.githubstats.clients;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GitHubGraphQLClientTest {

    @Inject
    GitHubGraphQLClient client;

    @Test
    void loadGraphqlDocuments() {
        assertNotNull(client);
    }
}
