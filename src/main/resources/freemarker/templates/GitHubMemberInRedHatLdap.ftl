The following users can no longer be found in LDAP:

<#list users as user>
- @${user.getWhatIsYourGitHubUsername()} (${user.getEmailAddress()})
</#list>