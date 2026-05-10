package com.garethahealy.githubstats.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigYamlProcessorTest {

    private ConfigYamlProcessor processor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        processor = new ConfigYamlProcessor();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getReposFromYaml_returnsEmpty_whenConfigNull() {
        assertTrue(processor.getReposFromYaml(null).isEmpty());
    }

    @Test
    void getReposFromYaml_returnsEmpty_whenOrgsMissing() throws Exception {
        JsonNode root = objectMapper.readTree("{\"other\":true}");
        assertTrue(processor.getReposFromYaml(root).isEmpty());
    }

    @Test
    void getReposFromYaml_collectsRepoKeysFromNestedStructure() throws Exception {
        String json = """
            {"orgs":[{"repos":{"repo-a":{},"repo-b":{}},"teams":{"aarchived":{"repos":{"inner":{}}}}}]}
            """;
        JsonNode root = objectMapper.readTree(json);
        assertEquals(Set.of("inner", "repo-a", "repo-b"), processor.getReposFromYaml(root));
    }

    @Test
    void getArchivedReposFromYaml_returnsEmpty_whenPathMissing() throws Exception {
        assertTrue(processor.getArchivedReposFromYaml(null).isEmpty());
        JsonNode root = objectMapper.readTree("{\"orgs\":{}}");
        assertTrue(processor.getArchivedReposFromYaml(root).isEmpty());
    }

    @Test
    void getArchivedReposFromYaml_readsRedhatCopArchivedTeamRepos() throws Exception {
        String json = """
            {"orgs":{"redhat-cop":{"teams":{"aarchived":{"repos":{"old-one":{},"old-two":{}}}}}}}
            """;
        JsonNode root = objectMapper.readTree(json);
        assertEquals(Set.of("old-one", "old-two"), processor.getArchivedReposFromYaml(root));
    }
}
