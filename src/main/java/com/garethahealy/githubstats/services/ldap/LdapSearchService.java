package com.garethahealy.githubstats.services.ldap;

import com.garethahealy.githubstats.model.users.OrgMember;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles searching Red Hat LDAP
 */
@ApplicationScoped
public class LdapSearchService {

    public static class AttributeKeys {
        public static final String Dn = "dn";
        public static final String PrimaryMail = "rhatPrimaryMail";
        public static final String SocialURL = "rhatSocialURL";
        public static final String SocialURLGitHub = SocialURL + "->Github";
        public static final String SocialURLQuay = SocialURL + "->Quay";
    }

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

    public LdapSearchService() {

    }

    /**
     * Attempts to connect to LDAP
     */
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

    /**
     * Can connect to LDAP
     *
     * @return
     */
    public boolean canConnect() {
        if (!warmedUp.get()) {
            init();
        }

        return warmedUp.get();
    }

    /**
     * Create a LdapConnection
     *
     * @return
     */
    public LdapConnection open() {
        return new LdapNetworkConnection(ldapConnection);
    }

    /**
     * Search based on uid=${param}
     *
     * @param connection
     * @param uid
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public boolean searchOnUser(LdapConnection connection, String uid) throws LdapException, IOException {
        String filter = "(uid=" + uid + ")";
        String answer = searchDn(connection, filter);
        return !answer.isEmpty();
    }

    /**
     * Search based on Dn
     *
     * @param connection
     * @param filter
     * @return
     * @throws LdapException
     * @throws IOException
     */
    private String searchDn(LdapConnection connection, String filter) throws LdapException, IOException {
        String answer = "";

        try (EntryCursor cursor = connection.search(systemDn, filter, SearchScope.SUBTREE, AttributeKeys.Dn)) {
            for (Entry entry : cursor) {
                logger.debugf("Found %s", filter);

                if (entry.getAttributes().isEmpty()) {
                    logger.debugf("- returning dn == %s", entry.getDn().getName());
                    answer = entry.getDn().getName();
                }
            }
        }

        return answer;
    }

    /**
     * Search based on cn=${param}
     *
     * @param connection
     * @param name
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public String searchOnName(LdapConnection connection, String name) throws LdapException, IOException {
        String filter = "(cn=" + name + ")";
        return searchAndGetPrimaryMail(connection, filter);
    }

    /**
     * Search based on uid=${param}
     *
     * @param connection
     * @param githubId
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public String searchOnGitHubLogin(LdapConnection connection, String githubId) throws LdapException, IOException {
        String filter = "(uid=" + githubId + ")";
        return searchAndGetPrimaryMail(connection, filter);
    }

    /**
     * Search based on ${AttributeKeys.SocialURL}=Github->${param}
     *
     * @param connection
     * @param githubId
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public String searchOnGitHubSocial(LdapConnection connection, String githubId) throws LdapException, IOException {
        String found = searchOnGitHubSocialExact(connection, githubId);
        if (found.isEmpty()) {
            found = searchOnGitHubSocialFuzzy(connection, githubId);
        }

        return found;
    }

    private String searchOnGitHubSocialExact(LdapConnection connection, String githubId) throws LdapException, IOException {
        String filter = "(" + AttributeKeys.SocialURL + "=Github->https://github.com/" + githubId + ")";
        return searchAndGetPrimaryMail(connection, filter);
    }

    private String searchOnGitHubSocialFuzzy(LdapConnection connection, String githubId) throws LdapException, IOException {
        String filter = "(" + AttributeKeys.SocialURL + "=Github->*" + githubId + "*)";
        return searchAndGetPrimaryMail(connection, filter);
    }

    /**
     * Search based on ${AttributeKeys.SocialURL}=Quay->${param}
     *
     * @param connection
     * @param quayId
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public String searchOnQuaySocial(LdapConnection connection, String quayId) throws LdapException, IOException {
        String filter = "(" + AttributeKeys.SocialURL + "=Quay->*" + quayId + "*)";
        return searchAndGetPrimaryMail(connection, filter);
    }

    /**
     * Search based on ${AttributeKeys.PrimaryMail}=${param}
     *
     * @param connection
     * @param email
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public String searchOnPrimaryMail(LdapConnection connection, String email) throws LdapException, IOException {
        String filter = "(" + AttributeKeys.PrimaryMail + "=" + email + ")";
        return searchAndGetPrimaryMail(connection, filter);
    }

    /**
     * Search based on a filter and return the PrimaryMail
     *
     * @param connection
     * @param filter
     * @return
     * @throws LdapException
     * @throws IOException
     */
    private String searchAndGetPrimaryMail(LdapConnection connection, String filter) throws LdapException, IOException {
        String answer = "";

        try (EntryCursor cursor = connection.search(systemDn, filter, SearchScope.SUBTREE, AttributeKeys.PrimaryMail)) {
            int count = 0;
            for (Entry entry : cursor) {
                logger.debugf("Found %s", filter);

                for (Attribute found : entry.getAttributes()) {
                    if (found.getId().equalsIgnoreCase(AttributeKeys.PrimaryMail)) {
                        logger.debugf("- returning %s == %s", AttributeKeys.PrimaryMail, found.get().toString());
                        answer = found.get().toString();
                    }
                }

                count++;
                if (count >= 2) {
                    throw new LdapException("cursor returned multiple entries for: " + filter);
                }
            }
        }

        return answer;
    }

