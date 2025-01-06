To be a member of the Red Hat CoP Quay organization, you are required to be a Red Hat employee.

To resolve Quay IDs to Red Hat IDs, we check if we can find you via LDAP:
```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=Quay-->*{your quay id}*'
```

Please add your Quay handle to Rover:

<#list users as user>
- [ ] @${user}
</#list>