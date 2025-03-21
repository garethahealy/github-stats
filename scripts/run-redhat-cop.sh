#!/usr/bin/env bash

./mvnw clean install -Pnative

source creds.source

#./target/github-stats-*-runner stats collect-stats --organization=redhat-cop --csv-output=target/redhat-cop-collect-stats.csv --validate-org-config=true

./target/github-stats-*-runner users collect-members-from-ldap --organization=redhat-cop --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --validate-csv=false --fail-if-no-vpn=true >> output.log
./target/github-stats-*-runner users create-who-are-you-issues -dry-run=true --organization=redhat-cop --issue-repo=org --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true --permission=read --guess=true >> output.log
./target/github-stats-*-runner users listen-to-issues --dry-run=true --organization=redhat-cop --issue-repo=org --processors=AddMeAsMember --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true >> output.log
./target/github-stats-*-runner users listen-to-pullrequests --dry-run=true --organization=redhat-cop --issue-repo=org --processors=MembersChangeInConfigYaml,MembersChangeInAnsibleVarsYaml --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --fail-if-no-vpn=true >> output.log


cat output.log
