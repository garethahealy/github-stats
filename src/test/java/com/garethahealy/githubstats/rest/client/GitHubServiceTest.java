package com.garethahealy.githubstats.rest.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.garethahealy.githubstats.model.RepoInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GitHubServiceTest {

    @Test
    public void canRun() throws IOException {
        GitHubService service = new GitHubService();
        List<RepoInfo> answer = service.run();

        Assertions.assertNotNull(answer);
        Assertions.assertTrue(answer.size() > 0);

        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get("target/github-output.csv")), CSVFormat.DEFAULT.withHeader(RepoInfo.Headers.class))) {
            for (RepoInfo current : answer) {
                csvPrinter.printRecord(current.toArray());
            }
        }
    }
}