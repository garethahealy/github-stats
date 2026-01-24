package com.garethahealy.githubstats.model.stats;

import org.kohsuke.github.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Repository(String repoName, String lastCommitAuthor, String lastCommitDate, String cop,
        int contributorCount, int commitCount, int openIssueCount, int openPullRequestCount,
        List<String> topics, long clonesInPast14Days, long viewsInPast14Days, boolean hasOwners,
        boolean hasCodeOwners, boolean hasWorkflows, boolean hasTravis, boolean hasRenovate,
        boolean inConfig, boolean isArchived, boolean inArchivedTeam) {

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

    public static Repository from(String repoName,
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
        GHCommit lastCommit = commits != null && !commits.isEmpty() ? commits.getFirst() : null;

        String t_lastCommitAuthor = lastCommit == null || lastCommit.getAuthor() == null ? null : lastCommit.getAuthor().getLogin();
        String t_lastCommitDate = lastCommit == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.ofInstant(lastCommit.getCommitDate().toInstant(), ZoneId.systemDefault()));
        String t_cop = topics == null ? null : topics.stream().filter(topic -> topic.contains("-cop") || topic.contains("gpte") || topic.contains("validated-content")).findFirst().orElse(null);
        int t_contributorCount = contributors == null ? 0 : contributors.size();
        int t_commitCount = commits == null ? 0 : commits.size();
        int t_openIssueCount = issues == null ? 0 : issues.size();
        int t_openPullRequestCount = pullRequests == null ? 0 : pullRequests.size();
        List<String> t_topics = topics == null ? new ArrayList<>() : topics;
        long t_clonesInPast14Days = cloneTraffic == null ? 0 : cloneTraffic.getUniques();
        long t_viewsInPast14Days = viewTraffic == null ? 0 : viewTraffic.getUniques();

        return new Repository(repoName, t_lastCommitAuthor, t_lastCommitDate, t_cop, t_contributorCount, t_commitCount, t_openIssueCount,
            t_openPullRequestCount, t_topics, t_clonesInPast14Days, t_viewsInPast14Days, hasOwners, hasCodeOwners, hasWorkflows,
            hasTravis, hasRenovate, inConfig, isArchived, inArchivedTeam);
    }

    public List<String> toArray() {
        String repoLink = "=HYPERLINK(\"https://www.github.com/redhat-cop/" + repoName + "\",\"" + repoName + "\")";
        return Arrays.asList(repoLink, cop, lastCommitDate, lastCommitAuthor,
            String.valueOf(contributorCount), String.valueOf(commitCount), String.valueOf(openIssueCount),
            String.valueOf(openPullRequestCount), String.join(",", topics),
            String.valueOf(clonesInPast14Days), String.valueOf(viewsInPast14Days),
            String.valueOf(hasOwners), String.valueOf(hasCodeOwners),
            String.valueOf(hasWorkflows), String.valueOf(hasTravis),
            String.valueOf(hasRenovate),
            String.valueOf(inConfig), String.valueOf(isArchived), String.valueOf(inArchivedTeam));
    }
}
