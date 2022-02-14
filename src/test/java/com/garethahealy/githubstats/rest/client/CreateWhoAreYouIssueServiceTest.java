package com.garethahealy.githubstats.rest.client;

import java.io.IOException;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CreateWhoAreYouIssueServiceTest {

    private static final Logger logger = LogManager.getLogger(CreateWhoAreYouIssueServiceTest.class);

    @Test
    public void canRun() throws IOException, LdapException {
        logger.info("Running.");

        CreateWhoAreYouIssueService service = new CreateWhoAreYouIssueService();
        service.run();

        Assertions.assertTrue(true);
    }
}