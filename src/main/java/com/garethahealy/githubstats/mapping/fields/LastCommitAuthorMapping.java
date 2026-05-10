package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

import java.util.Objects;

@ApplicationScoped
@RegisterForReflection
public class LastCommitAuthorMapping {

    @Named("lastCommitAuthor")
    public String lastCommitAuthor(Repository graphql) {
        return RepositoryGraphqlSupport.latestCommit(graphql)
            .map(Commit::getAuthor)
            .filter(Objects::nonNull)
            .map(a -> a.getUser())
            .filter(Objects::nonNull)
            .map(u -> u.getLogin())
            .orElse(null);
    }
}
