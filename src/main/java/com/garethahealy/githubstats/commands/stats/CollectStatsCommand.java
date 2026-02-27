package com.garethahealy.githubstats.commands.stats;

import com.garethahealy.githubstats.model.stats.Repository;
import com.garethahealy.githubstats.services.github.GitHubFileRetrievalService;
import com.garethahealy.githubstats.services.github.GitHubOrganizationLookupService;
import com.garethahealy.githubstats.services.github.GitHubRepositoryLookupService;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;
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

    @CommandLine.Option(names = {"-cfg", "--validate-org-config"}, description = "Whether to check the 'org/config.yaml'", defaultValue = "true")
    boolean validateOrgConfig;

    @CommandLine.Option(names = {"-api", "--api-limit"}, description = "Number of API requests needed", defaultValue = "3000")
    int limit;

    @CommandLine.Option(names = {"-l", "--repository-limit"}, description = "Number of GitHub repositories to check", defaultValue = "0")
    int repoLimit;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @Inject
    Logger logger;

    @Inject
    GitHubOrganizationLookupService gitHubOrganizationLookupService;

    @Inject
    GitHubRepositoryLookupService gitHubRepositoryLookupService;

    @Inject
    GitHubFileRetrievalService gitHubFileRetrievalService;

    @Override
    public void run() {
        try {
            Path outputPath = Path.of(output);
            if (!Files.exists(outputPath)) {
                Files.createFile(outputPath);
            }

            GHOrganization org = gitHubOrganizationLookupService.getOrganization(organization);
            GHRepository coreOrg = gitHubOrganizationLookupService.getRepository(org, "org");
            List<GHRepository> repos = gitHubOrganizationLookupService.listRepositories(org);
            List<GHRepository> reposLimit = limit <= 0 ? repos : repos.stream().limit(repoLimit).toList();

            gitHubOrganizationLookupService.hasRateLimit(limit);

            List<Repository> repositories = collect(org, coreOrg, reposLimit, validateOrgConfig);
            write(repositories, outputPath.toFile());

            logger.infof("Output written to %s", output);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Repository> collect(GHOrganization org, GHRepository coreOrg, List<GHRepository> repos, boolean validateOrgConfig) throws IOException, ExecutionException, InterruptedException {
        List<Repository> answer = new ArrayList<>();

        logger.infof("Found %s repos in %s", repos.size(), org.getName());

        GHContent configYaml = gitHubRepositoryLookupService.getConfigYaml(coreOrg, validateOrgConfig);
        Set<String> configRepos = gitHubFileRetrievalService.getRepos(configYaml, validateOrgConfig);
        Set<String> configArchivedRepos = gitHubFileRetrievalService.getArchivedRepos(configYaml, validateOrgConfig);

        try (ExecutorService executor = Executors.newCachedThreadPool()) {
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
        logger.infof("Working on: %s", repo.getName());

        String repoName = repo.getName();
        boolean isArchived = repo.isArchived();
        boolean inConfig = configRepos.contains(repoName);
        boolean inArchivedTeam = isArchived && configArchivedRepos.contains(repoName);

        List<String> topics = gitHubRepositoryLookupService.listTopics(repo);
        List<GHRepository.Contributor> contributors = null;
        List<GHCommit> commits = null;
        List<GHIssue> issues = null;
        List<GHPullRequest> pullRequests = null;
        GHRepositoryCloneTraffic cloneTraffic = null;
        GHRepositoryViewTraffic viewTraffic = null;
        boolean hasOwners = false;
        boolean hasCodeOwners = false;
        boolean hasWorkflows = false;
        boolean hasTravis = false;
        boolean hasRenovate = false;

        if (!isArchived) {
            contributors = gitHubRepositoryLookupService.listContributors(repo);
            issues = gitHubRepositoryLookupService.listOpenIssues(repo);
            pullRequests = gitHubRepositoryLookupService.listOpenPullRequests(repo);
            commits = gitHubRepositoryLookupService.listCommits(repo);
            cloneTraffic = gitHubRepositoryLookupService.cloneTraffic(repo);
            viewTraffic = gitHubRepositoryLookupService.viewTraffic(repo);
            hasOwners = gitHubRepositoryLookupService.hasOwners(repo);
            hasCodeOwners = gitHubRepositoryLookupService.hasCodeOwners(repo);
            hasWorkflows = gitHubRepositoryLookupService.hasWorkflows(repo);
            hasTravis = gitHubRepositoryLookupService.hasTravis(repo);
            hasRenovate = gitHubRepositoryLookupService.hasRenovate(repo);
        }

        return Repository.from(repoName, contributors, commits, issues, pullRequests, topics, cloneTraffic, viewTraffic,
            hasOwners, hasCodeOwners, hasWorkflows, hasTravis, hasRenovate, inConfig, isArchived, inArchivedTeam);
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
