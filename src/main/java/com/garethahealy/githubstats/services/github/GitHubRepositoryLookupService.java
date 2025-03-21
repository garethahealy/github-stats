package com.garethahealy.githubstats.services.github;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

/**
 * Retrieves GitHub Repository data
 */
@ApplicationScoped
public class GitHubRepositoryLookupService {

    private final Logger logger;

    @Inject
    public GitHubRepositoryLookupService(Logger logger) {
        this.logger = logger;
    }

    public List<GHRepository.Contributor> listContributors(GHRepository repo) throws IOException {
        logger.debugf("-> listContributors", repo.getName());
        return repo.listContributors().toList();
    }

    public List<GHIssue> listOpenIssues(GHRepository repo) throws IOException {
        logger.debugf("-> listOpenIssues", repo.getName());
        List<GHIssue> issues = repo.getIssues(GHIssueState.OPEN);
        return issues.stream().filter(issue -> !issue.isPullRequest()).toList();
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
        return hasDirectoryContent(repo, ".github/workflows");
    }

    private boolean hasDirectoryContent(GHRepository repo, String path) {
        boolean answer = false;

        try {
            logger.infof("Downloading %s/%s/%s from %s", repo.getOwnerName(), repo.getName(), path, "default branch");

            List<GHContent> contents = repo.getDirectoryContent(path, null);
            answer = !contents.isEmpty();
        } catch (IOException ex) {
            logger.debugf("Did not find %s/%s/%s from %s - maybe branch has been deleted? %s", repo.getOwnerName(), repo.getName(), path, "default branch", ex.getMessage());
            logger.debug("Failure", ex);
        }

        return answer;
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
            answer = getContent(repo, null, path) != null;
        } catch (IOException ex) {
            logger.warnf("Did not find %s/%s/%s from %s - maybe branch has been deleted? %s", repo.getOwnerName(), repo.getName(), path, "default branch", ex.getMessage());
            logger.debug("Failure", ex);
        }

        return answer;
    }

    public GHContent getConfigYaml(GHRepository coreOrg, boolean validateOrgConfig) throws IOException {
        if (validateOrgConfig) {
            return getContent(coreOrg, "main", "config.yaml");
        } else {
            return null;
        }
    }

    public GHContent getConfigYaml(GHRepository coreOrg, String branch) throws IOException {
        return getContent(coreOrg, branch, "config.yaml");
    }

    public GHContent getAnsibleInventoryGroupVarsAllYml(GHRepository coreOrg) throws IOException {
        return getAnsibleInventoryGroupVarsAllYml(coreOrg, "main");
    }

    public GHContent getAnsibleInventoryGroupVarsAllYml(GHRepository coreOrg, String sourceBranch) throws IOException {
        return getContent(coreOrg, sourceBranch, "ansible/inventory/group_vars/all.yml");
    }

    private GHContent getContent(GHRepository coreOrg, String branch, String fileName) throws IOException {
        GHContent answer = null;

        try {
            logger.infof("Downloading %s/%s/%s from %s", coreOrg.getOwnerName(), coreOrg.getName(), fileName, branch == null ? "default branch" : branch);

            answer = coreOrg.getFileContent(fileName, branch);
        } catch (GHFileNotFoundException ex) {
            logger.warnf("Did not find %s/%s/%s from %s - maybe branch has been deleted? %s", coreOrg.getOwnerName(), coreOrg.getName(), fileName, branch == null ? "default branch" : branch, ex.getMessage());
            logger.debug("Failure", ex);
        }

        return answer;
    }
}
