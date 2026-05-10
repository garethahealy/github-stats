package com.garethahealy.githubstats.model.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record GitHubRepoStatsGraphqlData(Repository repository) {
}
