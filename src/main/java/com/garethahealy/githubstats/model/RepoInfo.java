package com.garethahealy.githubstats.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;

public class RepoInfo {

    public enum Headers {
        RepoName,
        LastCommitAuthor,
        LastCommitDate,
        CoP,
        ContributorNames,
        Topics,
        ClonesInPast14Days,
        ViewsInPast14Days
    }

    private final String repoName;
    private final String lastCommitAuthor;
    private final String lastCommitDate;
    private final String cop;
    private final List<String> contributorNames;
    private final List<String> topics;
    private final long clonesInPast14Days;
    private final long viewsInPast14Days;

    public RepoInfo(String repoName,
                    GHCommit lastCommit,
                    List<GHRepository.Contributor> contributors,
                    List<String> topics,
                    GHRepositoryCloneTraffic cloneTraffic,
                    GHRepositoryViewTraffic viewTraffic) throws IOException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        this.repoName = repoName;
        this.lastCommitAuthor = lastCommit == null || lastCommit.getAuthor() == null ? null : lastCommit.getAuthor().getLogin();
        this.lastCommitDate = lastCommit == null ? null : df.format(lastCommit.getCommitDate());
        this.cop = topics.stream().filter(topic -> topic.contains("-cop")).findFirst().orElse(null);
        this.contributorNames = contributors.stream().map(GHPerson::getLogin).collect(Collectors.toList());
        this.topics = topics;
        this.clonesInPast14Days = cloneTraffic == null ? 0 : cloneTraffic.getUniques();
        this.viewsInPast14Days = viewTraffic == null ? 0 : viewTraffic.getUniques();
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

    public List<String> getContributorNames() {
        return contributorNames;
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

    public List<String> toArray() {
        return Arrays.asList(getRepoName(), getLastCommitDate(), getLastCommitAuthor(), getCop(),
                String.join(",", getContributorNames()), String.join(",", getTopics()),
                String.valueOf(getClonesInPast14Days()), String.valueOf(getViewsInPast14Days()));
    }
}
