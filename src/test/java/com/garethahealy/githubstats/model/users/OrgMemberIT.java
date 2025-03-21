package com.garethahealy.githubstats.model.users;

import com.garethahealy.githubstats.services.ldap.LdapSearchService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrgMemberIT {

    @Test
    void validate() {
        Map<String, List<String>> entries = new HashMap<>();
        entries.put(LdapSearchService.AttributeKeys.PrimaryMail, List.of("gahealy@redhat.com"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLGitHub, List.of("https://github.com/garethahealy"));
        entries.put(LdapSearchService.AttributeKeys.SocialURLQuay, List.of("https://quay.io/garethahealy"));

        OrgMember member = OrgMember.from("garethahealy", entries);

        assertNotNull(member.gitHubUsername());
        assertNotNull(member.linkedQuayUsernames());
        assertFalse(member.linkedQuayUsernames().isEmpty());
        assertEquals("garethahealy", member.gitHubUsername());
        assertEquals("garethahealy", member.linkedQuayUsernames().getFirst());
    }
}
