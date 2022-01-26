package com.garethahealy.githubstats.rest.client;

import java.io.IOException;
import java.util.List;

import com.garethahealy.githubstats.model.RepoInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CollectStatsServiceTest {

    @Test
    public void canRun() throws IOException {
        CollectStatsService service = new CollectStatsService();
        List<RepoInfo> answer = service.run();

        Assertions.assertNotNull(answer);
        Assertions.assertTrue(answer.size() > 0);
    }
}