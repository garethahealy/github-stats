The following users can no longer be found in LDAP, via:

```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub '(&(rhatPrimaryMail={your Red Hat id}@redhat.com)(rhatSocialURL=Github->*{your GitHub id}*))''
```

<#list users as user>
- @${user.gitHubUsername} - (${user.redhatEmailAddress})
</#list>
