package com.garethahealy.githubstats.clients;

import com.garethahealy.githubstats.clients.rest.GitHubDiffRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;

@ApplicationScoped
public class GitHubDiffService {

    @Inject
    Logger logger;

    @RestClient
    GitHubDiffRestClient gitHubDiffRestClient;

    public String getDiff(GHPullRequest pr) {
        String answer = null;

        try {
            answer = getDiff(pr.getRepository().getOwner().getLogin(), pr.getRepository().getName(), pr.getNumber());
        } catch (IOException ex) {
            logger.error(ex);
        }

        return answer;
    }

    public String getDiff(String org, String repo, int number) {
        String answer = null;

        try {
            RestResponse<String> diff = gitHubDiffRestClient.getDiff(org, repo, number);
            answer = diff.getEntity();
        } catch (ClientWebApplicationException ex) {
            logger.error(ex);
        }

        return answer;
    }
}
