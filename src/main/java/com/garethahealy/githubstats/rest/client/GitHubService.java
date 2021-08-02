package com.garethahealy.githubstats.rest.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garethahealy.githubstats.model.RepoInfo;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHCommit;
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

    public List<RepoInfo> run() throws IOException {
        List<RepoInfo> answer = new ArrayList<>();

        Cache cache = new Cache(new File("/tmp/github-okhttp"), 10 * 1024 * 1024); // 10MB cache
        GitHub gitHub = GitHubBuilder.fromEnvironment()
                .withConnector(new OkHttpConnector(new OkHttpClient.Builder().cache(cache).build()))
                .build();

        GHOrganization org = gitHub.getOrganization("redhat-cop");
        Map<String, GHRepository> repos = org.getRepositories();
        for (Map.Entry<String, GHRepository> current : repos.entrySet()) {
            GHRepository repo = current.getValue();
            String repoName = repo.getName();
            List<GHRepository.Contributor> contributors = repo.listContributors().toList();
            List<String> topics = repo.listTopics();
            GHCommit lastCommit = null;
            GHRepositoryCloneTraffic cloneTraffic = null;
            GHRepositoryViewTraffic viewTraffic = null;

            try {
                lastCommit = repo.listCommits().iterator().next();
                cloneTraffic = repo.getCloneTraffic();
                viewTraffic = repo.getViewTraffic();
            } catch (GHException | GHFileNotFoundException ex) {
                //ignore - dont have access to this repo to get traffic
            }

            answer.add(new RepoInfo(repoName, lastCommit, contributors, topics, cloneTraffic, viewTraffic));
        }

        return answer;
    }
}
