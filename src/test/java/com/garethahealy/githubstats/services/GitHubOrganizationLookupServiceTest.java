package com.garethahealy.githubstats.services;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GitHubOrganizationLookupServiceTest {

    @Inject
    GitHubOrganizationLookupService gitHubOrganizationLookupService;

    @Test
    void getOrganization() throws IOException {
        GHOrganization org = gitHubOrganizationLookupService.getOrganization("redhat-cop");

        assertNotNull(org);
    }


    @Test
    void getRepositories() throws IOException {
        GHOrganization org = gitHubOrganizationLookupService.getOrganization("redhat-cop");

        List<GHRepository> repos = gitHubOrganizationLookupService.getRepositories(org, 5);

        assertNotNull(repos);
        assertEquals(5, repos.size());
    }
}
