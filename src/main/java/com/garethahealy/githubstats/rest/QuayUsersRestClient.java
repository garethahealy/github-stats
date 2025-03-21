package com.garethahealy.githubstats.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/v1/users")
@RegisterRestClient
public interface QuayUsersRestClient {

    @GET
    @Path("/{user}")
    RestResponse<String> getUser(@PathParam("user") String user);
}
