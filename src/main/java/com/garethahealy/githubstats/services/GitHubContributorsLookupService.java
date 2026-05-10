package com.garethahealy.githubstats.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class GitHubContributorsLookupService {

    public List<GHRepository.Contributor> getContributors(GHRepository repo) throws IOException {
        return repo.listContributors().toList();
    }
}
