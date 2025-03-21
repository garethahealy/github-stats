package com.garethahealy.githubstats.model.users;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.kohsuke.github.GHUser;

import java.io.IOException;

/**
 * Wrapper class for GHUser
 */
public record BasicGHUser(String name, String company, String login, String email) implements Comparable<BasicGHUser> {

    public static BasicGHUser from(GHUser user) throws IOException {
        return new BasicGHUser(user.getName(), user.getCompany(), user.getLogin(), user.getEmail());
    }

    @Override
    public int compareTo(BasicGHUser o) {
        return new CompareToBuilder()
                .append(name(), o.name())
                .append(company(), o.company())
                .append(login(), o.login())
                .append(email(), o.email())
                .toComparison();
    }
}
