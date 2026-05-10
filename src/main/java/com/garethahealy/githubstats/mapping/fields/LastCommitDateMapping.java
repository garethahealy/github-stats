package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Commit;
import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class LastCommitDateMapping {

    @Named("lastCommitDate")
    public String lastCommitDate(Repository graphql) {
        return RepositoryGraphqlSupport.latestCommit(graphql)
            .map(Commit::getCommittedDate)
            .map(RepositoryGraphqlSupport::graphqlTimestampToIsoLocalDate)
            .orElse(null);
    }
}
