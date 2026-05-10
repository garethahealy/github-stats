package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.CommitHistoryConnection;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class CommitCountMapping {

    @Named("commitCount")
    public int commitCount(Repository graphql) {
        return RepositoryGraphqlSupport.tipCommit(graphql)
            .map(Commit::getHistory)
            .map(CommitHistoryConnection::getTotalCount)
            .orElse(0);
    }
}
