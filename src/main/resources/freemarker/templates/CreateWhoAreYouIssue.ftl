To be a member of the Red Hat CoP GitHub organization, you are required to be a Red Hat employee.
Non-employees are invited to be outside-collaborators (https://github.com/orgs/redhat-cop/outside-collaborators).

To resolve GitHub IDs to Red Hat IDs, we check if a response of the below form has been provided, if not, we search LDAP.
- https://red.ht/github-redhat-cop-username

If you are unsure how to set your GitHub ID within LDAP, see:
- https://source.redhat.com/departments/it/it-information-security/wiki/details_about_rover_github_information_security_and_scanning

The below list of members have *${permissions}* and cannot be found using the above methods.

Please complete the above form or add your GitHub handle to Rover.

<#list users as user>
- @${user.getWhatIsYourGitHubUsername()}
</#list>