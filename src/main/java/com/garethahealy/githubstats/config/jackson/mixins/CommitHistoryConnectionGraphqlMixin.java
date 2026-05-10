package com.garethahealy.githubstats.config.jackson.mixins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.GitObject;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * History nodes are always {@link Commit} in the RepoStats query; avoid {@link GitObject} polymorphism on list elements.
 */
@RegisterForReflection
public abstract class CommitHistoryConnectionGraphqlMixin {

    @JsonDeserialize(contentAs = Commit.class)
    public abstract List<Commit> getNodes();
}
