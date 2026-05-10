package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class HasWorkflowsMapping {

    @Named("hasWorkflows")
    public boolean hasWorkflows(Repository graphql) {
        return graphql != null && graphql.getWorkflows() != null;
    }
}
