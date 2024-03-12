package com.garethahealy.githubstats.model.csv;

import org.kohsuke.github.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Repository {

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
        HasRenovate,
        InConfig,
        IsArchived,
        InArchivedTeam
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
    private final boolean hasRenovate;
    private final boolean inConfig;
    private final boolean isArchived;
    private final boolean inArchivedTeam;

    public Repository(String repoName,
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
                      boolean hasRenovate,
                      boolean inConfig,
                      boolean isArchived,
                      boolean inArchivedTeam) throws IOException {
        this.repoName = repoName;
        this.lastCommitAuthor = lastCommit == null || lastCommit.getAuthor() == null ? null : lastCommit.getAuthor().getLogin();
        this.lastCommitDate = lastCommit == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.ofInstant(lastCommit.getCommitDate().toInstant(), ZoneId.systemDefault()));
        this.cop = topics == null ? null : topics.stream().filter(topic -> topic.contains("-cop") || topic.contains("gpte") || topic.contains("validated-content")).findFirst().orElse(null);
        this.contributorCount = contributors == null ? 0 : contributors.size();
        this.commitCount = commits == null ? 0 : commits.size();
        this.openIssueCount = issues == null ? 0 : issues.size();
        this.openPullRequestCount = pullRequests == null ? 0 : pullRequests.size();
        this.topics = topics == null ? new ArrayList<>() : topics;
        this.clonesInPast14Days = cloneTraffic == null ? 0 : cloneTraffic.getUniques();
        this.viewsInPast14Days = viewTraffic == null ? 0 : viewTraffic.getUniques();
        this.hasOwners = hasOwners;
        this.hasCodeOwners = hasCodeOwners;
        this.hasWorkflows = hasWorkflows;
        this.hasTravis = hasTravis;
        this.hasRenovate = hasRenovate;
        this.inConfig = inConfig;
        this.isArchived = isArchived;
        this.inArchivedTeam = inArchivedTeam;
    }

    public List<String> toArray() {
        return Arrays.asList(repoName, cop, lastCommitDate, lastCommitAuthor,
                String.valueOf(contributorCount), String.valueOf(commitCount), String.valueOf(openIssueCount),
                String.valueOf(openPullRequestCount), String.join(",", topics),
                String.valueOf(clonesInPast14Days), String.valueOf(viewsInPast14Days),
                String.valueOf(hasOwners), String.valueOf(hasCodeOwners),
                String.valueOf(hasWorkflows), String.valueOf(hasTravis),
                String.valueOf(hasRenovate),
                String.valueOf(inConfig), String.valueOf(isArchived), String.valueOf(inArchivedTeam));
    }
}
