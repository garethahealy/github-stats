package com.garethahealy.githubstats.commands.stats;

import com.garethahealy.githubstats.services.stats.CollectStatsService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Dependent
@CommandLine.Command(name = "collect-stats", mixinStandardHelpOptions = true, description = "Collect the stats in CSV format")
public class CollectStatsCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-cfg", "--validate-org-config"}, description = "Whether to check the 'org/config.yaml'", defaultValue = "true")
    boolean validateOrgConfig;

    @CommandLine.Option(names = {"-i", "--required-limit"}, description = "Number of API requests needed", defaultValue = "3000")
    int limit;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @Inject
    CollectStatsService collectStatsService;

    @Override
    public void run() {
        try {
            if (!Files.exists(Path.of(output))) {
                throw new FileNotFoundException("--csv-output=" + output + " not found.");
            }

            collectStatsService.run(organization, validateOrgConfig, limit, output);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}