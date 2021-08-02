# github-stats
Generates a CSV for a Git Hub org

## Run
As this is a simple mvn project, just:
```
export GITHUB_OAUTH={replace me}
mvn clean install
```

Once the build is complete, you can view the CSV:

```
ls -lh target/github-output.csv
open target/github-output.csv
```