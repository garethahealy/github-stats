package com.garethahealy.githubstats.rest.client;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CreateWhoAreYouIssueServiceTest {

    @Test
    public void canRun() throws IOException {
        CreateWhoAreYouIssueService service = new CreateWhoAreYouIssueService();
        service.run();

        Assertions.assertTrue(true);
    }
}