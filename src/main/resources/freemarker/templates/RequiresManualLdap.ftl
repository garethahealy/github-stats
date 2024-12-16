Unable to validate all members in config.yaml. Requires manual validation:

```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=Github->*{your github id}*'
```

- https://source.redhat.com/departments/it/it-information-security/wiki/details_about_rover_github_information_security_and_scanning