package com.garethahealy.githubstats.processor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.collections4.IteratorUtils;

import java.util.Set;
import java.util.TreeSet;

@ApplicationScoped
public class ConfigYamlProcessor {

    public Set<String> getReposFromYaml(JsonNode configMap) {
        Set<String> answer = new TreeSet<>();
        if (configMap == null || configMap.isNull() || configMap.isMissingNode()) {
            return answer;
        }

        JsonNode orgs = configMap.get("orgs");
        if (orgs == null || orgs.isMissingNode() || orgs.isNull()) {
            return answer;
        }

        recursiveGetRepos(answer, orgs);

        return answer;
    }

    private void recursiveGetRepos(Set<String> allRepos, JsonNode parent) {
        if (parent == null || parent.isMissingNode() || parent.isNull() || !parent.isContainerNode()) {
            return;
        }

        for (JsonNode child : parent) {
            JsonNode repos = child.get("repos");

            if (repos != null && repos.isObject()) {
                allRepos.addAll(IteratorUtils.toList(repos.fieldNames()));
            }

            recursiveGetRepos(allRepos, child);
        }
    }

    public Set<String> getArchivedReposFromYaml(JsonNode configMap) {
        Set<String> answer = new TreeSet<>();
        if (configMap == null || configMap.isNull() || configMap.isMissingNode()) {
            return answer;
        }

        JsonNode archived = configMap.path("orgs").path("redhat-cop").path("teams").path("aarchived").path("repos");
        if (!archived.isObject()) {
            return answer;
        }

        answer.addAll(IteratorUtils.toList(archived.fieldNames()));

        return answer;
    }
}
