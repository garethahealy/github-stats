package com.garethahealy.githubstats.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.garethahealy.githubstats.clients.graphql.generated.Blob;
import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.GitObject;
import com.garethahealy.githubstats.clients.graphql.generated.Tree;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Polymorphism for {@code object(expression:…)} and stub alias fields on {@code Repository}.
 */
@RegisterForReflection
public abstract class RepositoryGitObjectFieldsGraphqlMixin {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getObject();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getOwners();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getCodeowners();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getWorkflows();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getTravis();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Commit.class, name = "Commit"),
        @JsonSubTypes.Type(value = Blob.class, name = "Blob"),
        @JsonSubTypes.Type(value = Tree.class, name = "Tree")
    })
    public abstract GitObject getRenovate();
}
