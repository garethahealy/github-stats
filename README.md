# github-stats
Generates a CSV for a Git Hub org

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