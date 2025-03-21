package com.garethahealy.githubstats.commands;

public class BaseCommand {

    protected String getRunner() {
        // handle if runner doesnt exist, run it as a standard java app
        return System.getProperty("native.image.path");
    }
}