    /**
     * Retrieve OrgMember based on ${AttributeKeys.PrimaryMail}=${email} and ${AttributeKeys.SocialURL}=Github->${githubId}
     *
     * @param connection
     * @param member
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public OrgMember retrieve(LdapConnection connection, OrgMember member) throws LdapException, IOException {
        String filter = "(&(" + AttributeKeys.PrimaryMail + "=" + member.redhatEmailAddress() + ")(" + AttributeKeys.SocialURL + "=Github->*" + member.gitHubUsername() + "*))";
        OrgMember found = OrgMember.from(member.gitHubUsername(), search(connection, filter, AttributeKeys.PrimaryMail, AttributeKeys.SocialURL));
        return found.redhatEmailAddress() == null || found.redhatEmailAddress().isEmpty() ? null : found;
    }

    /**
     * Retrieve OrgMember based on ${AttributeKeys.PrimaryMail}=${param}
     *
     * @param connection
     * @param email
     * @return
     * @throws LdapException
     * @throws IOException
     */
    public OrgMember retrieve(LdapConnection connection, String githubUsername, String email) throws LdapException, IOException {
        String filter = "(" + AttributeKeys.PrimaryMail + "=" + email + ")";
        OrgMember found = OrgMember.from(githubUsername, search(connection, filter, AttributeKeys.PrimaryMail, AttributeKeys.SocialURL));
        return found.redhatEmailAddress() == null || found.redhatEmailAddress().isEmpty() ? null : found;
    }

    /**
     * Search based on a filter and return certain attributes
     *
     * @param connection
     * @param filter
     * @param attributes
     * @return
     * @throws LdapException
     * @throws IOException
     */
    private Map<String, List<String>> search(LdapConnection connection, String filter, String... attributes) throws LdapException, IOException {
        Map<String, List<String>> values = new HashMap<>();

        try (EntryCursor cursor = connection.search(systemDn, filter, SearchScope.SUBTREE, attributes)) {
            int count = 0;
            for (Entry entry : cursor) {
                logger.debugf("Found %s", filter);

                if (entry.getAttributes().isEmpty()) {
                    logger.debugf("- returning dn == %s", entry.getDn().getName());
                    values.put("dn", List.of(entry.getDn().getName()));
                } else {
                    for (Attribute found : entry.getAttributes()) {
                        if (found.getId().equalsIgnoreCase(AttributeKeys.PrimaryMail)) {
                            logger.debugf("- returning %s == %s", AttributeKeys.PrimaryMail, found.get().toString());
                            values.put(AttributeKeys.PrimaryMail, List.of(found.get().toString()));
                        } else if (found.getId().equalsIgnoreCase(AttributeKeys.SocialURL)) {
                            for (Value current : found) {
                                String[] split = current.toString().split("->");

                                String key = AttributeKeys.SocialURL + "->" + split[0];
                                String valueStr = split[1];

                                logger.debugf("- returning %s == %s", key, valueStr);
                                if (values.containsKey(key)) {
                                    List<String> items = values.get(key);
                                    items.add(valueStr);
                                } else {
                                    values.put(key, new ArrayList<>(List.of(valueStr)));
                                }
                            }
                        } else {
                            logger.debugf("- returning %s == %s", found.getId(), found.get().toString());
                            values.put(found.getId(), List.of(found.get().toString()));
                        }
                    }
                }

                count++;
                if (count >= 2) {
                    throw new LdapException("cursor returned multiple entries for: " + filter);
                }
            }
        }

        return values;
    }
}
