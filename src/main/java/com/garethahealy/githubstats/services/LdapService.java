package com.garethahealy.githubstats.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class LdapService {

    @Inject
    Logger logger;

    @ConfigProperty(name = "redhat.ldap.dn")
    String ldapDn;

    @ConfigProperty(name = "redhat.ldap.connection")
    String ldapConnection;

    @ConfigProperty(name = "redhat.ldap.warmup-user")
    String ldapWarmupUser;

    private final AtomicBoolean warmedUp = new AtomicBoolean(false);
    private Dn systemDn;

    public boolean canConnect() {
        return warmedUp.get();
    }

    @PostConstruct
    void init() {
        try {
            systemDn = new Dn(ldapDn);
            try (LdapConnection connection = open()) {
                try (EntryCursor cursor = connection.search(systemDn, "(uid=" + ldapWarmupUser + ")", SearchScope.SUBTREE, "dn")) {
                    for (Entry entry : cursor) {
                        logger.infof("Warmup found %s", entry.getDn());
                        warmedUp.set(true);
                        break;
                    }
                }
            }
        } catch (IOException | LdapException e) {
            logger.error("Failed to open connection to LDAP", e);
        }
    }

    public LdapConnection open() {
        return new LdapNetworkConnection(ldapConnection);
    }

    public boolean searchOnUser(LdapConnection connection, String uid) throws LdapException, IOException {
        String filter = "(uid=" + uid + ")";
        String value = search(connection, filter, null);
        return !value.isEmpty();
    }

    public String searchOnGitHub(LdapConnection connection, String githubId) throws LdapException, IOException {
        String filter = "(rhatSocialURL=Github->https://github.com/" + githubId + ")";
        return search(connection, filter, "rhatPrimaryMail");
    }

    private String search(LdapConnection connection, String filter, String attribute) throws LdapException, IOException {
        String value = "";
        try (EntryCursor cursor = connection.search(systemDn, filter, SearchScope.SUBTREE, attribute)) {
            int count = 0;
            for (Entry entry : cursor) {
                if (attribute == null) {
                    logger.infof("Found %s", filter);
                    value = entry.getDn().getName();
                } else {
                    Attribute foundAttribute = entry.get(attribute);
                    if (foundAttribute != null) {
                        logger.infof("Found %s - returning %s", filter, attribute);
                        value = foundAttribute.get().toString();
                    }
                }

                count++;
                if (count >= 2) {
                    throw new LdapException("cursor returned multiple entries for: " + filter);
                }
            }
        }

        return value;
    }
}
