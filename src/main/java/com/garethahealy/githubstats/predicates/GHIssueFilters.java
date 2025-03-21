package com.garethahealy.githubstats.predicates;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHPermissionType;

import java.util.function.Predicate;

public class GHIssueFilters {

    /**
     * Is the issue a request to link their GitHub to rover created by this tool
     *
     * @param permissions
     * @return
     */
    public static Predicate<GHIssue> isLinkUsersWith(GHPermissionType permissions) {
        return issue -> !issue.isPullRequest() && issue.getTitle().equalsIgnoreCase("Request GitHub to Red Hat ID linkage for users with " + permissions);
    }

    /**
     * Is the issue a request to remove people who have left Red Hat created by this tool
     *
     * @return
     */
    public static Predicate<GHIssue> isRemoveNonRH() {
        return issue -> !issue.isPullRequest() && issue.getTitle().equalsIgnoreCase("Remove users - Not in RH LDAP");
    }
}
