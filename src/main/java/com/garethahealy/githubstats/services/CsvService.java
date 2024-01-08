package com.garethahealy.githubstats.services;

import com.garethahealy.githubstats.model.MembersInfo;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CsvService {

    public Map<String, MembersInfo> getKnownMembers(String membersCsv) throws IOException {
        Map<String, MembersInfo> answer = new HashMap<>();
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(MembersInfo.Headers.class)
                .setSkipHeaderRecord(true)
                .build();

        try (Reader in = new FileReader(membersCsv)) {
            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {
                String timestamp = record.get(MembersInfo.Headers.Timestamp);
                String redhatEmail = record.get(MembersInfo.Headers.EmailAddress);
                String username = record.get(MembersInfo.Headers.WhatIsYourGitHubUsername);

                answer.put(username, new MembersInfo(timestamp, redhatEmail, username));
            }
        }

        return answer;
    }
}
