# github-stats
Generates a CSV for a Git Hub org

# CollectStatsService
## Run
As this is a simple mvn project, just:
```
export GITHUB_LOGIN=replace
export GITHUB_OAUTH=replace
mvn clean install -Dtest=CollectStatsServiceTest
```

Once the build is complete, you can view the CSV:

```
ls -lh target/github-output.csv
open target/github-output.csv
```

# CreateWhoAreYouIssueService
## Download Responses
The current form responses can be excluded from the issue creation by downloading and putting in the root directory:
- GitHub Red Hat CoP Members (Responses) - Form Responses 1.csv

## Run
As this is a simple mvn project, just:
```
export GITHUB_LOGIN=replace
export GITHUB_OAUTH=replace
mvn clean install -Dtest=CreateWhoAreYouIssueServiceTest
```