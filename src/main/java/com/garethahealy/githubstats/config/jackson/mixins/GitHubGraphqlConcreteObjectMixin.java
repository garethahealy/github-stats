package com.garethahealy.githubstats.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Drops {@code __typename} after polymorphic deserialization into {@code Commit} / {@code Blob} / {@code Tree}.
 */
@JsonIgnoreProperties("__typename")
@RegisterForReflection
public abstract class GitHubGraphqlConcreteObjectMixin {
}
