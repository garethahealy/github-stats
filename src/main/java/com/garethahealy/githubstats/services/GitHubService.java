package com.garethahealy.githubstats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GitHubService {

    @Inject
    Logger logger;

    public GitHub getGitHub() throws IOException {
        logger.info("Starting...");

        GitHub gitHub = GitHubBuilder.fromEnvironment().build();
        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
        if (gitHub.getRateLimit().getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        return gitHub;
    }

    public void logRateLimit(GitHub gitHub) throws IOException {
        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
    }

    public GHOrganization getOrganization(GitHub gitHub, String organization) throws IOException {
        return gitHub.getOrganization(organization);
    }

    public Map<String, GHRepository> getRepositories(GHOrganization org) throws IOException {
        return org.getRepositories();
    }

    public GHRepository getRepository(GHOrganization org, String repo) throws IOException {
        return org.getRepository(repo);
    }

    public List<GHUser> listMembers(GHOrganization org) throws IOException {
        return org.listMembers().toList();
    }

    public List<GHRepository.Contributor> listContributors(GHRepository repo) throws IOException {
        logger.debugf("-> listContributors", repo.getName());
        return repo.listContributors().toList();
    }

    public List<GHIssue> listOpenIssues(GHRepository repo) throws IOException {
        logger.debugf("-> listOpenIssues", repo.getName());
        return repo.getIssues(GHIssueState.OPEN);
    }

    public List<GHPullRequest> listOpenPullRequests(GHRepository repo) throws IOException {
        logger.debugf("-> listOpenPullRequests", repo.getName());
        return repo.getPullRequests(GHIssueState.OPEN);
    }

    public List<String> listTopics(GHRepository repo) throws IOException {
        logger.debugf("-> listTopics", repo.getName());
        return repo.listTopics();
    }

    public List<GHCommit> listCommits(GHRepository repo) {
        List<GHCommit> commits = null;
        try {
            logger.debugf("-> listCommits", repo.getName());
            commits = repo.listCommits().toList();
        } catch (GHException | IOException ex) {
            //ignore - has no commits
            logger.debug(ex);
        }
        return commits;
    }

    public GHRepositoryCloneTraffic cloneTraffic(GHRepository repo) {
        GHRepositoryCloneTraffic traffic = null;
        try {
            logger.debugf("-> cloneTraffic", repo.getName());
            traffic = repo.getCloneTraffic();
        } catch (GHException | IOException ex) {
            //ignore - token doesn't have access to this repo to get traffic
            logger.debug(ex);
        }

        return traffic;
    }

    public GHRepositoryViewTraffic viewTraffic(GHRepository repo) {
        GHRepositoryViewTraffic traffic = null;
        try {
            logger.debugf("-> viewTraffic", repo.getName());
            traffic = repo.getViewTraffic();
        } catch (GHException | IOException ex) {
            //ignore - token doesn't have access to this repo to get traffic
            logger.debug(ex);
        }

        return traffic;
    }

    public boolean hasOwners(GHRepository repo) {
        return hasFileContent(repo, "OWNERS");
    }

    public boolean hasCodeOwners(GHRepository repo) {
        return hasFileContent(repo, "CODEOWNERS");
    }

    public boolean hasWorkflows(GHRepository repo) {
        return hasFileContent(repo, ".github/workflows");
    }

    public boolean hasTravis(GHRepository repo) {
        return hasFileContent(repo, ".travis.yml");
    }

    public boolean hasRenovate(GHRepository repo) {
        return hasFileContent(repo, "renovate.json");
    }

    private boolean hasFileContent(GHRepository repo, String path) {
        boolean answer = false;

        try {
            logger.debugf("-> %s", path, repo.getName());

            GHContent content = repo.getFileContent(path);
            answer = content != null && content.isFile();
        } catch (IOException ex) {
            //ignore - file doesn't exist
            logger.debug(ex);
        }

        return answer;
    }
}
