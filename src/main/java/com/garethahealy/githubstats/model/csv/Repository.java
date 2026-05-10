package com.garethahealy.githubstats.model.csv;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.List;

@RegisterForReflection
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

    public List<String> toArray() {
        String repoLink = "=HYPERLINK(\"https://www.github.com/redhat-cop/" + repoName + "\",\"" + repoName + "\")";
        boolean archived = isArchived;
        return Arrays.asList(repoLink, cop, lastCommitDate, lastCommitAuthor,
            intCsv(contributorCount, archived), intCsv(commitCount, archived), intCsv(openIssueCount, archived),
            intCsv(openPullRequestCount, archived), String.join(",", topics == null ? List.of() : topics),
            longCsv(clonesInPast14Days, archived), longCsv(viewsInPast14Days, archived),
            boolCsv(hasOwners, archived), boolCsv(hasCodeOwners, archived),
            boolCsv(hasWorkflows, archived), boolCsv(hasTravis, archived),
            boolCsv(hasRenovate, archived),
            String.valueOf(inConfig), String.valueOf(isArchived), String.valueOf(inArchivedTeam));
    }

    private String intCsv(int value, boolean isArchived) {
        return isArchived ? "" : String.valueOf(value);
    }

    private String longCsv(long value, boolean isArchived) {
        return isArchived ? "" : String.valueOf(value);
    }

    private String boolCsv(boolean value, boolean isArchived) {
        return isArchived ? "" : String.valueOf(value);
    }
}
