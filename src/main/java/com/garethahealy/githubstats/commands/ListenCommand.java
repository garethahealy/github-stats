package com.garethahealy.githubstats.commands;

import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import picocli.CommandLine;

@Dependent
@CommandLine.Command(name = "listen", mixinStandardHelpOptions = true, description = "Starts web service")
public class ListenCommand implements Runnable {

    @Inject
    Logger logger;

    @Override
    public void run() {
        logger.info("--> listening on REST");
        Quarkus.waitForExit();
    }
}