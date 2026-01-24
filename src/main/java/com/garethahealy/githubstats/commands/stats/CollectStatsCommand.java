package com.garethahealy.githubstats.commands.stats;

import com.garethahealy.githubstats.services.stats.CollectStatsService;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@CommandLine.Command(name = "collect-stats", mixinStandardHelpOptions = true, description = "Collect the stats in CSV format")
public class CollectStatsCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-cfg", "--validate-org-config"}, description = "Whether to check the 'org/config.yaml'", defaultValue = "true")
    boolean validateOrgConfig;

    @CommandLine.Option(names = {"-api", "--api-limit"}, description = "Number of API requests needed", defaultValue = "3000")
    int limit;

    @CommandLine.Option(names = {"-l", "--repository-limit"}, description = "Number of GitHub repositories to check", defaultValue = "0")
    int repoLimit;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @Inject
    CollectStatsService collectStatsService;

    @Override
    public void run() {
        try {
            if (!Files.exists(Path.of(output))) {
                new File(output).createNewFile();
            }

            collectStatsService.run(organization, validateOrgConfig, repoLimit, limit, new File(output));
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
