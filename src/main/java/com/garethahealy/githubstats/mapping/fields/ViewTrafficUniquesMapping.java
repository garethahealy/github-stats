package com.garethahealy.githubstats.mapping.fields;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHRepositoryViewTraffic;
import org.mapstruct.Named;

@ApplicationScoped
@RegisterForReflection
public class ViewTrafficUniquesMapping {

    @Named("viewTrafficUniques")
    public long viewTrafficUniques(GHRepositoryViewTraffic viewTraffic) {
        return viewTraffic == null ? 0 : viewTraffic.getUniques();
    }
}
