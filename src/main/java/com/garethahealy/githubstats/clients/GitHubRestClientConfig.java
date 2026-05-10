package com.garethahealy.githubstats.clients;

import com.garethahealy.githubstats.config.GitHubConfigProperties;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

@Startup
@Singleton
public class GitHubRestClientConfig {

    private final Logger logger;
    private final GitHubConfigProperties github;

    public GitHubRestClientConfig(Logger logger, GitHubConfigProperties github) {
        this.logger = logger;
        this.github = github;
    }

    @Singleton
    @Produces
    public GitHub getClient() throws IOException {
        return getClientVia(new GitHubBuilder().withOAuthToken(github.oauth(), github.login()));
    }

    private GitHub getClientVia(GitHubBuilder builder) throws IOException {
        GitHub gitHub = builder.build();
        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        GHRateLimit rateLimit = gitHub.getRateLimit();
        if (rateLimit.getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", rateLimit.getLimit(), rateLimit.getRemaining(), rateLimit.getResetDate());

        return gitHub;
    }
}
