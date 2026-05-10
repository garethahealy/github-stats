package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class OpenPullRequestCountMapping {

    @Named("openPullRequestCount")
    public int openPullRequestCount(Repository graphql) {
        if (graphql == null || graphql.getPullRequests() == null) {
            return 0;
        }
        return graphql.getPullRequests().getTotalCount();
    }
}
