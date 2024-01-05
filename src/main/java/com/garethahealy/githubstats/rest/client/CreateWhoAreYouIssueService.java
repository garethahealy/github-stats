package com.garethahealy.githubstats.rest.client;

import com.garethahealy.githubstats.model.MembersInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CreateWhoAreYouIssueService extends BaseGitHubService {

    @Inject
    Logger logger;

    public void run(String organization, String issueRepo, boolean isDryRun, String membersCsv, boolean failNoVpn) throws IOException, LdapException {
        GitHub gitHub = getGitHub();
        GHOrganization org = gitHub.getOrganization(organization);

        GHRepository orgRepo = org.getRepository(issueRepo);
        List<GHUser> members = org.listMembers().toList();

        Set<String> usernamesToIgnore = getUsernamesToIgnore(membersCsv);

        logger.infof("There are %s members", members.size());
        logger.infof("There are %s members we already have emails for who will be ignored", usernamesToIgnore.size());

        for (GHUser current : members) {
            if (usernamesToIgnore.contains(current.getLogin())) {
                logger.infof("Ignoring: %s", current.getLogin());
            } else {
                GHIssueBuilder builder = orgRepo.createIssue("@" + current.getLogin() + " please complete form")
                        .assignee(current)
                        .label("admin")
                        .body("To be a member of the Red Hat CoP GitHub org, you are required to be a Red Hat employee. " +
                                "Non-employees are invited to be outside-collaborators (https://github.com/orgs/redhat-cop/outside-collaborators). " +
                                "As we currently do not know who is an employee and who is not, we are requiring all members to submit the following google form " +
                                "so that we can verify who are employees: " +
                                "https://red.ht/github-redhat-cop-username");

                if (isDryRun) {
                    logger.infof("DRY-RUN: Would have created issue for %s in %s", current.getLogin(), orgRepo.getName());
                } else {
                    builder.create();

                    logger.infof("Created issue for %s", current.getLogin());
                }
            }
        }

        logger.info("Issues DONE");

        Set<String> membersLogins = getMembersLogins(members);
        for (String current : usernamesToIgnore) {
            if (!membersLogins.contains(current)) {
                logger.infof("Have a google form response but they are not part the github org anymore for %s", current);
            }
        }

        logger.info("Responses to GH Org Lookup DONE");

        //ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'uid=gahealy'
        Dn systemDn = new Dn("dc=redhat,dc=com");
        try (LdapConnection connection = new LdapNetworkConnection("ldap.corp.redhat.com")) {
            for (String current : getCollectedEmails(membersCsv)) {
                String uid = current.split("@")[0];
                try (EntryCursor cursor = connection.search(systemDn, "(uid=" + uid + ")", SearchScope.SUBTREE)) {
                    boolean found = false;
                    for (Entry entry : cursor) {
                        entry.getDn();
                        logger.infof("Found %s in ldap", uid);
                        found = true;
                    }

                    if (!found) {
                        logger.infof("Did not find %s in ldap", uid);
                    }
                }
            }
        } catch (InvalidConnectionException ex) {
            if (failNoVpn) {
                logger.error("Unable to search ldap for users. Are you on the VPN?", ex);
            } else {
                logger.warn("Unable to search ldap for users. Are you on the VPN?", ex);
            }
        }

        logger.info("Ldap Lookup DONE");
    }

    private Set<String> getUsernamesToIgnore(String membersCsv) throws IOException {
        Set<String> answer = new HashSet<>();
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(MembersInfo.Headers.class)
                .setSkipHeaderRecord(true)
                .build();

        try (Reader in = new FileReader(membersCsv)) {
            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {
                answer.add(record.get(MembersInfo.Headers.WhatIsYourGitHubUsername));
            }
        }

        return answer;
    }

    private List<String> getCollectedEmails(String membersCsv) throws IOException {
        List<String> answer = new ArrayList<>();
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(MembersInfo.Headers.class)
                .setSkipHeaderRecord(true)
                .build();

        try (Reader in = new FileReader(membersCsv)) {
            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {
                answer.add(record.get(MembersInfo.Headers.EmailAddress));
            }
        }

        return answer;
    }

    private Set<String> getMembersLogins(List<GHUser> members) {
        Set<String> answer = new HashSet<>();
        for (GHUser current : members) {
            answer.add(current.getLogin());
        }

        return answer;
    }
}
