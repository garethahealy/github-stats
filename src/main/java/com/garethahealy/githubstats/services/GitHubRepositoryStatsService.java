package com.garethahealy.githubstats.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garethahealy.githubstats.clients.GitHubGraphQLClient;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import com.garethahealy.githubstats.config.jackson.GraphqlObjectMapper;
import com.garethahealy.githubstats.model.graphql.GitHubRepoStatsGraphqlData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class GitHubRepositoryStatsService {

    private final Logger logger;
    private final GitHubGraphQLClient gitHubGraphQLClient;
    private final ObjectMapper graphqlResponseMapper;

    public GitHubRepositoryStatsService(Logger logger, GitHubGraphQLClient gitHubGraphQLClient, @GraphqlObjectMapper ObjectMapper objectMapper) {
        this.logger = logger;
        this.gitHubGraphQLClient = gitHubGraphQLClient;
        this.graphqlResponseMapper = objectMapper;
    }

    public Repository fetchRepositoryStats(String owner, String name) {
        Repository answer = null;

        try {
            JsonObject response = gitHubGraphQLClient.getRepoStats(owner, name);
            GitHubRepoStatsGraphqlData payload = graphqlResponseMapper.readValue(response.toString(), GitHubRepoStatsGraphqlData.class);

            answer = payload.repository();
        } catch (IOException | IllegalStateException | ExecutionException | InterruptedException e) {
            logger.errorf("Error while trying to fetch repository stats for %s/%s because: %s", owner, name, e.getMessage());
        }

        return answer;
    }
}
