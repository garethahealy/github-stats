package com.garethahealy.githubstats.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garethahealy.githubstats.clients.GitHubGraphQLClient;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import com.garethahealy.githubstats.model.graphql.GitHubRepoStatsGraphqlData;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubRepositoryStatsServiceTest {

    private final Logger logger = Logger.getLogger(GitHubRepositoryStatsServiceTest.class);

    @Mock
    GitHubGraphQLClient gitHubGraphQLClient;

    @Mock
    ObjectMapper graphqlResponseMapper;

    private GitHubRepositoryStatsService service;

    @BeforeEach
    void setUp() {
        service = new GitHubRepositoryStatsService(logger, gitHubGraphQLClient, graphqlResponseMapper);
    }

    @Test
    void fetchRepositoryStats_returnsRepository_whenGraphQlSucceeds() throws Exception {
        Repository repo = new Repository();
        repo.setName("demo");

        JsonObject data = Json.createObjectBuilder().build();
        when(gitHubGraphQLClient.getRepoStats("owner", "demo")).thenReturn(data);
        when(graphqlResponseMapper.readValue(eq(data.toString()), eq(GitHubRepoStatsGraphqlData.class)))
            .thenReturn(new GitHubRepoStatsGraphqlData(repo));

        assertSame(repo, service.fetchRepositoryStats("owner", "demo"));
    }

    @Test
    void fetchRepositoryStats_returnsNull_whenMapperThrows() throws Exception {
        JsonObject data = Json.createObjectBuilder().build();
        when(gitHubGraphQLClient.getRepoStats("o", "r")).thenReturn(data);
        when(graphqlResponseMapper.readValue(anyString(), eq(GitHubRepoStatsGraphqlData.class)))
            .thenThrow(new IllegalStateException("parse error"));

        assertNull(service.fetchRepositoryStats("o", "r"));
    }

    @Test
    void fetchRepositoryStats_returnsNull_whenGraphQlClientThrows() throws Exception {
        when(gitHubGraphQLClient.getRepoStats("o", "r"))
            .thenThrow(new ExecutionException("boom", new RuntimeException()));

        assertNull(service.fetchRepositoryStats("o", "r"));
    }
}
