package com.garethahealy.githubstats.rest.client;

import java.io.IOException;
import java.util.List;

import com.garethahealy.githubstats.model.RepoInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CollectStatsServiceTest {

    private static final Logger logger = LogManager.getLogger(CollectStatsServiceTest.class);

    @Test
    public void canRun() throws IOException {
        logger.info("Running.");

        CollectStatsService service = new CollectStatsService();
        List<RepoInfo> answer = service.run();

        Assertions.assertNotNull(answer);
        Assertions.assertTrue(answer.size() > 0);
    }
}