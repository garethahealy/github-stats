package com.garethahealy.githubstats.rest.client;

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

import com.garethahealy.githubstats.model.RepoInfo;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

public class CollectStatsService {

    private static final Logger logger = LogManager.getLogger(CollectStatsService.class);

    public List<RepoInfo> run() throws IOException {
        logger.info("Starting...");

        List<RepoInfo> answer = new ArrayList<>();

        Cache cache = new Cache(new File("/tmp/github-okhttp"), 10 * 1024 * 1024); // 10MB cache
        GitHub gitHub = GitHubBuilder.fromEnvironment()
                .withConnector(new OkHttpGitHubConnector(new OkHttpClient.Builder().cache(cache).build()))
                .build();

        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        logger.info("Connector with cache created.");
        logger.info("RateLimit: limit {}, remaining {}, resetDate {}", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());

        if (gitHub.getRateLimit().getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        GHOrganization org = gitHub.getOrganization("redhat-cop");

        logger.info("Downloading org/config.yaml");

        GHRepository coreOrg = org.getRepository("org");
        GHContent orgConfig = coreOrg.getFileContent("config.yaml");
        File configOutputFile = new File("target/core-config.yaml");
        FileUtils.copyInputStreamToFile(orgConfig.read(), configOutputFile);
        String configContent = FileUtils.readFileToString(configOutputFile, Charset.defaultCharset());

        LocalDateTime flushAt = LocalDateTime.now();
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get("target/github-output.csv")), CSVFormat.DEFAULT.withHeader(RepoInfo.Headers.class))) {
            Map<String, GHRepository> repos = org.getRepositories();
            logger.info("Found {} repos.", repos.size());

            int i = 1;
            for (Map.Entry<String, GHRepository> current : repos.entrySet()) {
                logger.info("Working on: {} - {} / {}", current.getValue().getName(), i, repos.size());

                GHRepository repo = current.getValue();
                String repoName = repo.getName();

                logger.info("-> listContributors");
                List<GHRepository.Contributor> contributors = repo.listContributors().toList();
                List<GHCommit> commits = null;

                logger.info("-> listIssues");
                List<GHIssue> issues = repo.getIssues(GHIssueState.OPEN);

                logger.info("-> listPullRequests");
                List<GHPullRequest> pullRequests = repo.getPullRequests(GHIssueState.OPEN);

                logger.info("-> listTopics");
                List<String> topics = repo.listTopics();
                GHCommit lastCommit = null;
                GHRepositoryCloneTraffic cloneTraffic = null;
                GHRepositoryViewTraffic viewTraffic = null;
                boolean inConfig = configContent.contains(repoName);
                boolean isArchived = repo.isArchived();

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
                    //ignore - token doesnt have access to this repo to get traffic
                }

                boolean hasOwners = false;
                boolean hasCodeOwners = false;
                boolean hasWorkflows = false;
                boolean hasTravis = false;

                try {
                    logger.info("-> OWNERS");
                    GHContent owners = repo.getFileContent("OWNERS");
                    hasOwners = owners != null && owners.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    logger.info("-> CODEOWNERS");
                    GHContent codeowners = repo.getFileContent("CODEOWNERS");
                    hasCodeOwners = codeowners != null && codeowners.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    logger.info("-> .github/workflows");
                    List<GHContent> workflows = repo.getDirectoryContent(".github/workflows");
                    hasWorkflows = workflows != null && workflows.size() > 0;
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    logger.info("-> .travis.yml");
                    GHContent travis = repo.getFileContent(".travis.yml");
                    hasTravis = travis != null && travis.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                RepoInfo repoInfo = new RepoInfo(repoName, lastCommit, contributors, commits, issues, pullRequests,
                        topics, cloneTraffic, viewTraffic, hasOwners, hasCodeOwners, hasWorkflows, hasTravis, inConfig, isArchived);

                answer.add(repoInfo);

                csvPrinter.printRecord(repoInfo.toArray());

                logger.info("-> DONE");

                if (Duration.between(flushAt, LocalDateTime.now()).getSeconds() > 60) {
                    flushAt = LocalDateTime.now();
                    csvPrinter.flush();

                    logger.info("RateLimit: limit {}, remaining {}, resetDate {}", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
                }

                i++;
            }
        }

        logger.info("Finished.");
        logger.info("RateLimit: limit {}, remaining {}, resetDate {}", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());

        return answer;
    }
}
