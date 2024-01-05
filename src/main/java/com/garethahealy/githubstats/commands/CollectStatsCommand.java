package com.garethahealy.githubstats.commands;

import com.garethahealy.githubstats.rest.client.CollectStatsService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.IOException;

@Dependent
@CommandLine.Command(name = "collect-stats", mixinStandardHelpOptions = true, description = "Collect the stats in CSV format")
public class CollectStatsCommand implements Runnable {

    @CommandLine.Option(names = {"-org", "--organization"}, description = "GitHub organization", required = true)
    String organization;

    @CommandLine.Option(names = {"-cfg", "--validate-org-config"}, description = "Whether to check the 'org/config.yaml'", defaultValue = "false")
    boolean validateOrgConfig;

    @CommandLine.Option(names = {"-o", "--csv-output"}, description = "Output location for CSV", defaultValue = "github-output.csv")
    String output;

    @Inject
    CollectStatsService collectStatsService;

    @Override
    public void run() {
        try {
            collectStatsService.run(organization, validateOrgConfig, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}