package com.garethahealy.githubstats.model.stats;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RepositoryTest {

    @Test
    void canConstruct() throws IOException {
        List<String> topics = List.of("core-cop");
        List<GHRepository.Contributor> contributors = List.of(new GHRepository.Contributor());
        List<GHCommit> commits = Collections.emptyList();
        List<GHIssue> issues = List.of(new GHIssue());
        List<GHPullRequest> pullRequests = List.of(new GHPullRequest());

        Repository repository = Repository.from("org", contributors, commits, issues, pullRequests, topics, null, null,
                false, true, true, false, true, true, false, false);

        assertNotNull(repository);
        assertNotNull(repository.repoName());
        assertEquals("org", repository.repoName());
        assertNull(repository.lastCommitAuthor());
        assertNull(repository.lastCommitDate());
        assertNotNull(repository.cop());
        assertTrue(repository.contributorCount() > 0);
        assertEquals(0, repository.commitCount());
        assertNotNull(repository.topics());
        assertFalse(repository.topics().isEmpty());
        assertEquals(0, repository.clonesInPast14Days());
        assertEquals(0, repository.viewsInPast14Days());
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
