#!/usr/bin/env bash

./mvnw clean install -Pnative

source creds.source

#./target/github-stats-*-runner stats collect-stats --organization=redhat-cop

./target/github-stats-*-runner users collect-members-from-ldap --organization=redhat-cop --csv-output=ldap-members.csv --ldap-members-csv=ldap-members.csv --fail-if-no-vpn=true --guess=false >> output.log

./target/github-stats-*-runner users create-who-are-you-issues --dry-run=true --organization=redhat-cop --issue-repo=org --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --permission=read  --fail-if-no-vpn=true --guess=true >> output.log

./target/github-stats-*-runner users github-member-in-ldap --dry-run=true --organization=redhat-cop --issue-repo=org --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true >> output.log

./target/github-stats-*-runner users quay-still-correct --dry-run=true --organization=redhat-cop --issue-repo=org >> output.log

cat output.log