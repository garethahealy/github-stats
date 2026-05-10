package com.garethahealy.githubstats.mapping.fields;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHRepository;
import org.mapstruct.Named;

import java.util.List;

@ApplicationScoped
@RegisterForReflection
public class ContributorCountMapping {

    @Named("contributorCount")
    public int contributorCount(List<GHRepository.Contributor> contributors) {
        return contributors == null ? 0 : contributors.size();
    }
}
