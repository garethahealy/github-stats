package com.garethahealy.githubstats.services.quay;

import com.garethahealy.githubstats.clients.rest.QuayUsersRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class QuayUserService {

    @Inject
    Logger logger;

    @RestClient
    QuayUsersRestClient quayUsersRestClient;

    public RestResponse<String> getUser(String user) {
        RestResponse<String> answer;
        try {
            answer = quayUsersRestClient.getUser(user);
        } catch (ClientWebApplicationException ex) {
            logger.error(ex);

            answer = RestResponse.ResponseBuilder.create(ex.getResponse().getStatusInfo().toEnum(), "")
                .location(ex.getResponse().getLocation())
                .build();
        }

        return answer;
    }
}
