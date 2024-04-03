package com.garethahealy.githubstats.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LdapGuessService {

    @Inject
    Logger logger;

    private final LdapService ldapService;

    public LdapGuessService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    public void attemptToGuess(List<GHUser> members, boolean shouldGuess, boolean failNoVpn) throws IOException, LdapException {
        if (shouldGuess) {
            Map<String, GHUser> guessed = new HashMap<>();
            List<GHUser> unknown = new ArrayList<>();
            List<GHUser> unknownWorksForRH = new ArrayList<>();

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

            if (!unknown.isEmpty()) {
                logger.info("Unable to work out the following:");

                for (GHUser current : unknown) {
                    String company = current.getCompany() == null ? "" : " - they work for: " + current.getCompany();
                    logger.infof("-> %s%s", current.getLogin(), company);
                }
            }

            if (!unknownWorksForRH.isEmpty()) {
                logger.info("Unable to work out the following via LDAP but profile says Red Hat:");

                for (GHUser current : unknownWorksForRH) {
                    logger.infof("-> %s", current.getLogin());
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
        String rhEmail = ldapService.searchOnGitHubLogin(connection, user.getName());
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
