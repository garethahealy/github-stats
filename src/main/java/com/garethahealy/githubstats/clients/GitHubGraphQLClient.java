package com.garethahealy.githubstats.clients;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class GitHubGraphQLClient {

    private static final String REPO_STATS_GRAPHQL = "/graphql/queries/GetRepoStats.graphql";

    private final DynamicGraphQLClient client;
    private String repoStatsQuery;

    public GitHubGraphQLClient(@GraphQLClient("github-graphql") DynamicGraphQLClient client) {
        this.client = client;
    }

    @PostConstruct
    void loadGraphqlDocuments() throws IOException {
        this.repoStatsQuery = readClasspath(REPO_STATS_GRAPHQL);
    }

    private static String readClasspath(String path) throws IOException {
        try (InputStream in = Objects.requireNonNull(GitHubGraphQLClient.class.getResourceAsStream(path), "Missing classpath resource: " + path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public JsonObject getRepoStats(String owner, String name) throws ExecutionException, InterruptedException {
        Response response = client.executeSync(repoStatsQuery, Map.of("owner", owner, "name", name));
        if (response.hasError()) {
            throw new IllegalStateException("GraphQL errors: " + response.getErrors());
        }

        return response.getData();
    }
}
