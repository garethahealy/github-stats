package com.garethahealy.githubstats.mapping.fields;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class CloneTrafficUniquesMapping {

    @Named("cloneTrafficUniques")
    public long cloneTrafficUniques(GHRepositoryCloneTraffic cloneTraffic) {
        return cloneTraffic == null ? 0 : cloneTraffic.getUniques();
    }
}
