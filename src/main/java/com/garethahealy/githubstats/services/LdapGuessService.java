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
                            unknown.add(user);
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

            for (GHUser current : unknown) {
                logger.infof("Unable to work out: %s", current.getLogin());
            }

            StringBuilder emailList = new StringBuilder();
            for (Map.Entry<String, GHUser> current : guessed.entrySet()) {
                logger.infof("Think %s is %s", current.getValue().getLogin(), current.getKey());
                emailList.append(current.getKey()).append(",");
            }

            logger.infof("%s", emailList.toString());

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
}
