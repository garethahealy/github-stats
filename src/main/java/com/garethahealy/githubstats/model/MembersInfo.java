package com.garethahealy.githubstats.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.List;

@RegisterForReflection
public class MembersInfo {
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

    public MembersInfo(String timestamp, String emailAddress, String whatIsYourGitHubUsername) {
        this.timestamp = timestamp;
        this.emailAddress = emailAddress;
        this.whatIsYourGitHubUsername = whatIsYourGitHubUsername;
    }

    public List<String> toArray() {
        return Arrays.asList(timestamp, emailAddress, whatIsYourGitHubUsername);
    }
}
