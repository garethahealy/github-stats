package com.garethahealy.githubstats.services.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.garethahealy.githubstats.model.EmptyJsonNode;
import com.garethahealy.githubstats.model.csv.Repository;
import com.garethahealy.githubstats.services.GitHubService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@ApplicationScoped
public class CollectStatsService {

    @Inject
    Logger logger;

    private final GitHubService gitHubService;

    @Inject
    public CollectStatsService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public void run(String organization, boolean validateOrgConfig, String output) throws IOException, ExecutionException, InterruptedException {
        GitHub gitHub = gitHubService.getGitHub();
        GHOrganization org = gitHubService.getOrganization(gitHub, organization);
        GHRepository coreOrg = gitHubService.getRepository(org, "org");
        Map<String, GHRepository> repos = gitHubService.getRepositories(org);

        gitHubService.hasRateLimit(gitHub, 3000);

        logger.infof("Found %s repos.", repos.size());

        String configContent = validateOrgConfig ? getOrgConfigYaml(coreOrg) : "";
        JsonNode archivedRepos = validateOrgConfig ? getArchivedRepos(configContent) : new EmptyJsonNode();

        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader((Repository.Headers.class))
                .build();

        int cores = Runtime.getRuntime().availableProcessors() * 2;
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output)), csvFormat)) {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<Repository>> futures = new ArrayList<>();
                for (Map.Entry<String, GHRepository> current : repos.entrySet()) {
                    futures.add(executor.submit(() -> {
                        logger.infof("Working on: %s", current.getValue().getName());

                        GHRepository repo = current.getValue();
                        String repoName = repo.getName();
                        boolean isArchived = repo.isArchived();
                        boolean inConfig = configContent.contains(" " + repoName + ":");
                        boolean inArchivedTeam = isArchived && archivedRepos.get(repoName) != null;

                        List<String> topics = gitHubService.listTopics(repo);
                        List<GHRepository.Contributor> contributors = null;
                        List<GHCommit> commits = null;
                        List<GHIssue> issues = null;
                        List<GHPullRequest> pullRequests = null;
                        GHCommit lastCommit = null;
                        GHRepositoryCloneTraffic cloneTraffic = null;
                        GHRepositoryViewTraffic viewTraffic = null;
                        boolean hasOwners = false;
                        boolean hasCodeOwners = false;
                        boolean hasWorkflows = false;
                        boolean hasTravis = false;
                        boolean hasRenovate = false;

                        if (!isArchived) {
                            contributors = gitHubService.listContributors(repo);
                            issues = gitHubService.listOpenIssues(repo);
                            pullRequests = gitHubService.listOpenPullRequests(repo);
                            commits = gitHubService.listCommits(repo);
                            lastCommit = commits != null && !commits.isEmpty() ? commits.getFirst() : null;
                            cloneTraffic = gitHubService.cloneTraffic(repo);
                            viewTraffic = gitHubService.viewTraffic(repo);
                            hasOwners = gitHubService.hasOwners(repo);
                            hasCodeOwners = gitHubService.hasCodeOwners(repo);
                            hasWorkflows = gitHubService.hasWorkflows(repo);
                            hasTravis = gitHubService.hasTravis(repo);
                            hasRenovate = gitHubService.hasRenovate(repo);
                        }

                        return new Repository(repoName, lastCommit, contributors, commits, issues, pullRequests, topics, cloneTraffic, viewTraffic,
                                hasOwners, hasCodeOwners, hasWorkflows, hasTravis, hasRenovate, inConfig, isArchived, inArchivedTeam);
                    }));

                    if (futures.size() == cores) {
                        for (Future<Repository> future : futures) {
                            csvPrinter.printRecord(future.get().toArray());
                        }

                        csvPrinter.flush();
                        futures.clear();

                        gitHubService.logRateLimit(gitHub);
                    }
                }

                //Incase there are any leftover calls to be made
                for (Future<Repository> future : futures) {
                    csvPrinter.printRecord(future.get().toArray());
                }
            }
        }

        gitHubService.logRateLimit(gitHub);
        logger.infof("Output written to %s", output);
    }

    private String getOrgConfigYaml(GHRepository coreOrg) throws IOException {
        logger.info("Downloading org/config.yaml");

        GHContent orgConfig = coreOrg.getFileContent("config.yaml");
        File configOutputFile = new File("target/core-config.yaml");
        FileUtils.copyInputStreamToFile(orgConfig.read(), configOutputFile);

        return FileUtils.readFileToString(configOutputFile, Charset.defaultCharset());
    }

    private JsonNode getArchivedRepos(String configContent) throws JsonProcessingException {
        YAMLMapper mapper = new YAMLMapper();
        JsonNode configMap = mapper.readValue(configContent, JsonNode.class);

        return configMap.get("orgs").get("redhat-cop").get("teams").get("aarchived").get("repos");
    }
}
