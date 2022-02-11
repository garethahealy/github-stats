package com.garethahealy.githubstats.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;

public class RepoInfo {

    public enum Headers {
        RepoName,
        CoP,
        LastCommitDate,
        LastCommitAuthor,
        ContributorCount,
        CommitCount,
        OpenIssueCount,
        OpenPullRequestCount,
        Topics,
        ClonesInPast14Days,
        ViewsInPast14Days,
        HasOwners,
        HasCodeOwners,
        HasWorkflows,
        HasTravis,
        InConfig,
        IsArchived
    }

    private final String repoName;
    private final String lastCommitAuthor;
    private final String lastCommitDate;
    private final String cop;
    private final int contributorCount;
    private final int commitCount;
    private final int openIssueCount;
    private final int openPullRequestCount;
    private final List<String> topics;
    private final long clonesInPast14Days;
    private final long viewsInPast14Days;
    private final boolean hasOwners;
    private final boolean hasCodeOwners;
    private final boolean hasWorkflows;
    private final boolean hasTravis;
    private final boolean inConfig;
    private final boolean isArchived;

    public RepoInfo(String repoName,
                    GHCommit lastCommit,
                    List<GHRepository.Contributor> contributors,
                    List<GHCommit> commits,
                    List<GHIssue> issues,
                    List<GHPullRequest> pullRequests,
                    List<String> topics,
                    GHRepositoryCloneTraffic cloneTraffic,
                    GHRepositoryViewTraffic viewTraffic,
                    boolean hasOwners,
                    boolean hasCodeOwners,
                    boolean hasWorkflows,
                    boolean hasTravis,
                    boolean inConfig,
                    boolean isArchived) throws IOException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        this.repoName = repoName;
        this.lastCommitAuthor = lastCommit == null || lastCommit.getAuthor() == null ? null : lastCommit.getAuthor().getLogin();
        this.lastCommitDate = lastCommit == null ? null : df.format(lastCommit.getCommitDate());
        this.cop = topics.stream().filter(topic -> topic.contains("-cop") || topic.contains("gpte")).findFirst().orElse(null);
        this.contributorCount = contributors.size();
        this.commitCount = commits == null ? 0 : commits.size();
        this.openIssueCount = issues.size();
        this.openPullRequestCount = pullRequests.size();
        this.topics = topics;
        this.clonesInPast14Days = cloneTraffic == null ? 0 : cloneTraffic.getUniques();
        this.viewsInPast14Days = viewTraffic == null ? 0 : viewTraffic.getUniques();
        this.hasOwners = hasOwners;
        this.hasCodeOwners = hasCodeOwners;
        this.hasWorkflows = hasWorkflows;
        this.hasTravis = hasTravis;
        this.inConfig = inConfig;
        this.isArchived = isArchived;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    public String getLastCommitDate() {
        return lastCommitDate;
    }

    public String getCop() {
        return cop;
    }

    public int getContributorCount() {
        return contributorCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public int getOpenIssueCount() {
        return openIssueCount;
    }

    public int getOpenPullRequestCount() {
        return openPullRequestCount;
    }

    public List<String> getTopics() {
        return topics;
    }

    public long getClonesInPast14Days() {
        return clonesInPast14Days;
    }

    public long getViewsInPast14Days() {
        return viewsInPast14Days;
    }

    public boolean isHasOwners() {
        return hasOwners;
    }

    public boolean isHasCodeOwners() {
        return hasCodeOwners;
    }

    public boolean isHasWorkflows() {
        return hasWorkflows;
    }

    public boolean isHasTravis() {
        return hasTravis;
    }

    public boolean isInConfig() {
        return inConfig;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public List<String> toArray() {
        return Arrays.asList(repoName, cop, lastCommitDate, lastCommitAuthor,
                String.valueOf(contributorCount), String.valueOf(commitCount), String.valueOf(openIssueCount),
                String.valueOf(openPullRequestCount), String.join(",", topics),
                String.valueOf(clonesInPast14Days), String.valueOf(viewsInPast14Days),
                String.valueOf(hasOwners), String.valueOf(hasCodeOwners),
                String.valueOf(hasWorkflows), String.valueOf(hasTravis),
                String.valueOf(inConfig), String.valueOf(isArchived));
    }
}
