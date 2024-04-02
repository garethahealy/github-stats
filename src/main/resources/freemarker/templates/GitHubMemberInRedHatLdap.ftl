The following users can no longer be found in LDAP, via:

```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=Github->*{their github id}*'
```

<#list users as user>
- @${user.getWhatIsYourGitHubUsername()} (${user.getEmailAddress()})
</#list>