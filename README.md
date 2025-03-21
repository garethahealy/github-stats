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
./target/github-stats-*-runner help
java -jar target/quarkus-app/quarkus-run.jar help
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
./target/github-stats-*-runner users collect-members-from-ldap --organization=redhat-cop --csv-output=ldap-members.csv --ldap-members-csv=ldap-members.csv --fail-if-no-vpn=true --guess=false
```

For a full list of commands, see: [docs](docs)
