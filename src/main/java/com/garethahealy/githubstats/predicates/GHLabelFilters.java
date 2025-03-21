package com.garethahealy.githubstats.predicates;

import org.kohsuke.github.GHLabel;

import java.util.function.Predicate;

public class GHLabelFilters {

    /**
     * Filter by the name
     *
     * @param name
     * @return
     */
    public static Predicate<GHLabel> equals(String name) {
        return label -> name.equalsIgnoreCase(label.getName());
    }
}
