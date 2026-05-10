package com.garethahealy.githubstats.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.garethahealy.githubstats.mapping.GraphqlRepositoryToCsvMapper;
import com.garethahealy.githubstats.model.csv.Repository;
import com.garethahealy.githubstats.processor.ConfigYamlProcessor;
import com.garethahealy.githubstats.services.*;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@CommandLine.Command(name = "collect-stats", mixinStandardHelpOptions = true, description = "Collect the stats in CSV format")
public class CollectStatsCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-l", "--repository-limit"}, description = "Max repositories to list and process; 0 means no limit (all org repos)", defaultValue = "0")
    int repoLimit;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @Inject
    Logger logger;

    @Inject
    GitHubOrganizationLookupService gitHubOrganizationLookupService;

    @Inject
    GitHubTrafficLookupService gitHubTrafficLookupService;

    @Inject
    GitHubContributorsLookupService gitHubContributorsLookupService;

    @Inject
    ConfigYamlProcessor configYamlProcessor;

    @Inject
    GitHubRepositoryStatsService gitHubRepositoryStatsService;

    @Inject
    GitHubConfigYamlService gitHubConfigYamlService;

    @Inject
    GraphqlRepositoryToCsvMapper graphqlRepositoryToCsvMapper;

    @Override
    public void run() {
        try {
            Path outputPath = Path.of(output);
            if (!Files.exists(outputPath)) {
                Files.createFile(outputPath);
            }

            GHOrganization org = gitHubOrganizationLookupService.getOrganization(organization);
            List<GHRepository> repos = gitHubOrganizationLookupService.getRepositories(org, repoLimit);

            logger.infof("Found %s repos in %s", repos.size(), org.getName());

            List<Repository> repositories = collect(org, repos);
            write(repositories, outputPath.toFile());

            logger.infof("Output written to %s", output);
        } catch (IOException | InterruptedException | ExecutionException | IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Repository> collect(GHOrganization org, List<GHRepository> repos) throws IOException, ExecutionException, InterruptedException {
        List<Repository> answer = new ArrayList<>();

        JsonNode configMap = gitHubConfigYamlService.fetchOrgConfigYaml(org.getRepository("org"));
        Set<String> configRepos = configYamlProcessor.getReposFromYaml(configMap);
        Set<String> configArchivedRepos = configYamlProcessor.getArchivedReposFromYaml(configMap);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Repository>> futures = new ArrayList<>();
            for (GHRepository current : repos) {
                futures.add(executor.submit(() -> runnable(current, configRepos, configArchivedRepos)));
            }

            for (Future<Repository> future : futures) {
                answer.add(future.get());
            }
        }

        return answer;
    }

    private Repository runnable(GHRepository repo, Set<String> configRepos, Set<String> configArchivedRepos) throws IOException {
        logger.infof("Working on: %s/%s", repo.getOwnerName(), repo.getName());

        boolean inConfig = configRepos.contains(repo.getName());
        boolean inArchivedTeam = repo.isArchived() && configArchivedRepos.contains(repo.getName());

        com.garethahealy.githubstats.clients.graphql.generated.Repository graphqlRepository = null;
        List<GHRepository.Contributor> contributors = null;
        GHRepositoryCloneTraffic cloneTraffic = null;
        GHRepositoryViewTraffic viewTraffic = null;

        if (!repo.isArchived()) {
            graphqlRepository = gitHubRepositoryStatsService.fetchRepositoryStats(repo.getOwnerName(), repo.getName());

            contributors = gitHubContributorsLookupService.getContributors(repo);
            cloneTraffic = gitHubTrafficLookupService.getCloneTraffic(repo);
            viewTraffic = gitHubTrafficLookupService.getViewTraffic(repo);
        }

        return graphqlRepositoryToCsvMapper.toCsvRow(repo.getName(), graphqlRepository, contributors, cloneTraffic, viewTraffic, inConfig, repo.isArchived(), inArchivedTeam);
    }

    private void write(List<Repository> repositories, File output) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader((Repository.Headers.class))
            .get();

        try (Writer writer = Files.newBufferedWriter(output.toPath(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                for (Repository current : repositories) {
                    csvPrinter.printRecord(current.toArray());
                }
            }
        }
    }
}
