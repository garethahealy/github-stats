package com.garethahealy.githubstats.services.users.utils;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.rest.QuayUsersRestClient;
import com.garethahealy.githubstats.services.github.GitHubClient;
import com.garethahealy.githubstats.utils.OrgMemberMockData;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

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
}
