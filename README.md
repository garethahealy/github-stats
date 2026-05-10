# github-stats

CLI to generate stats for a GitHub org.

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

Read permissions are required for the OAuth PAT:

```bash
export GITHUB_LOGIN=replace
export GITHUB_OAUTH=replace
```

## APIs

Once you've built the code, you can execute the commands, for example:

```bash
./target/github-stats-*-runner collect-stats --organization=redhat-cop --csv-output=/tmp/redhat-cop-collect-stats.csv --repository-limit=5
```

For a full list of commands, see: [docs](docs)
