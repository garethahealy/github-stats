package com.garethahealy.githubstats.services.ldap;

import com.garethahealy.githubstats.BaseRequiresLdapConnection;
import com.garethahealy.githubstats.model.users.OrgMember;
import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.services.github.GitHubOrganizationLookupService;
import com.garethahealy.githubstats.services.quay.QuayUserService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LdapSearchServiceIT extends BaseRequiresLdapConnection {

    @Inject
    LdapSearchService ldapSearchService;

    @Inject
    GitHubOrganizationLookupService gitHubOrganizationLookupService;

    @Inject
    QuayUserService quayUserService;

    @Test
    @EnabledIf("canConnectVpn")
    void canConnect() {
        assertTrue(ldapSearchService.canConnect());
    }

    @Test
    @EnabledIf("canConnectVpn")
    void searchOnUser() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            assertTrue(ldapSearchService.searchOnUser(connection, "gahealy"));
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void searchOnName() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnName(connection, "Gareth Healy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void searchOnGitHubLogin() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnGitHubLogin(connection, "gahealy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void searchOnQuaySocial() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnQuaySocial(connection, "garethahealy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void searchOnPrimaryMail() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnPrimaryMail(connection, "gahealy@redhat.com");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void retrieve() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            OrgMember answer = ldapSearchService.retrieve(connection, "garethahealy", "gahealy@redhat.com");

            assertNotNull(answer);

            assertEquals("gahealy@redhat.com", answer.redhatEmailAddress());
            assertEquals("garethahealy", answer.gitHubUsername());
            assertEquals("garethahealy", answer.linkedGitHubUsernames().getFirst());
            assertEquals("garethahealy", answer.linkedQuayUsernames().getFirst());
        }
    }


    @Test
    @EnabledIf("canConnectVpn")
    void retrieveAndValidateSsulliva() throws IOException, LdapException {
        OrgMemberRepository orgMemberRepository = new OrgMemberRepository(null, null, gitHubOrganizationLookupService, quayUserService);

        try (LdapConnection connection = ldapSearchService.open()) {
            OrgMember answer = ldapSearchService.retrieve(connection, "sean-m-sullivan", "ssulliva@redhat.com");
            assertNotNull(answer);

            orgMemberRepository.validate(answer);

            assertEquals("ssulliva@redhat.com", answer.redhatEmailAddress());
            assertEquals("sean-m-sullivan", answer.gitHubUsername());
            assertFalse(answer.linkedGitHubUsernames().isEmpty());
            assertTrue(answer.linkedQuayUsernames().isEmpty());
            assertEquals(1, answer.linkedGitHubUsernames().size());
            assertEquals("sean-m-sullivan", answer.linkedGitHubUsernames().getFirst());
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void retrieveAndValidateClaudiol() throws IOException, LdapException {
        OrgMemberRepository orgMemberRepository = new OrgMemberRepository(null, null, gitHubOrganizationLookupService, quayUserService);

        try (LdapConnection connection = ldapSearchService.open()) {
            OrgMember answer = ldapSearchService.retrieve(connection, "claudiol", "claudiol@redhat.com");
            assertNotNull(answer);

            orgMemberRepository.validate(answer);

            assertEquals("claudiol@redhat.com", answer.redhatEmailAddress());
            assertEquals("claudiol", answer.gitHubUsername());
            assertFalse(answer.linkedGitHubUsernames().isEmpty());
            assertTrue(answer.linkedQuayUsernames().isEmpty());
            assertEquals(1, answer.linkedGitHubUsernames().size());
            assertEquals("claudiol", answer.linkedGitHubUsernames().getFirst());
        }
    }

    @Test
    @EnabledIf("canConnectVpn")
    void retrieveAndValidateAblock() throws IOException, LdapException {
        OrgMemberRepository orgMemberRepository = new OrgMemberRepository(null, null, gitHubOrganizationLookupService, quayUserService);

        try (LdapConnection connection = ldapSearchService.open()) {
            OrgMember answer = ldapSearchService.retrieve(connection, "sabre1041", "ablock@redhat.com");
            assertNotNull(answer);

            orgMemberRepository.validate(answer);

            assertEquals("ablock@redhat.com", answer.redhatEmailAddress());
            assertEquals("sabre1041", answer.gitHubUsername());
            assertFalse(answer.linkedGitHubUsernames().isEmpty());
            assertFalse(answer.linkedQuayUsernames().isEmpty());
            assertEquals(1, answer.linkedGitHubUsernames().size());
            assertEquals(1, answer.linkedQuayUsernames().size());
            assertEquals("sabre1041", answer.linkedGitHubUsernames().getFirst());
            assertEquals("ablock", answer.linkedQuayUsernames().getFirst());
        }
    }
}
