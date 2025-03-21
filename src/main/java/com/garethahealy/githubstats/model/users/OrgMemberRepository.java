package com.garethahealy.githubstats.model.users;

import com.garethahealy.githubstats.predicates.OrgMemberFilters;
import com.garethahealy.githubstats.rest.QuayUsersRestClient;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class OrgMemberRepository {

    private final Map<String, OrgMember> members;
    private final File input;
    private final GitHub github;
    private final QuayUsersRestClient quayUsersRestClient;

    public OrgMemberRepository(File input, Map<String, OrgMember> members) {
        this(input, members, null, null);
    }

    public OrgMemberRepository(File input, Map<String, OrgMember> members, GitHub github, QuayUsersRestClient quayUsersRestClient) {
        this.input = input;
        this.members = members;
        this.github = github;
        this.quayUsersRestClient = quayUsersRestClient;
    }

    public String name() {
        return input.getName();
    }

    public Path path() {
        return input.toPath();
    }

    public List<OrgMember> items() {
        return new ArrayList<>(members.values());
    }

    public int size() {
        return members.size();
    }

    public OrgMember put(OrgMember add) {
        return members.put(add.gitHubUsername(), add);
    }

    public void remove(List<OrgMember> remove) {
        for (OrgMember current : remove) {
            remove(current);
        }
    }

    public void remove(OrgMember remove) {
        members.remove(remove.gitHubUsername());
    }

    public void remove(String remove) {
        if (containsKey(remove)) {
            remove(members.get(remove));
        }
    }

    public void remove(LocalDate date) {
        List<OrgMember> remove = members.values().stream().filter(OrgMemberFilters.deleteAfter(date)).toList();
        remove(remove);
    }

    public void replace(List<OrgMember> replace) {
        for (OrgMember current : replace) {
            members.replace(current.gitHubUsername(), current);
        }
    }

    public boolean containsKey(String key) {
        return members.containsKey(key);
    }

    public List<OrgMember> filter(Predicate<OrgMember> predicate) {
        return members.values().stream().filter(predicate).toList();
    }

    /**
     * Validate the GitHub and Quay usernames are valid against github.com and quay.io
     *
     * @param expectedGitHubLogin
     * @throws IOException
     * @throws URISyntaxException
     */
    public void validate(OrgMember member, String expectedGitHubLogin) throws IOException, URISyntaxException {
        if (member.gitHubUsername() == null || member.gitHubUsername().isEmpty()) {
            throw new IllegalStateException("GitHubUsername is null or empty. Should never happen!");
        }

        if (!expectedGitHubLogin.equalsIgnoreCase(member.gitHubUsername())) {
            throw new IllegalStateException("Expected Github " + expectedGitHubLogin + " but have " + member.gitHubUsername());
        }

        if (!member.linkedGitHubUsernames().isEmpty()) {
            for (String githubUsername : member.linkedGitHubUsernames()) {
                GHUser user = github.getUser(githubUsername);
                if (user == null) {
                    throw new IllegalStateException("Expected GitHub " + githubUsername + " but couldn't find");
                }
            }
        }

        if (!member.linkedQuayUsernames().isEmpty()) {
            for (String quayUsername : member.linkedQuayUsernames()) {
                RestResponse<String> response = quayUsersRestClient.getUser(quayUsername);
                if (response.getStatusInfo().toEnum() != Response.Status.OK) {
                    throw new IllegalStateException("Expected Quay " + quayUsername + " but could not find @ " + response.getLocation() + " - ResponseCode: " + response.getStatus());
                }
            }
        }
    }

    /**
     * Switch the keys around for the map to work against quay IDs
     *
     * @return
     */
    public OrgMemberRepository convertToQuay() {
        Map<String, OrgMember> quayBasedMembers = new HashMap<>();
        for (Map.Entry<String, OrgMember> entry : members.entrySet()) {
            if (!entry.getValue().linkedQuayUsernames().isEmpty()) {
                for (String quayUsername : entry.getValue().linkedQuayUsernames()) {
                    quayBasedMembers.put(quayUsername, entry.getValue());
                }
            }
        }

        return new OrgMemberRepository(input, quayBasedMembers, github, quayUsersRestClient);
    }
}
