package com.garethahealy.githubstats.services.users.utils;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OrgMemberCsvServiceIT {

    @Test
    void parseLocalLdapMembers() throws IOException {
        File input = new File("ldap-members.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService();
        OrgMemberRepository answer = orgMemberCsvService.parse(input);

        assertNotNull(answer);
        assertTrue(answer.size() > 200);
    }

    @Test
    void parseLocalSupplementary() throws IOException {
        File input = new File("supplementary.csv");

        OrgMemberCsvService orgMemberCsvService = new OrgMemberCsvService();
        OrgMemberRepository answer = orgMemberCsvService.parse(input);

        assertNotNull(answer);
        assertEquals(20, answer.size());
    }
}
