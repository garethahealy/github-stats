package com.garethahealy.githubstats.services.ldap;

import com.garethahealy.githubstats.model.users.OrgMember;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LdapSearchServiceIT {

    @Inject
    LdapSearchService ldapSearchService;

    @Test
    void canConnect() {
        assertTrue(ldapSearchService.canConnect());
    }

    @Test
    void searchOnUser() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            assertTrue(ldapSearchService.searchOnUser(connection, "gahealy"));
        }
    }

    @Test
    void searchOnName() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnName(connection, "Gareth Healy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    void searchOnGitHubLogin() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnGitHubLogin(connection, "gahealy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    void searchOnQuaySocial() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnQuaySocial(connection, "garethahealy");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    void searchOnPrimaryMail() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            String answer = ldapSearchService.searchOnPrimaryMail(connection, "gahealy@redhat.com");

            assertNotNull(answer);
            assertEquals("gahealy@redhat.com", answer);
        }
    }

    @Test
    void retrieve() throws IOException, LdapException {
        try (LdapConnection connection = ldapSearchService.open()) {
            OrgMember answer = ldapSearchService.retrieve(connection, "garethahealy", "gahealy@redhat.com");

            assertNotNull(answer);

            assertEquals("gahealy@redhat.com", answer.redhatEmailAddress());
            assertEquals("garethahealy", answer.gitHubUsername());
            assertEquals("garethahealy", answer.linkedQuayUsernames().getFirst());
        }
    }
}
