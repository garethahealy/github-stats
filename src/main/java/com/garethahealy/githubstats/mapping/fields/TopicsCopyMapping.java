package com.garethahealy.githubstats.mapping.fields;

import com.garethahealy.githubstats.clients.graphql.generated.Repository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RegisterForReflection
public class TopicsCopyMapping {

    @Named("topicsCopy")
    public List<String> topicsCopy(Repository graphql) {
        List<String> gqlTopics = RepositoryGraphqlSupport.topicNames(graphql);
        return gqlTopics == null ? new ArrayList<>() : new ArrayList<>(gqlTopics);
    }
}
