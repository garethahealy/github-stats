package com.garethahealy.githubstats.rest.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

public class CreateWhoAreYouIssueService {

    private static final Logger logger = LogManager.getLogger(CreateWhoAreYouIssueService.class);
    private final boolean isDryRun = true;

    public void run() throws IOException, LdapException {
        logger.info("Starting...");

        Cache cache = new Cache(new File("/tmp/github-okhttp"), 10 * 1024 * 1024); // 10MB cache
        GitHub gitHub = GitHubBuilder.fromEnvironment()
                .withConnector(new OkHttpGitHubConnector(new OkHttpClient.Builder().cache(cache).build()))
                .build();

        if (!gitHub.isCredentialValid()) {
            throw new IllegalStateException("isCredentialValid - are GITHUB_LOGIN / GITHUB_OAUTH valid?");
        }

        if (gitHub.isAnonymous()) {
            throw new IllegalStateException("isAnonymous - have you set GITHUB_LOGIN / GITHUB_OAUTH ?");
        }

        logger.info("Connector with cache created.");
        logger.info("RateLimit: limit {}, remaining {}, resetDate {}", gitHub.getRateLimit().getLimit(), gitHub.getRateLimit().getRemaining(), gitHub.getRateLimit().getResetDate());

        if (gitHub.getRateLimit().getRemaining() == 0) {
            throw new IllegalStateException("RateLimit - is zero, you need to wait until the reset date");
        }

        GHOrganization org = gitHub.getOrganization("redhat-cop");
        GHRepository orgRepo = org.getRepository("org");
        List<GHUser> members = org.listMembers().toList();

        logger.info("There are {} members", members.size());

        Set<String> usernamesToIgnore = getUsernamesToIgnore();

        logger.info("There are {} members we already have emails for who will be ignored", usernamesToIgnore.size());

        for (GHUser current : members) {
            if (usernamesToIgnore.contains(current.getLogin())) {
                logger.info("Ignoring: {}", current.getLogin());
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
                    logger.info("DRY-RUN: Would have created issue for {}", current.getLogin());
                } else {
                    builder.create();

                    logger.info("Created issue for {}", current.getLogin());
                }
            }
        }

        logger.info("Issues DONE");

        Set<String> membersLogins = getMembersLogins(members);
        for (String current : usernamesToIgnore) {
            if (!membersLogins.contains(current)) {
                logger.info("Have a google form response but they are not part the git hub org anymore for {}", current);
            }
        }

        logger.info("Responses to GH Org Lookup DONE");

        //ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'uid=gahealy'
        Dn systemDn = new Dn("dc=redhat,dc=com");
        try (LdapConnection connection = new LdapNetworkConnection("ldap.corp.redhat.com")) {
            for (String current : getCollectedEmails()) {
                String uid = current.split("@")[0];
                try (EntryCursor cursor = connection.search(systemDn, "(uid=" + uid + ")", SearchScope.SUBTREE)) {
                    boolean found = false;
                    for (Entry entry : cursor) {
                        entry.getDn();
                        found = true;
                    }

                    if (!found) {
                        logger.info("Did not find {} in ldap", uid);
                    }
                }
            }
        } catch (InvalidConnectionException ex) {
            logger.error("Unable to search ldap for users. Are you on the VPN?", ex);
        }

        logger.info("Ldap Lookup DONE");
    }

    private Set<String> getUsernamesToIgnore() throws IOException {
        Set<String> answer = new HashSet<>();
        try (Reader in = new FileReader("GitHub Red Hat CoP Members (Responses) - Form Responses 1.csv")) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                answer.add(record.get("What is your GitHub username?"));
            }
        }

        return answer;
    }

    private List<String> getCollectedEmails() throws IOException {
        List<String> answer = new ArrayList<>();
        try (Reader in = new FileReader("GitHub Red Hat CoP Members (Responses) - Form Responses 1.csv")) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                answer.add(record.get("Email Address"));
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
