package com.garethahealy.githubstats.model.users;

import com.garethahealy.githubstats.predicates.OrgMemberFilters;
import com.garethahealy.githubstats.services.github.GitHubOrganizationLookupService;
import com.garethahealy.githubstats.services.quay.QuayUserService;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class OrgMemberRepository {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OrgMemberRepository.class);
    private final Logger logger = Logger.getLogger(OrgMemberRepository.class);
    private final Map<String, OrgMember> members;
    private final File input;
    private final GitHubOrganizationLookupService gitHubOrganizationLookupService;
    private final QuayUserService quayUserService;

    public OrgMemberRepository(File input, Map<String, OrgMember> members) {
        this(input, members, null, null);
    }

    public OrgMemberRepository(File input, Map<String, OrgMember> members, GitHubOrganizationLookupService gitHubOrganizationLookupService, QuayUserService quayUserService) {
        this.input = input;
        this.members = members;
        this.gitHubOrganizationLookupService = gitHubOrganizationLookupService;
        this.quayUserService = quayUserService;
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

    public OrgMember put(OrgMember add) throws IOException {
        validate(add);

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

    public void replace(List<OrgMember> replace) throws IOException {
        for (OrgMember current : replace) {
            validate(current);

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
     * @throws IOException
     */
    public void validate(OrgMember member) throws IOException {
        if (gitHubOrganizationLookupService == null || quayUserService == null) {
            logger.warn("gitHubOrganizationLookupService == null || quayUserService == null, not validating");
            return;
        }

        if (member.gitHubUsername() == null || member.gitHubUsername().isEmpty()) {
            throw new IllegalStateException("GitHubUsername is null or empty. Should never happen!");
        }

        if (!member.linkedGitHubUsernames().isEmpty()) {
            validateLinkedGitHubUsernames(member);
        }

        if (!member.linkedQuayUsernames().isEmpty()) {
            validateLinkedQuayUsernames(member);
        }
    }

    private void validateLinkedGitHubUsernames(OrgMember member) throws IOException {
        List<String> remove = new ArrayList<>();

        for (String githubUsername : member.linkedGitHubUsernames()) {
            String userValue = githubUsername;
            if (githubUsername.contains("/")) {
                userValue = githubUsername.split("/")[0];
            }

            GHUser user = gitHubOrganizationLookupService.getUser(userValue);
            if (user == null) {
                logger.warnf("%s was not found via the GitHub API, removing", githubUsername);

                remove.add(githubUsername);
            } else if (!user.getType().equalsIgnoreCase("User")) {
                logger.warnf("%s is not a `User`, its a %s, removing", githubUsername, user.getType());

                remove.add(githubUsername);
            } else {
                if (githubUsername.contains("/")) {
                    GHRepository repo = gitHubOrganizationLookupService.getRepository(githubUsername);
                    if (repo == null) {
                        throw new IllegalStateException("Expected GitHub Username but got '" + githubUsername + "' - Not sure what it is...");
                    } else {
                        logger.warnf("%s is a repository, removing", githubUsername);

                        remove.add(githubUsername);
                    }
                }
            }
        }

        if (!remove.isEmpty()) {
            logger.infof("-> Removing %s from linkedGitHubUsernames for %s", remove.size(), member.gitHubUsername());

            member.linkedGitHubUsernames().removeAll(remove);
        }
    }

    private void validateLinkedQuayUsernames(OrgMember member) {
        List<String> remove = new ArrayList<>();

        for (String quayUsername : member.linkedQuayUsernames()) {
            RestResponse<String> response = quayUserService.getUser(quayUsername);
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                logger.warnf("%s was not found via the Quay API, received code %s, removing", quayUsername, response.getStatus());

                remove.add(quayUsername);
            }
        }

        if (!remove.isEmpty()) {
            logger.infof("-> Removing %s from linkedQuayUsernames for %s", remove.size(), member.gitHubUsername());

            member.linkedQuayUsernames().removeAll(remove);
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

        return new OrgMemberRepository(input, quayBasedMembers, gitHubOrganizationLookupService, quayUserService);
    }
}
