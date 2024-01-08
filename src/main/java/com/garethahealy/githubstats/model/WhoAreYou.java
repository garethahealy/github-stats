package com.garethahealy.githubstats.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.lang3.builder.CompareToBuilder;

@RegisterForReflection
public record WhoAreYou(String username, String repo) implements Comparable<WhoAreYou> {

    @Override
    public int compareTo(WhoAreYou o) {
        return new CompareToBuilder().append(this.username(), o.username()).toComparison();
    }
}
