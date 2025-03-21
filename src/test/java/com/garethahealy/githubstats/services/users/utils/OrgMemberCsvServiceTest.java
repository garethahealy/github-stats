package com.garethahealy.githubstats.services.users.utils;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.utils.OrgMemberMockData;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class OrgMemberCsvServiceTest {

    @Test
    void parseSample() throws IOException {
        File input = new File(this.getClass().getClassLoader().getResource("sample-ldap-members.csv").getFile());

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), null);
        OrgMemberRepository answer = orgMemberCsvService.parse(input);

        assertNotNull(answer);
        assertEquals(3, answer.size());
    }

    @Test
    void writeSample() throws IOException {
        File output = new File("target/OrgMemberCsvServiceTest/sample-ldap-members.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), null);
        orgMemberCsvService.write(new OrgMemberRepository(output, OrgMemberMockData.getOrgMembersMap()));
    }

    @Test
    void writeHandlesEmptyMembers() throws IOException {
        File output = new File("target/OrgMemberCsvServiceTest/empty-ldap-members.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), null);
        orgMemberCsvService.write(new OrgMemberRepository(output, new HashMap<>()));
    }
}
