# github-stats
CLI to generate stats and issues for a GitHub org.

## Build
Both JVM and Native mode are supported.

```bash
./mvnw clean install
./mvnw clean install -Pnative
```

## GitHub Auth
Read permissions are required for the OAuth PAT.

```
export GITHUB_LOGIN=replace
export GITHUB_OAUTH=replace
```

## APIs
Once you've built the code, you can execute by...

### CollectStatsService
```
./target/github-stats-1.0.0-SNAPSHOT-runner collect-stats --organization={your-org}
```

Once the binary is complete, you can view the CSV:

```bash
open github-output.csv
```

### CreateWhoAreYouIssueService
`--members-csv` is a list of known members that have validated their GitHub ID against their RH ID. See: `tests/members.csv` as an example.

```
./target/github-stats-1.0.0-SNAPSHOT-runner create-who-are-you-issues --dry-run=true --organization={your-org} --issue-repo={a-repo-in-that-org} --members-csv={list-of-known-members} --fail-if-no-vpn=false
```