package com.garethahealy.githubstats.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Singleton;

public class YAMLMapperConfig {

    @Singleton
    YAMLMapper mapper() {
        return new YAMLMapper();
    }
}
