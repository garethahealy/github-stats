package com.garethahealy.githubstats.services.ldap;

import com.garethahealy.githubstats.utils.OrgMemberMockData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
class DefaultLdapGuessServiceIT {

    @Inject
    DefaultLdapGuessService defaultLdapGuessService;

    @Test
    void attempt() throws IOException, LdapException {
        defaultLdapGuessService.attempt(OrgMemberMockData.getMe(), true);
    }
}
