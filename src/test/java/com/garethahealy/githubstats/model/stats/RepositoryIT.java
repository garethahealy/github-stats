package com.garethahealy.githubstats.model.stats;

import com.garethahealy.githubstats.services.github.GitHubClient;
import com.garethahealy.githubstats.services.github.GitHubOrganizationLookupService;
import com.garethahealy.githubstats.services.github.GitHubRepositoryLookupService;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryIT {

    @Test
    void canConstruct() throws IOException {
        GitHubClient client = new GitHubClient();
        GitHubOrganizationLookupService gitHubOrganizationLookupService = new GitHubOrganizationLookupService(Logger.getLogger(GitHubOrganizationLookupService.class), client.getClient());
        GitHubRepositoryLookupService gitHubRepositoryLookupService = new GitHubRepositoryLookupService(Logger.getLogger(GitHubRepositoryLookupService.class));

        GHOrganization org = gitHubOrganizationLookupService.getOrganization("redhat-cop");
        GHRepository repo = gitHubOrganizationLookupService.getRepository(org, "org");
        String repoName = repo.getName();
        boolean isArchived = repo.isArchived();
        boolean inConfig = true;
        boolean inArchivedTeam = false;
        List<String> topics = gitHubRepositoryLookupService.listTopics(repo);
        List<GHRepository.Contributor> contributors = gitHubRepositoryLookupService.listContributors(repo);
        List<GHCommit> commits = gitHubRepositoryLookupService.listCommits(repo);
        List<GHIssue> issues = gitHubRepositoryLookupService.listOpenIssues(repo);
        List<GHPullRequest> pullRequests = gitHubRepositoryLookupService.listOpenPullRequests(repo);
        GHRepositoryCloneTraffic cloneTraffic = gitHubRepositoryLookupService.cloneTraffic(repo);
        GHRepositoryViewTraffic viewTraffic = gitHubRepositoryLookupService.viewTraffic(repo);
        boolean hasOwners = gitHubRepositoryLookupService.hasOwners(repo);
        boolean hasCodeOwners = gitHubRepositoryLookupService.hasCodeOwners(repo);
        boolean hasWorkflows = gitHubRepositoryLookupService.hasWorkflows(repo);
        boolean hasTravis = gitHubRepositoryLookupService.hasTravis(repo);
        boolean hasRenovate = gitHubRepositoryLookupService.hasRenovate(repo);

        Repository repository = Repository.from(repoName, contributors, commits, issues, pullRequests, topics, cloneTraffic, viewTraffic,
                hasOwners, hasCodeOwners, hasWorkflows, hasTravis, hasRenovate, inConfig, isArchived, inArchivedTeam);

        assertNotNull(repository);
        assertNotNull(repository.repoName());
        assertEquals("org", repository.repoName());
        assertNotNull(repository.lastCommitAuthor());
        assertNotNull(repository.lastCommitDate());
        assertNotNull(repository.cop());
        assertTrue(repository.contributorCount() > 0);
        assertTrue(repository.commitCount() > 0);
        assertNotNull(repository.topics());
        assertFalse(repository.topics().isEmpty());
        assertFalse(repository.hasOwners());
        assertTrue(repository.hasCodeOwners());
        assertTrue(repository.hasWorkflows());
        assertFalse(repository.hasTravis());
        assertTrue(repository.hasRenovate());
        assertTrue(repository.inConfig());
        assertFalse(repository.isArchived());
        assertFalse(repository.inArchivedTeam());
    }
}
