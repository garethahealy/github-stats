package com.garethahealy.githubstats.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

@ApplicationScoped
public class GitHubService {

    @Inject
    Logger logger;

    private GitHub gitHub;

    public GitHub getGitHub() throws IOException {
        if (gitHub == null) {
            logger.info("Starting...");

            gitHub = GitHubBuilder.fromEnvironment().build();
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
        }

        return gitHub;
    }

    public void logRateLimit(GitHub gitHub) throws IOException {
        logger.infof("RateLimit: limit %s, remaining %s, resetDate %s", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());
    }

    public void hasRateLimit(GitHub gitHub, Integer need) throws IOException {
        if ((gitHub.getRateLimit().getRemaining() - need) <= 0) {
            logRateLimit(gitHub);

            throw new IllegalStateException("RateLimit - we think we need " + need + " which is not enough to complete");
        }
    }

    public GHOrganization getOrganization(GitHub gitHub, String organization) throws IOException {
        return gitHub.getOrganization(organization);
    }

    public Map<String, GHRepository> getRepositories(GHOrganization org) throws IOException {
        return org.getRepositories();
    }

    public GHRepository getRepository(String owner, String repo) throws IOException {
        return getGitHub().getRepository(owner + "/" + repo);
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

    public List<GHPullRequest> getOpenPullRequests(GHRepository repo) throws IOException {
        return repo.getPullRequests(GHIssueState.OPEN);
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

    public PagedIterable<GHTeam> listTeams(GHOrganization org) throws IOException {
        return org.listTeams();
    }

    public String getOrgConfigYaml(GHRepository coreOrg) throws IOException {
        return getOrgConfigYaml(coreOrg, "main");
    }

    public String getOrgConfigYaml(GHRepository coreOrg, String branch) throws IOException {
        logger.infof("Downloading %s/%s/config.yaml from %s", coreOrg.getOwnerName(), coreOrg.getName(), branch);

        String answer = null;

        try {
            GHContent orgConfig = coreOrg.getFileContent("config.yaml", branch);
            File configOutputFile = new File("target/core-config.yaml");
            FileUtils.copyInputStreamToFile(orgConfig.read(), configOutputFile);

            answer = FileUtils.readFileToString(configOutputFile, Charset.defaultCharset());
        } catch (GHFileNotFoundException ex) {
            logger.warnf("Did not find %s/%s/config.yaml from %s - maybe branch has been deleted", coreOrg.getOwnerName(), coreOrg.getName(), branch);
            logger.warn("Failure", ex);
        }

        return answer;
    }

    public JsonNode getArchivedRepos(String configContent) throws JsonProcessingException {
        YAMLMapper mapper = new YAMLMapper();
        JsonNode configMap = mapper.readValue(configContent, JsonNode.class);

        return configMap.get("orgs").get("redhat-cop").get("teams").get("aarchived").get("repos");
    }

    public List<String> getConfigMembers(String configContent) throws JsonProcessingException {
        List<String> allMembers = new ArrayList<>();

        YAMLMapper mapper = new YAMLMapper();
        JsonNode configMap = mapper.readValue(configContent, JsonNode.class);

        JsonNode admins = configMap.get("orgs").get("redhat-cop").get("admins");
        JsonNode members = configMap.get("orgs").get("redhat-cop").get("members");

        for (JsonNode current : admins) {
            allMembers.add(current.asText());
        }

        for (JsonNode current : members) {
            allMembers.add(current.asText());
        }

        Collections.sort(allMembers);
        return allMembers;
    }

    public Map<GHUser, String> getContributedTo(GHOrganization org, Set<GHUser> unknown, Set<GHUser> unknownWorksForRH) throws IOException, ExecutionException, InterruptedException {
        Map<GHUser, String> contributedTo = new ConcurrentHashMap<>();

        logger.info("Started to build contribute and commit history...");
        logRateLimit(getGitHub());

        Collection<GHRepository> repositories = getRepositories(org).values();

        int cores = Runtime.getRuntime().availableProcessors() * 2;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<GHRepository>> futures = new ArrayList<>();
            for (GHRepository repository : repositories) {
                futures.add(executor.submit(() -> {
                    logger.infof("Working on: %s", repository.getName());

                    if (!repository.isArchived()) {
                        List<GHRepository.Contributor> contributors = listContributors(repository);
                        for (GHRepository.Contributor contributor : contributors) {
                            if (unknown.contains(contributor) || unknownWorksForRH.contains(contributor)) {
                                if (contributedTo.containsKey(contributor)) {
                                    contributedTo.compute(contributor, (k, message) -> message + ", " + repository.getName());
                                } else {
                                    contributedTo.put(contributor, repository.getName());
                                }
                            }
                        }
                    }

                    return repository;
                }));

                if (futures.size() == cores) {
                    for (Future<GHRepository> future : futures) {
                        future.get();
                    }

                    futures.clear();
                }
            }

            for (Future<GHRepository> future : futures) {
                future.get();
            }
        }

        return contributedTo;
    }
}
