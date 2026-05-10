package com.garethahealy.githubstats.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;

import java.io.IOException;

@ApplicationScoped
public class GitHubTrafficLookupService {

    public GHRepositoryCloneTraffic getCloneTraffic(GHRepository repo) throws IOException {
        return repo.getCloneTraffic();
    }

    public GHRepositoryViewTraffic getViewTraffic(GHRepository repo) throws IOException {
        return repo.getViewTraffic();
    }
}
