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
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=Github->*garethahealy*'
```

## APIs

Once you've built the code, you can execute the commands, for example:

```bash
./target/github-stats-1.0.0-SNAPSHOT-runner collect-members-from-ldap --organization={your-org} --csv-output=supplementary.csv --supplementary-csv={list-of-supplementary-members} --guess=false --fail-if-no-vpn=false
```

For a full list of commands, see: [docs](docs)