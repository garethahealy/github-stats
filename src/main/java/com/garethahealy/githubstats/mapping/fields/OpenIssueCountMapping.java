package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class OpenIssueCountMapping {

    @Named("openIssueCount")
    public int openIssueCount(Repository graphql) {
        if (graphql == null || graphql.getIssues() == null) {
            return 0;
        }
        return graphql.getIssues().getTotalCount();
    }
}
