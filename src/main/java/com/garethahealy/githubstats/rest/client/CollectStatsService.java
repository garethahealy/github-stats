package com.garethahealy.githubstats.rest.client;

import com.garethahealy.githubstats.model.RepoInfo;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CollectStatsService extends BaseGitHubService {

    @Inject
    Logger logger;

    public List<RepoInfo> run(String organization, boolean validateOrgConfig, String output) throws IOException {
        List<RepoInfo> answer = new ArrayList<>();

        GitHub gitHub = getGitHub();
        GHOrganization org = gitHub.getOrganization(organization);

        String configContent = validateOrgConfig ? getOrgConfigYaml(org) : "";

        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader((RepoInfo.Headers.class)).build();
        LocalDateTime flushAt = LocalDateTime.now();
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output)), csvFormat)) {
            Map<String, GHRepository> repos = org.getRepositories();
            logger.infof("Found %s repos.", repos.size());

            int i = 1;
            for (Map.Entry<String, GHRepository> current : repos.entrySet()) {
                logger.infof("Working on: %s - %s / %s", current.getValue().getName(), i, repos.size());

                GHRepository repo = current.getValue();
                String repoName = repo.getName();

                List<GHRepository.Contributor> contributors = null;
                List<GHCommit> commits = null;
                List<GHIssue> issues = null;
                List<GHPullRequest> pullRequests = null;
                List<String> topics = new ArrayList<>();
                GHCommit lastCommit = null;
                GHRepositoryCloneTraffic cloneTraffic = null;
                GHRepositoryViewTraffic viewTraffic = null;
                boolean inConfig = configContent.contains(repoName);
                boolean isArchived = repo.isArchived();
                boolean hasOwners = false;
                boolean hasCodeOwners = false;
                boolean hasWorkflows = false;
                boolean hasTravis = false;
                boolean hasRenovate = false;

                if (!isArchived) {
                    logger.info("-> listContributors");
                    contributors = repo.listContributors().toList();

                    logger.info("-> listIssues");
                    issues = repo.getIssues(GHIssueState.OPEN);

                    logger.info("-> listPullRequests");
                    pullRequests = repo.getPullRequests(GHIssueState.OPEN);

                    logger.info("-> listTopics");
                    topics = repo.listTopics();

                    try {
                        logger.info("-> listCommits");
                        commits = repo.listCommits().toList();
                        lastCommit = commits.get(0);
                    } catch (GHException | IOException ex) {
                        //ignore - has no commits
                    }

                    try {
                        logger.info("-> Traffic");
                        cloneTraffic = repo.getCloneTraffic();
                        viewTraffic = repo.getViewTraffic();
                    } catch (GHException | GHFileNotFoundException ex) {
                        //ignore - token doesn't have access to this repo to get traffic
                    }

                    try {
                        logger.info("-> OWNERS");
                        GHContent owners = repo.getFileContent("OWNERS");
                        hasOwners = owners != null && owners.isFile();
                    } catch (GHFileNotFoundException ex) {
                        //ignore - file doesn't exist
                    }

                    try {
                        logger.info("-> CODEOWNERS");
                        GHContent codeowners = repo.getFileContent("CODEOWNERS");
                        hasCodeOwners = codeowners != null && codeowners.isFile();
                    } catch (GHFileNotFoundException ex) {
                        //ignore - file doesn't exist
                    }

                    try {
                        logger.info("-> .github/workflows");
                        List<GHContent> workflows = repo.getDirectoryContent(".github/workflows");
                        hasWorkflows = workflows != null && !workflows.isEmpty();
                    } catch (GHFileNotFoundException ex) {
                        //ignore - file doesn't exist
                    }

                    try {
                        logger.info("-> .travis.yml");
                        GHContent travis = repo.getFileContent(".travis.yml");
                        hasTravis = travis != null && travis.isFile();
                    } catch (GHFileNotFoundException ex) {
                        //ignore - file doesn't exist
                    }

                    try {
                        logger.info("-> renovate.json");
                        GHContent renovate = repo.getFileContent("renovate.json");
                        hasRenovate = renovate != null && renovate.isFile();
                    } catch (GHFileNotFoundException ex) {
                        //ignore - file doesn't exist
                    }
                }

                RepoInfo repoInfo = new RepoInfo(repoName, lastCommit, contributors, commits, issues, pullRequests,
                        topics, cloneTraffic, viewTraffic, hasOwners, hasCodeOwners, hasWorkflows, hasTravis, hasRenovate, inConfig, isArchived);

                answer.add(repoInfo);

                csvPrinter.printRecord(repoInfo.toArray());

                logger.info("-> DONE");

                if (Duration.between(flushAt, LocalDateTime.now()).getSeconds() > 60) {
                    flushAt = LocalDateTime.now();
                    csvPrinter.flush();

                    logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
                }

                i++;
            }
        }

        logger.info("Finished.");
        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());

        return answer;
    }

    private String getOrgConfigYaml(GHOrganization org) throws IOException {
        logger.info("Downloading org/config.yaml");

        GHRepository coreOrg = org.getRepository("org");
        GHContent orgConfig = coreOrg.getFileContent("config.yaml");
        File configOutputFile = new File("target/core-config.yaml");
        FileUtils.copyInputStreamToFile(orgConfig.read(), configOutputFile);

        return FileUtils.readFileToString(configOutputFile, Charset.defaultCharset());
    }
}
