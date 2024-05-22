To be a member of the Red Hat CoP GitHub organization, you are required to be a Red Hat employee.
Non-employees are invited to be outside-collaborators (https://github.com/orgs/redhat-cop/outside-collaborators).

To resolve GitHub IDs to Red Hat IDs, we check if we can find you via LDAP:
```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=Github->*{your github id}*'
```

If you are unsure how to set your GitHub ID within LDAP, see:
- https://source.redhat.com/departments/it/it-information-security/wiki/details_about_rover_github_information_security_and_scanning

Please add your GitHub handle to Rover.

<#list users as user>
- [ ] @${user.username()}
</#list>