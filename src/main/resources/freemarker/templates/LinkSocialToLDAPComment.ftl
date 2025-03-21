To be a member of the Red Hat CoP ${system} organization, you are required to be a Red Hat employee.

To resolve ${system} IDs to Red Hat IDs, we check if we can find you via LDAP:
```
ldapsearch -x -h ldap.corp.redhat.com -b dc=redhat,dc=com -s sub 'rhatSocialURL=${system}->*{your ${system} id}*'
```

If you are unsure how to set your ${system} ID within LDAP, see:
- https://source.redhat.com/departments/it/it-information-security/wiki/details_about_rover_github_information_security_and_scanning
<#if permissions??>

The below list of members have *${permissions}* on a repository and cannot be found using the above method.

</#if>
Please add your ${system} handle to Rover:

<#list users as user>
- [ ] @${user.gitHubUsername} <#if user.redhatEmailAddress??> (guessed as: ${user.redhatEmailAddress})</#if>
</#list>
