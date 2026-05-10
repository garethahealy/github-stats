package com.garethahealy.githubstats.mapping;

import com.garethahealy.githubstats.model.csv.Repository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GraphqlRepositoryToCsvMapperTest {

    @Inject
    GraphqlRepositoryToCsvMapper mapper;

    @Test
    void toCsvRow_handlesNullGraphqlForArchivedRepository() {
        Repository row = mapper.toCsvRow(
            "archived-repo",
            null,
            null,
            null,
            null,
            false,
            true,
            false);

        assertEquals("archived-repo", row.repoName());
        assertTrue(row.isArchived());
        assertEquals(0, row.contributorCount());
        assertEquals(0, row.commitCount());
        assertEquals(0, row.openIssueCount());
        assertEquals(0, row.openPullRequestCount());
        assertEquals(0L, row.clonesInPast14Days());
        assertEquals(0L, row.viewsInPast14Days());
    }

    @Test
    void toCsvRow_handlesNullGraphqlForActiveRepository() {
        Repository row = mapper.toCsvRow(
            "active-repo",
            null,
            List.of(),
            null,
            null,
            true,
            false,
            false);

        assertEquals("active-repo", row.repoName());
        assertFalse(row.isArchived());
        assertTrue(row.inConfig());
        assertEquals(0, row.contributorCount());
    }
}
