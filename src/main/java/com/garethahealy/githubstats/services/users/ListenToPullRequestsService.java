package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.processors.users.pullrequests.MembersChangeInAnsibleVarsYamlProcessor;
import com.garethahealy.githubstats.processors.users.pullrequests.MembersChangeInConfigYamlProcessor;
import com.garethahealy.githubstats.processors.users.pullrequests.Processor;
import com.garethahealy.githubstats.services.github.GitHubFileRetrievalService;
import com.garethahealy.githubstats.services.github.GitHubOrganizationWriterService;
import com.garethahealy.githubstats.services.github.GitHubRepositoryLookupService;
import com.garethahealy.githubstats.services.users.utils.OrgMemberCsvService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@ApplicationScoped
public class ListenToPullRequestsService {

    @Inject
    Logger logger;

    private final GitHubOrganizationWriterService gitHubOrganizationWriterService;
    private final GitHubRepositoryLookupService gitHubRepositoryLookupService;
    private final GitHubFileRetrievalService gitHubFileRetrievalService;
    private final OrgMemberCsvService orgMemberCsvService;

    private final List<Processor> processors = new ArrayList<>();

    @Inject
    public ListenToPullRequestsService(GitHubOrganizationWriterService gitHubOrganizationWriterService, GitHubRepositoryLookupService gitHubRepositoryLookupService, GitHubFileRetrievalService gitHubFileRetrievalService, OrgMemberCsvService orgMemberCsvService, MembersChangeInConfigYamlProcessor membersChangeInConfigYamlProcessor, MembersChangeInAnsibleVarsYamlProcessor membersChangeInAnsibleVarsYamlProcessor) {
        this.gitHubOrganizationWriterService = gitHubOrganizationWriterService;
        this.gitHubRepositoryLookupService = gitHubRepositoryLookupService;
        this.gitHubFileRetrievalService = gitHubFileRetrievalService;
        this.orgMemberCsvService = orgMemberCsvService;

        processors.add(membersChangeInConfigYamlProcessor);
        processors.add(membersChangeInAnsibleVarsYamlProcessor);
    }

    public void run(String organization, String issueRepo, Set<String> activeProcessors, File ldapMembersCsv, File supplementaryCsv, boolean isDryRun, boolean failNoVpn) throws IOException, LdapException, TemplateException, URISyntaxException {
        GHOrganization org = gitHubOrganizationWriterService.getOrganization(organization);
        GHRepository orgRepo = gitHubOrganizationWriterService.getRepository(org, issueRepo);

        OrgMemberRepository ldapMembers = orgMemberCsvService.parse(ldapMembersCsv);
        OrgMemberRepository supplementaryMembers = orgMemberCsvService.parse(supplementaryCsv);

        logger.infof("There are %s known members and %s supplementary members in the CSVs, total %s", ldapMembers.size(), supplementaryMembers.size(), (ldapMembers.size() + supplementaryMembers.size()));

        process(orgRepo, activeProcessors, ldapMembers, supplementaryMembers, isDryRun, failNoVpn);
    }

    private void process(GHRepository orgRepo, Set<String> activeProcessors, OrgMemberRepository ldapMembers, OrgMemberRepository supplementaryMembers, boolean isDryRun, boolean failNoVpn) throws IOException, TemplateException, LdapException, URISyntaxException {
        List<GHPullRequest> pullRequests = gitHubRepositoryLookupService.listOpenPullRequests(orgRepo);

        logger.infof("Currently %s open pull-requests", pullRequests.size());

        Map<String, Set<String>> data = new HashMap<>();
        data.put(Processor.CONFIG_MEMBERS, getMainConfigMembers());
        data.put(Processor.VARS_MEMBERS, getMainVarsMembers());

        for (Processor processor : processors) {
            if (activeProcessors.contains(processor.id())) {
                for (GHPullRequest current : pullRequests) {
                    if (processor.isActive(current)) {
                        logger.infof("#%s looking at PR", current.getNumber());

                        processor.process(current, data, ldapMembers, supplementaryMembers, isDryRun, failNoVpn);
                    } else {
                        logger.infof("#%s Pull request is not a %s change, ignoring", current.getNumber(), processor.id());
                    }
                }
            } else {
                logger.warnf("Processor %s is not active, ignoring", processor.id());
            }
        }

        logger.info("--> process DONE");
    }

    private Set<String> getMainConfigMembers() throws IOException {
        GHRepository mainOrgRepo = gitHubOrganizationWriterService.getRepository("redhat-cop", "org");
        GHContent mainConfigYaml = gitHubRepositoryLookupService.getConfigYaml(mainOrgRepo, true);
        Set<String> mainMembers = gitHubFileRetrievalService.getConfigMembers(mainConfigYaml);
        if (mainMembers.isEmpty()) {
            throw new IllegalArgumentException("config.yaml members is empty for " + mainOrgRepo.getOwnerName() + "/" + mainOrgRepo.getName() + "/main");
        }

        return mainMembers;
    }

    private Set<String> getMainVarsMembers() throws IOException {
        GHRepository mainOrgRepo = gitHubOrganizationWriterService.getRepository("redhat-cop", "org");
        GHContent mainVarsYaml = gitHubRepositoryLookupService.getAnsibleInventoryGroupVarsAllYml(mainOrgRepo);
        Set<String> mainMembers = gitHubFileRetrievalService.getAnsibleMembers(mainVarsYaml);
        if (mainMembers.isEmpty()) {
            throw new IllegalArgumentException("ansible/inventory/group_vars/all.yml members is empty for " + mainOrgRepo.getOwnerName() + "/" + mainOrgRepo.getName() + "/main");
        }

        return mainMembers;
    }
}
