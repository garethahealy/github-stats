package com.garethahealy.githubstats.commands;

import java.nio.file.Files;
import java.nio.file.Path;

public class BaseCommand {

    protected String getRunner() {
        return System.getProperty("native.image.path");
    }

    protected boolean isRunnerSet() {
        String value = getRunner();
        return Files.exists(Path.of(value));
    }
}
