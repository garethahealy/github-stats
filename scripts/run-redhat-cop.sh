#!/usr/bin/env bash

scripts/download-memebers-sheet.sh

./mvnw clean install -Pnative

#./target/github-stats-1.0.0-SNAPSHOT-runner collect-stats --organization=redhat-cop

./target/github-stats-1.0.0-SNAPSHOT-runner collect-members-from-ldap --organization=redhat-cop --members-csv=gh-members.csv --csv-output=supplementary.csv --fail-if-no-vpn=true
./target/github-stats-1.0.0-SNAPSHOT-runner github-member-in-ldap --organization=redhat-cop --issue-repo=org --members-csv=gh-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true