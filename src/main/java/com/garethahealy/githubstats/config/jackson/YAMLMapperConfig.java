package com.garethahealy.githubstats.config.jackson;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class YAMLMapperConfig {

    @Produces
    @Singleton
    @Named("yaml")
    public YAMLMapper mapper() {
        return new YAMLMapper();
    }
}
