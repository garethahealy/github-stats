package com.garethahealy.githubstats.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class YAMLMapperConfigTest {

    @Test
    void mapper() {
        assertNotNull(new YAMLMapperConfig().mapper());
    }
}
