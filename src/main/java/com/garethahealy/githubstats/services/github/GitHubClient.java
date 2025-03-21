package com.garethahealy.githubstats.services.github;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

@Singleton
public class GitHubClient {

    @Singleton
    @Produces
    @Named(value = "write")
    public GitHub getWriteClient() throws IOException {
        String githubOAuth = System.getenv("WRITE_GITHUB_OAUTH");
        return getClientVia(GitHubBuilder.fromEnvironment().withOAuthToken(githubOAuth));
    }

    @Singleton
    @Produces
    @Named(value = "read")
    public GitHub getClient() throws IOException {
        return getClientVia(GitHubBuilder.fromEnvironment());
    }

    private GitHub getClientVia(GitHubBuilder builder) throws IOException {
        GitHub gitHub = builder.build();
        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        if (gitHub.getRateLimit().getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        return gitHub;
    }
}
