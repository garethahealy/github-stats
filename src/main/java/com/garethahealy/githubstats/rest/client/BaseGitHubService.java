package com.garethahealy.githubstats.rest.client;

import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

public abstract class BaseGitHubService {

    @Inject
    Logger logger;

    protected GitHub getGitHub() throws IOException {
        logger.info("Starting...");

        GitHub gitHub = GitHubBuilder.fromEnvironment().build();
        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
        if (gitHub.getRateLimit().getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        return gitHub;
    }
}
