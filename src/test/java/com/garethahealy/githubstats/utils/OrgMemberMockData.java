package com.garethahealy.githubstats.utils;

import com.garethahealy.githubstats.model.users.BasicGHUser;
import com.garethahealy.githubstats.model.users.OrgMember;
import com.garethahealy.githubstats.services.github.GitHubClient;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgMemberMockData {

    public static List<OrgMember> getOrgMembers() {
        OrgMember one = new OrgMember("gahealy@redhat.com", "garethahealy", List.of("garethahealy"), List.of("garethahealy"), OrgMember.Source.Manual, null, null);
        OrgMember two = new OrgMember("ablock@redhat.com", "sabre1041", List.of("sabre1041"), List.of("ablock"), OrgMember.Source.Automated, null, null);
        OrgMember three = new OrgMember("esauer@redhat.com", "etsauer", List.of("etsauer"), List.of("etsauer"), OrgMember.Source.GoogleSheet, null, null);

        return List.of(one, two, three);
    }

    public static Map<String, OrgMember> getOrgMembersMap() {
        Map<String, OrgMember> answer = new HashMap<>();
        for (OrgMember current : getOrgMembers()) {
            answer.put(current.gitHubUsername(), current);
        }

        return answer;
    }

    public static OrgMember getMe() throws IOException {
        GitHubClient client = new GitHubClient();
        GHUser me = client.getClient().getUser("garethahealy");
        return new OrgMember("gahealy@redhat.com", "garethahealy", List.of("garethahealy"), List.of("garethahealy"), OrgMember.Source.Manual, null, BasicGHUser.from(me));
    }
}
