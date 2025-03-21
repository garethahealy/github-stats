package com.garethahealy.githubstats.rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class QuayUsersRestClientTest {

    @RestClient
    QuayUsersRestClient quayUsersRestClient;

    @Test
    void getUser() {
        RestResponse<String> resp = quayUsersRestClient.getUser("garethahealy");

        assertNotNull(resp);
        assertEquals(Response.Status.OK, resp.getStatusInfo().toEnum());
        assertTrue(resp.getEntity().contains("garethahealy"));
    }

    @Test
    void handlesUserNotExists() {
        assertThrows(WebApplicationException.class, () -> {
            quayUsersRestClient.getUser("does-not-exist");
        });
    }
}
