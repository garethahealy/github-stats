package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.RepositoryTopic;
import com.garethahealy.githubstats.clients.graphql.generated.RepositoryTopicConnection;
import com.garethahealy.githubstats.clients.graphql.generated.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryGraphqlSupportTest {

    @Test
    void topicNames_returnsEmpty_whenGraphqlNull() {
        assertTrue(RepositoryGraphqlSupport.topicNames(null).isEmpty());
    }

    @Test
    void topicNames_mapsTopicNodes() {
        var topicName = new Topic();
        topicName.setName("containers-cop");
        var repoTopic = new RepositoryTopic();
        repoTopic.setTopic(topicName);
        var conn = new RepositoryTopicConnection();
        conn.setNodes(List.of(repoTopic));

        var gql = new com.garethahealy.githubstats.clients.graphql.generated.Repository();
        gql.setRepositoryTopics(conn);

        assertEquals(List.of("containers-cop"), RepositoryGraphqlSupport.topicNames(gql));
    }

    @Test
    void copLabelFromTopics_returnsNull_whenEmpty() {
        assertNull(RepositoryGraphqlSupport.copLabelFromTopics(List.of()));
        assertNull(RepositoryGraphqlSupport.copLabelFromTopics(null));
    }

    @Test
    void copLabelFromTopics_matchesCopSuffix() {
        assertEquals("foo-cop", RepositoryGraphqlSupport.copLabelFromTopics(List.of("other", "foo-cop")));
    }

    @Test
    void graphqlTimestampToIsoLocalDate_handlesNullAndBlank() {
        assertNull(RepositoryGraphqlSupport.graphqlTimestampToIsoLocalDate(null));
        assertNull(RepositoryGraphqlSupport.graphqlTimestampToIsoLocalDate("   "));
    }

    @Test
    void graphqlTimestampToIsoLocalDate_parsesOffsetDateTime() {
        assertEquals("2024-03-15", RepositoryGraphqlSupport.graphqlTimestampToIsoLocalDate("2024-03-15T12:00:00Z"));
    }

    @Test
    void graphqlTimestampToIsoLocalDate_fallsBackToDatePrefix() {
        assertEquals("2024-03-15", RepositoryGraphqlSupport.graphqlTimestampToIsoLocalDate("2024-03-15 garbage"));
    }
}
