package com.garethahealy.githubstats.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.users.CollectMembersFromRedHatLdapService;
import com.garethahealy.githubstats.services.users.ConfigYamlMemberInRedHatLdapService;
import com.garethahealy.githubstats.services.users.CreateWhoAreYouIssueService;
import com.garethahealy.githubstats.services.users.GitHubMemberInRedHatLdapService;
import freemarker.template.TemplateException;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.resteasy.reactive.RestResponse;
import org.kohsuke.github.GHPermissionType;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Path("/users")
public class UsersResource {

    @Inject
    CollectMembersFromRedHatLdapService collectMembersFromRedHatLdapService;

    @Inject
    CreateWhoAreYouIssueService createWhoAreYouIssueService;

    @Inject
    GitHubMemberInRedHatLdapService gitHubMemberInRedHatLdapService;

    @Inject
    ConfigYamlMemberInRedHatLdapService configYamlMemberInRedHatLdapService;

    @POST
    @Path("collect-members-from-ldap")
    public void collect() throws TemplateException, IOException, ExecutionException, LdapException, InterruptedException {
        collectMembersFromRedHatLdapService.run("redhat-cop", "ldap-members.csv", "ldap-members.csv", "supplementary.csv", false, true);
    }

    @POST
    @Path("create-who-are-you-issues")
    public RestResponse<String> whoAreYou() throws TemplateException, IOException, ExecutionException, LdapException, InterruptedException {
        String resp = createWhoAreYouIssueService.run("redhat-cop", "org", true, "ldap-members.csv", "supplementary.csv", GHPermissionType.READ, false, true);
        return RestResponse.ResponseBuilder
                .create(resp.isEmpty() ? RestResponse.Status.OK : RestResponse.Status.BAD_REQUEST, resp)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    @POST
    @Path("github-member-in-ldap")
    public RestResponse<String> inGitHubLdap() throws TemplateException, IOException, LdapException {
        String resp = gitHubMemberInRedHatLdapService.run("redhat-cop", "org", true, "ldap-members.csv", "supplementary.csv", true);
        return RestResponse.ResponseBuilder
                .create(resp.isEmpty() ? RestResponse.Status.OK : RestResponse.Status.BAD_REQUEST, resp)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    @POST
    @Path("configyaml-member-in-ldap")
    public RestResponse<String> inConfigLdap(@QueryParam("source-org") String sourceOrg, @QueryParam("source-repo") String sourceRepo, @QueryParam("source-branch") String sourceBranch) throws TemplateException, IOException, LdapException {
        List<Members> resp = configYamlMemberInRedHatLdapService.run(sourceOrg, sourceRepo, sourceBranch, "ldap-members.csv", "supplementary.csv", true);

        boolean foundUnknown = false;
        for (Members current : resp) {
            if (current.getEmailAddress().isEmpty()) {
                foundUnknown = true;
                break;
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return RestResponse.ResponseBuilder
                .create(foundUnknown ? RestResponse.Status.BAD_REQUEST : RestResponse.Status.OK, objectMapper.writeValueAsString(resp))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
