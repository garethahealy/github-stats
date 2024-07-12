package com.garethahealy.githubstats.services;

import com.garethahealy.githubstats.model.csv.Members;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jboss.logging.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@ApplicationScoped
public class CsvService {

    @Inject
    Logger logger;

    public Map<String, Members> getKnownMembers(String membersCsv) throws IOException {
        Map<String, Members> answer = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(Members.Headers.class)
                .setSkipHeaderRecord(true)
                .build();

        try (Reader in = new FileReader(membersCsv)) {
            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {
                String timestamp = record.get(Members.Headers.Timestamp);
                String redhatEmail = record.get(Members.Headers.EmailAddress);
                String username = record.get(Members.Headers.WhatIsYourGitHubUsername);

                answer.put(username.trim(), new Members(timestamp, redhatEmail.trim(), username.trim()));
            }
        }

        return answer;
    }

    public void writeLdapMembersCsv(String output, List<Members> members, boolean isCsvEmpty) throws IOException {
        if (!members.isEmpty()) {
            CSVFormat.Builder csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT);
            if (isCsvEmpty) {
                csvFormat.setHeader(Members.Headers.class);
            }

            try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output), StandardOpenOption.APPEND), csvFormat.build())) {
                for (Members current : members) {
                    csvPrinter.printRecord(current.toArray());
                }
            }

            logger.info("--> Write LdapMembers CSV DONE");
        }
    }

    public void writeSupplementaryMembersCsv(String output, List<Members> members) throws IOException {
        CSVFormat.Builder csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT);
        csvFormat.setHeader(Members.Headers.class);

        members.sort((o1, o2) -> new CompareToBuilder().append(o1.getTimestamp(), o2.getTimestamp()).toComparison());

        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(output), StandardOpenOption.TRUNCATE_EXISTING), csvFormat.build())) {
            for (Members current : members) {
                csvPrinter.printRecord(current.toArray());
            }
        }

        logger.info("--> Write Supplementary CSV DONE");
    }
}
