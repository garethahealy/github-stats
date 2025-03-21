package com.garethahealy.githubstats.model.users;

import com.garethahealy.githubstats.services.ldap.LdapSearchService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class OrgMemberTest {

    @Test
    void handlesGithubDoubleHttps() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLGitHub, List.of("https://https://github.com/garethahealy"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.gitHubUsername());
        assertEquals("garethahealy", member.gitHubUsername());
    }

    @Test
    void handlesGithubOrg() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLGitHub, List.of("https://github.com/redhat-gpe/agnosticv"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.linkedGitHubUsernames());
        assertTrue(member.linkedGitHubUsernames().isEmpty());
    }

    @Test
    void handlesGithubOrgEndingSlash() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLGitHub, List.of("https://github.com/redhat-gpe/agnosticv/"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.linkedGitHubUsernames());
        assertTrue(member.linkedGitHubUsernames().isEmpty());
    }

    @Test
    void handlesQuayUserUrl() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLQuay, List.of("https://quay.io/user/garethahealy"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.linkedQuayUsernames());
        assertFalse(member.linkedQuayUsernames().isEmpty());
        assertEquals("garethahealy", member.linkedQuayUsernames().getFirst());
    }

    @Test
    void handlesQuayRepository() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLQuay, List.of("https://quay.io/repository/garethahealy"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.linkedQuayUsernames());
        assertFalse(member.linkedQuayUsernames().isEmpty());
        assertEquals("garethahealy", member.linkedQuayUsernames().getFirst());
    }

    @Test
    void handlesQuay() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLQuay, List.of("https://quay.io/garethahealy"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.linkedQuayUsernames());
        assertFalse(member.linkedQuayUsernames().isEmpty());
        assertEquals("garethahealy", member.linkedQuayUsernames().getFirst());
    }
}
