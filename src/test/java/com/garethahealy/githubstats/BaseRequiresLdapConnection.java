package com.garethahealy.githubstats;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.search.FilterBuilder;

import java.io.IOException;

public abstract class BaseRequiresLdapConnection {

    String ldapConnection = "ldap.corp.redhat.com";
    String ldapDn = "dc=redhat,dc=com";
    String ldapWarmupUser = "gahealy";

    protected boolean canConnectVpn() {
        boolean canConnect = false;

        try {
            try (LdapConnection connection = new LdapNetworkConnection(ldapConnection)) {
                String filter = FilterBuilder.equal("uid", ldapWarmupUser).toString();
                try (EntryCursor cursor = connection.search(new Dn(ldapDn), filter, SearchScope.SUBTREE, "dn")) {
                    for (Entry entry : cursor) {
                        canConnect = true;
                        break;
                    }
                }
            }
        } catch (IOException | LdapException ex) {
            //Ignore
        }

        return canConnect;
    }
}
