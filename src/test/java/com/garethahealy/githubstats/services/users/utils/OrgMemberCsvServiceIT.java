package com.garethahealy.githubstats.services.users.utils;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.rest.QuayUsersRestClient;
import com.garethahealy.githubstats.services.github.GitHubClient;
import com.garethahealy.githubstats.utils.OrgMemberMockData;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class OrgMemberCsvServiceIT {

    @RestClient
    QuayUsersRestClient quayUsersRestClient;

    @Test
    void validate() throws IOException, URISyntaxException {
        GitHubClient client = new GitHubClient();

        OrgMemberRepository answer = new OrgMemberRepository(null, null, client.getClient(), quayUsersRestClient);
        answer.validate(OrgMemberMockData.getOrgMembers().getFirst(), "garethahealy");
    }

    @Test
    void parseLocalLdapMembers() throws IOException {
        File input = new File("ldap-members.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), null);
        OrgMemberRepository answer = orgMemberCsvService.parse(input);

        assertNotNull(answer);
        assertTrue(answer.size() > 200);
    }

    @Test
    void parseLocalSupplementary() throws IOException {
        File input = new File("supplementary.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), null);
        OrgMemberRepository answer = orgMemberCsvService.parse(input);

        assertNotNull(answer);
        assertEquals(21, answer.size());
    }
}
