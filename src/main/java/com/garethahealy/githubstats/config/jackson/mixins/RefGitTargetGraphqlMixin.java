package com.garethahealy.githubstats.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.garethahealy.githubstats.clients.graphql.generated.Blob;
import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.GitObject;
import com.garethahealy.githubstats.clients.graphql.generated.Tree;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Polymorphic {@code Ref.target} only (not {@code Commit} nodes inside {@code history}).
 */
@RegisterForReflection
public abstract class RefGitTargetGraphqlMixin {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getTarget();
}
