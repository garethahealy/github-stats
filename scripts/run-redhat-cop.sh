#!/usr/bin/env bash

scripts/download-memebers-sheet.sh

./mvnw clean install -Pnative

source creds.source

#./target/github-stats-*-runner collect-stats --organization=redhat-cop

./target/github-stats-*-runner collect-members-from-ldap --organization=redhat-cop --members-csv=gh-members.csv --csv-output=supplementary.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true >> output.log
./target/github-stats-*-runner github-member-in-ldap --dry-run=true --organization=redhat-cop --issue-repo=org --members-csv=gh-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true >> output.log

./target/github-stats-*-runner create-who-are-you-issues --dry-run=true --organization=redhat-cop --issue-repo=org --members-csv=gh-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true --permission=admin >> output.log
./target/github-stats-*-runner create-who-are-you-issues --dry-run=true --organization=redhat-cop --issue-repo=org --members-csv=gh-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true --permission=write >> output.log