package com.garethahealy.githubstats.services;

import com.garethahealy.githubstats.model.csv.Members;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class LdapGuessService {

    @Inject
    Logger logger;

    private final LdapService ldapService;
    private final GitHubService gitHubService;

    public LdapGuessService(LdapService ldapService, GitHubService gitHubService) {
        this.ldapService = ldapService;
        this.gitHubService = gitHubService;
    }

    public void attemptToGuess(Map<String, Members> knownMembers, List<GHUser> members, boolean shouldGuess, boolean failNoVpn, GHOrganization org) throws IOException, LdapException, ExecutionException, InterruptedException {
        List<GHUser> guess = new ArrayList<>();
        for (GHUser current : members) {
            if (!knownMembers.containsKey(current.getLogin())) {
                guess.add(current);
            }
        }

        attemptToGuess(guess, shouldGuess, failNoVpn, org);
    }

    public void attemptToGuess(List<GHUser> members, boolean shouldGuess, boolean failNoVpn, GHOrganization org) throws IOException, LdapException, ExecutionException, InterruptedException {
        if (!shouldGuess) {
            logger.info("--> Attempt to guess DONE");
            return;
        }

        Map<String, GHUser> guessed = new TreeMap<>();
        Set<GHUser> unknown = new TreeSet<>(Comparator.comparing(GHPerson::getLogin));
        Set<GHUser> unknownWorksForRH = new TreeSet<>(Comparator.comparing(GHPerson::getLogin));

        if (ldapService.canConnect()) {
            try (LdapConnection connection = ldapService.open()) {
                for (GHUser user : members) {
                    Pair<String, GHUser> guess = guessViaGithubProfileEmail(connection, user);
                    if (guess == null) {
                        guess = guessViaGithubLogin(connection, user);
                    }

                    if (guess == null) {
                        guess = guessViaGithubProfileName(connection, user);
                    }

                    if (guess == null) {
                        if (guessWorksForRedHat(user)) {
                            unknownWorksForRH.add(user);
                        } else {
                            unknown.add(user);
                        }
                    } else {
                        guessed.put(guess.getKey(), guess.getValue());
                    }
                }
            }
        } else {
            if (failNoVpn) {
                throw new IOException("Unable to connect to LDAP. Are you on the VPN?");
            }
        }

        Map<GHUser, String> history = null;
        if (!unknown.isEmpty() || !unknownWorksForRH.isEmpty()) {
            history = gitHubService.getContributedTo(org, unknown, unknownWorksForRH);
        }

        if (!unknown.isEmpty()) {
            logger.info("Unable to work out the following:");

            for (GHUser current : unknown) {
                String company = current.getCompany() == null ? "" : " - they work for: " + current.getCompany();
                String contribHistory = history.getOrDefault(current, "No contrib history");

                logger.infof("-> %s%s - %s", current.getLogin(), company, contribHistory);
            }
        }

        if (!unknownWorksForRH.isEmpty()) {
            logger.info("Unable to work out the following via LDAP but profile says Red Hat:");

            for (GHUser current : unknownWorksForRH) {
                String contribHistory = history.getOrDefault(current, "No contrib history");

                logger.infof("-> %s - %s", current.getLogin(), contribHistory);
            }
        }

        if (!guessed.isEmpty()) {
            logger.info("Guessed the following via LDAP:");

            StringBuilder emailList = new StringBuilder();
            for (Map.Entry<String, GHUser> current : guessed.entrySet()) {
                logger.infof("-> %s is %s", current.getValue().getLogin(), current.getKey());
                emailList.append(current.getKey()).append(",");
            }

            logger.infof("Email list dump: %s", emailList.toString());
        }

        logger.info("--> Attempt to guess DONE");
    }

    private Pair<String, GHUser> guessViaGithubProfileEmail(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (user.getEmail().contains("@redhat.com")) {
                String rhEmail = ldapService.searchOnEmail(connection, user.getEmail());
                if (!rhEmail.isEmpty()) {
                    answer = Pair.of(rhEmail, user);
                }
            }
        }

        return answer;
    }

    private Pair<String, GHUser> guessViaGithubLogin(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        String rhEmail = ldapService.searchOnGitHubLogin(connection, user.getLogin());
        if (!rhEmail.isEmpty()) {
            answer = Pair.of(rhEmail, user);
        }

        return answer;
    }

    private Pair<String, GHUser> guessViaGithubProfileName(LdapConnection connection, GHUser user) throws IOException, LdapException {
        Pair<String, GHUser> answer = null;
        if (user.getName() != null && !user.getName().isEmpty()) {
            String rhEmail = ldapService.searchOnName(connection, user.getName());
            if (!rhEmail.isEmpty()) {
                answer = Pair.of(rhEmail, user);
            }
        }

        return answer;
    }

    private boolean guessWorksForRedHat(GHUser user) throws IOException {
        boolean answer = false;
        if (user.getCompany() != null && !user.getCompany().isEmpty()) {
            String companyLower = user.getCompany().toLowerCase();
            if (companyLower.contains("redhat") || companyLower.contains("red hat")) {
                answer = true;
            }
        }

        return answer;
    }
}
