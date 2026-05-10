package com.garethahealy.githubstats.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garethahealy.githubstats.clients.graphql.generated.*;
import com.garethahealy.githubstats.config.jackson.mixins.CommitHistoryConnectionGraphqlMixin;
import com.garethahealy.githubstats.config.jackson.mixins.GitHubGraphqlConcreteObjectMixin;
import com.garethahealy.githubstats.config.jackson.mixins.RefGitTargetGraphqlMixin;
import com.garethahealy.githubstats.config.jackson.mixins.RepositoryGitObjectFieldsGraphqlMixin;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class ObjectMapperConfig {

    @Produces
    @Singleton
    @GraphqlObjectMapper
    public ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Ref.class, RefGitTargetGraphqlMixin.class);
        mapper.addMixIn(Repository.class, RepositoryGitObjectFieldsGraphqlMixin.class);
        mapper.addMixIn(CommitHistoryConnection.class, CommitHistoryConnectionGraphqlMixin.class);
        mapper.addMixIn(Commit.class, GitHubGraphqlConcreteObjectMixin.class);
        mapper.addMixIn(Blob.class, GitHubGraphqlConcreteObjectMixin.class);
        mapper.addMixIn(Tree.class, GitHubGraphqlConcreteObjectMixin.class);

        return mapper;
    }
}
