package com.garethahealy.githubstats.model.csv;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Arrays;
import java.util.List;

@RegisterForReflection
public class Members implements Comparable<Members> {

    public enum Headers {
        Timestamp,
        EmailAddress,
        WhatIsYourGitHubUsername
    }

    private final String timestamp;
    private final String emailAddress;
    private final String whatIsYourGitHubUsername;
    private String redHatUserId;

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getRedHatUserId() {
        if (redHatUserId == null || redHatUserId.isEmpty()) {
            redHatUserId = emailAddress.split("@")[0];
        }

        return redHatUserId;
    }

    public String getWhatIsYourGitHubUsername() {
        return whatIsYourGitHubUsername;
    }

    public Members(String timestamp, String emailAddress, String whatIsYourGitHubUsername) {
        this.timestamp = timestamp;
        this.emailAddress = emailAddress;
        this.whatIsYourGitHubUsername = whatIsYourGitHubUsername;
    }

    public List<String> toArray() {
        return Arrays.asList(timestamp, emailAddress, whatIsYourGitHubUsername);
    }

    @Override
    public int compareTo(Members o) {
        return new CompareToBuilder().append(getRedHatUserId(), o.getRedHatUserId()).toComparison();
    }
}
