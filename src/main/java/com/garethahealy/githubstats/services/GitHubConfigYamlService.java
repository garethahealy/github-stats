package com.garethahealy.githubstats.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class GitHubConfigYamlService {

    private final Logger logger;
    private final YAMLMapper yamlMapper;

    public GitHubConfigYamlService(Logger logger, @Named("yaml") YAMLMapper yamlMapper) {
        this.logger = logger;
        this.yamlMapper = yamlMapper;
    }

    public JsonNode fetchOrgConfigYaml(GHRepository coreOrg) throws IOException {
        JsonNode answer = null;

        String fileName = "config.yaml";
        String branch = "main";

        try {
            logger.infof("Downloading %s/%s/%s from %s", coreOrg.getOwnerName(), coreOrg.getName(), fileName, branch);

            GHContent content = coreOrg.getFileContent(fileName, branch);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(content.read(), StandardCharsets.UTF_8))) {
                answer = yamlMapper.readValue(reader, JsonNode.class);
            }
        } catch (GHFileNotFoundException ex) {
            logger.warnf("Did not find %s/%s/%s from %s : %s", coreOrg.getOwnerName(), coreOrg.getName(), fileName, branch, ex);
        }

        return answer;
    }
}
