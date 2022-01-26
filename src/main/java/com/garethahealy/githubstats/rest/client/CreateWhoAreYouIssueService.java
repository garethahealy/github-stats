package com.garethahealy.githubstats.rest.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.garethahealy.githubstats.model.RepoInfo;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

public class CreateWhoAreYouIssueService {

    private static final Logger logger = LogManager.getLogger(CreateWhoAreYouIssueService.class);

    public void run() throws IOException {
        logger.info("Starting...");

        List<RepoInfo> answer = new ArrayList<>();

        Cache cache = new Cache(new File("/tmp/github-okhttp"), 10 * 1024 * 1024); // 10MB cache
        GitHub gitHub = GitHubBuilder.fromEnvironment()
                .withConnector(new OkHttpConnector(new OkHttpClient.Builder().cache(cache).build()))
                .build();

        logger.info("Connector with cache created.");

        GHOrganization org = gitHub.getOrganization("redhat-cop");
        GHRepository orgRepo = org.getRepository("org");
        List<GHUser> members = org.listMembers().toList();

        logger.info("There are {} members", members.size());

        for (GHUser current : members) {
            if (current.getLogin().equalsIgnoreCase("garethahealy")) {
                orgRepo.createIssue("@" + current.getLogin() + " please complete form")
                        .assignee(current)
                        .label("admin")
                        .body("To be a member of the Red Hat CoP GitHub org, you are required to be a Red Hat employee. " +
                                "Non-employees are invited to be outside-collaborators (https://github.com/orgs/redhat-cop/outside-collaborators). " +
                                "As we currently do not know who is an employee and who is not, we are requiring all members to submit the following google form " +
                                "so that we can verify who are employees: " +
                                "https://red.ht/github-redhat-cop-username")
                        .create();

                logger.info("Created issue for {}", current.getLogin());
            }
        }
    }
}
