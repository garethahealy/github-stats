package com.garethahealy.githubstats.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ObjectMapperConfigTest {

    @GraphqlObjectMapper
    ObjectMapper objectMapper;

    @Test
    void mapper() {
        assertNotNull(objectMapper);
    }
}
