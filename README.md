# github-stats

CLI to generate stats and issues for a GitHub org.

## Build

Both JVM and Native mode are supported.

```bash
./mvnw clean install
./mvnw clean install -Pnative
```

Which allows you to run via:

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner
java -jar target/quarkus-app/quarkus-run.jar
```

## GitHub Auth

Read permissions are required for the OAuth PAT.

```bash
export GITHUB_LOGIN=replace
export GITHUB_OAUTH=replace
```

## LDAP Lookup

```bash
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'uid=gahealy'
```

## APIs

Once you've built the code, you can execute by...

### CollectStats

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner collect-stats --organization={your-org}
```

Once the binary is complete, you can view the CSV:

```bash
open github-output.csv
```

### CollectRedHatLdapSupplementaryList

Loop over the GitHub members and see if we can find them in LDAP. Output what we find to a CSV.

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner collect-members-from-ldap --organization={your-org} --members-csv={list-of-known-members} --csv-output=supplementary.csv --fail-if-no-vpn=false
```

### GitHubMemberInRedHatLdap

Loop over the GitHub members and see if we can find them in LDAP. Output what we find to a CSV.

`--supplementary-csv` is a list of known members that been created via `CollectRedHatLdapSupplementaryList`

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner github-member-in-ldap --dry-run=true --organization={your-org} --issue-repo={a-repo-in-that-org} --members-csv={list-of-known-members} --supplementary-csv={list-of-supplementary-members} -fail-if-no-vpn=false
```

### CreateWhoAreYouIssue

`--members-csv` is a list of known members that have validated their GitHub ID against their RH ID.
See: `tests/members.csv` as an example.

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner create-who-are-you-issues --dry-run=true --organization={your-org} --issue-repo={a-repo-in-that-org} --members-csv={list-of-known-members} --supplementary-csv={list-of-supplementary-members} --fail-if-no-vpn=false
```