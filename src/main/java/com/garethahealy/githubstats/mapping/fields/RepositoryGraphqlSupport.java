package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shared static helpers for GraphQL {@link Repository} payloads (no CDI).
 */
public final class RepositoryGraphqlSupport {

    private RepositoryGraphqlSupport() {
    }

    public static List<String> topicNames(Repository graphql) {
        if (graphql == null || graphql.getRepositoryTopics() == null || graphql.getRepositoryTopics().getNodes() == null) {
            return List.of();
        }

        return graphql.getRepositoryTopics().getNodes().stream()
            .map(RepositoryTopic::getTopic)
            .filter(Objects::nonNull)
            .map(t -> t.getName())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static String copLabelFromTopics(List<String> gqlTopics) {
        if (gqlTopics == null || gqlTopics.isEmpty()) {
            return null;
        }

        return gqlTopics.stream()
            .filter(topic -> topic.contains("-cop") || topic.contains("gpte") || topic.contains("validated-content"))
            .findFirst()
            .orElse(null);
    }

    public static Optional<Commit> latestCommit(Repository graphql) {
        return tipCommit(graphql).flatMap(tip -> {
            CommitHistoryConnection history = tip.getHistory();
            if (history != null && history.getNodes() != null && !history.getNodes().isEmpty()) {
                return Optional.of(history.getNodes().getFirst());
            }

            return Optional.of(tip);
        });
    }

    public static Optional<Commit> tipCommit(Repository graphql) {
        if (graphql == null) {
            return Optional.empty();
        }

        Ref ref = graphql.getDefaultBranchRef();
        if (ref == null || !(ref.getTarget() instanceof Commit tip)) {
            return Optional.empty();
        }

        return Optional.of(tip);
    }

    public static String graphqlTimestampToIsoLocalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(raw).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {
            if (raw.length() >= 10 && raw.charAt(4) == '-') {
                return raw.substring(0, 10);
            }

            return raw;
        }
    }
}
