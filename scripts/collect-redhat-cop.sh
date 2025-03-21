#!/usr/bin/env bash

pushd ~/Documents/Git/github.com/garethahealy/github-stats

source creds.source

./target/github-stats-*-runner users collect-members-from-ldap --organization=redhat-cop --ldap-members-csv=ldap-members.csv --supplementary-csv=supplementary.csv --validate-csv=false --fail-if-no-vpn=true

rm -rf member-mapping || true
git clone git@github.com:redhat-cop-dev/member-mapping.git

cp ldap-members.csv member-mapping/ldap-members.csv
cp supplementary.csv member-mapping/supplementary.csv

pushd ~/Documents/Git/github.com/garethahealy/github-stats/member-mapping

git add *.csv
git commit -m "updated members list"
git push

popd
popd
