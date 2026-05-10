package com.garethahealy.githubstats.config.jackson;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class YAMLMapperConfigTest {

    @Inject
    @Named("yaml")
    YAMLMapper mapper;

    @Test
    void mapper() {
        assertNotNull(mapper);
    }
}
