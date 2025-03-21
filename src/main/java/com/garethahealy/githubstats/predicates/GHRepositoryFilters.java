package com.garethahealy.githubstats.predicates;

import org.kohsuke.github.GHPermissionType;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.function.Predicate;

public class GHRepositoryFilters {

    /**
     * Does this member have the permissions on this repository?
     *
     * @param member
     * @param perms
     * @return
     */
    public static Predicate<GHRepository> hasPermission(GHUser member, GHPermissionType perms) {
        return repository -> {
            try {
                return repository.hasPermission(member, perms);
            } catch (IOException ex) {
                return false;
            }
        };
    }
}
