package com.garethahealy.githubstats.rest.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.garethahealy.githubstats.model.RepoInfo;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

public class GitHubService {

    private static final Logger logger = LogManager.getLogger(GitHubService.class);

    public List<RepoInfo> run() throws IOException {
        logger.info("Starting...");

        List<RepoInfo> answer = new ArrayList<>();

        Cache cache = new Cache(new File("/tmp/github-okhttp"), 10 * 1024 * 1024); // 10MB cache
        GitHub gitHub = GitHubBuilder.fromEnvironment()
                .withConnector(new OkHttpConnector(new OkHttpClient.Builder().cache(cache).build()))
                .build();

        logger.info("Connector with cache created.");

        GHOrganization org = gitHub.getOrganization("redhat-cop");
        Map<String, GHRepository> repos = org.getRepositories();

        logger.info("Found {} repos.", repos.size());

        LocalDateTime started = LocalDateTime.now();
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get("target/github-output.csv")), CSVFormat.DEFAULT.withHeader(RepoInfo.Headers.class))) {
            for (Map.Entry<String, GHRepository> current : repos.entrySet()) {
                logger.info("Working on: {}", current.getValue().getName());

                GHRepository repo = current.getValue();
                String repoName = repo.getName();
                List<GHRepository.Contributor> contributors = repo.listContributors().toList();
                List<String> topics = repo.listTopics();
                GHCommit lastCommit = null;
                GHRepositoryCloneTraffic cloneTraffic = null;
                GHRepositoryViewTraffic viewTraffic = null;

                try {
                    lastCommit = repo.listCommits().iterator().next();
                } catch (GHException ex) {
                    //ignore - has no commits
                }

                try {
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
                    GHContent owners = repo.getFileContent("OWNERS");
                    hasOwners = owners != null && owners.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    GHContent codeowners = repo.getFileContent("CODEOWNERS");
                    hasCodeOwners = codeowners != null && codeowners.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    List<GHContent> workflows = repo.getDirectoryContent(".github/workflows");
                    hasWorkflows = workflows != null && workflows.size() > 0;
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                try {
                    GHContent travis = repo.getFileContent(".travis.yml");
                    hasTravis = travis != null && travis.isFile();
                } catch (GHFileNotFoundException ex) {
                    //ignore - file doesnt exist
                }

                RepoInfo repoInfo = new RepoInfo(repoName, lastCommit, contributors, topics, cloneTraffic, viewTraffic, hasOwners, hasCodeOwners, hasWorkflows, hasTravis);
                answer.add(repoInfo);

                csvPrinter.printRecord(repoInfo.toArray());

                if (started.getMinute() != LocalDateTime.now().getMinute()) {
                    started = LocalDateTime.now();
                    csvPrinter.flush();
                }
            }
        }

        return answer;
    }
}
