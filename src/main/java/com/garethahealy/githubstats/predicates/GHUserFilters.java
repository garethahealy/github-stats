package com.garethahealy.githubstats.predicates;

import com.garethahealy.githubstats.model.users.OrgMember;
import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import org.kohsuke.github.GHUser;

import java.util.function.Predicate;

public class GHUserFilters {

    /**
     * Filter the GitHub user to this member
     *
     * @param member
     * @return
     */
    public static Predicate<GHUser> equals(OrgMember member) {
        return user -> user.getLogin().equalsIgnoreCase(member.gitHubUsername());
    }

    /**
     * Filter GitHub users who we don't know about
     *
     * @param ldapMembers
     * @param supplementaryMembers
     * @return
     */
    public static Predicate<GHUser> notContains(OrgMemberRepository ldapMembers, OrgMemberRepository supplementaryMembers) {
        return user -> !ldapMembers.containsKey(user.getLogin()) && !supplementaryMembers.containsKey(user.getLogin());
    }
}
