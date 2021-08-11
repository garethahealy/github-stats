package com.garethahealy.githubstats.rest.client;

import java.io.IOException;
import java.util.List;

import com.garethahealy.githubstats.model.RepoInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GitHubServiceTest {

    @Test
    public void canRun() throws IOException {
        GitHubService service = new GitHubService();
        List<RepoInfo> answer = service.run();

        Assertions.assertNotNull(answer);
        Assertions.assertTrue(answer.size() > 0);
    }
}