package com.garethahealy.githubstats.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GitHubOrganizationLookupService {

    private final GitHub client;

    public GitHubOrganizationLookupService(GitHub client) {
        this.client = client;
    }

    public GHOrganization getOrganization(String organization) throws IOException {
        return client.getOrganization(organization);
    }

    public List<GHRepository> getRepositories(GHOrganization org, int repoLimit) throws IOException {
        List<GHRepository> answer = new ArrayList<>();

        int pageSize = repoLimit > 0 ? Math.min(repoLimit, 100) : 100;
        for (GHRepository ghRepository : org.listRepositories(pageSize)) {
            answer.add(ghRepository);
            if (repoLimit > 0 && answer.size() >= repoLimit) {
                break;
            }
        }

        return answer;
    }
}
