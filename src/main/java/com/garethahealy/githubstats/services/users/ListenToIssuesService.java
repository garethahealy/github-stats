package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.processors.users.issues.AddMeAsMemberProcessor;
import com.garethahealy.githubstats.processors.users.issues.Processor;
import com.garethahealy.githubstats.services.github.GitHubOrganizationWriterService;
import com.garethahealy.githubstats.services.github.GitHubRepositoryLookupService;
import com.garethahealy.githubstats.services.users.utils.OrgMemberCsvService;
import freemarker.template.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ListenToIssuesService {

    @Inject
    Logger logger;

    private final GitHubOrganizationWriterService gitHubOrganizationWriterService;
    private final GitHubRepositoryLookupService gitHubRepositoryLookupService;
    private final OrgMemberCsvService orgMemberCsvService;
    private final List<Processor> processors = new ArrayList<>();

    @Inject
    public ListenToIssuesService(GitHubOrganizationWriterService gitHubOrganizationWriterService, GitHubRepositoryLookupService gitHubRepositoryLookupService, OrgMemberCsvService orgMemberCsvService, AddMeAsMemberProcessor addMeAsMemberProcessor) {
        this.gitHubOrganizationWriterService = gitHubOrganizationWriterService;
        this.gitHubRepositoryLookupService = gitHubRepositoryLookupService;
        this.orgMemberCsvService = orgMemberCsvService;

        processors.add(addMeAsMemberProcessor);
    }

    public void run(String organization, String issueRepo, Set<String> activeProcessors, File ldapMembersCsv, File supplementaryCsv, boolean isDryRun, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GHOrganization org = gitHubOrganizationWriterService.getOrganization(organization);
        GHRepository orgRepo = gitHubOrganizationWriterService.getRepository(org, issueRepo);

        OrgMemberRepository ldapMembers = orgMemberCsvService.parse(ldapMembersCsv);
        OrgMemberRepository supplementaryMembers = orgMemberCsvService.parse(supplementaryCsv);

        logger.infof("There are %s known members and %s supplementary members in the CSVs, total %s", ldapMembers.size(), supplementaryMembers.size(), (ldapMembers.size() + supplementaryMembers.size()));

        process(orgRepo, activeProcessors, ldapMembers, supplementaryMembers, isDryRun, failNoVpn);
    }

    private void process(GHRepository orgRepo, Set<String> activeProcessors, OrgMemberRepository ldapMembers, OrgMemberRepository supplementaryMembers, boolean isDryRun, boolean failNoVpn) throws IOException, LdapException {
        List<GHIssue> openIssues = gitHubRepositoryLookupService.listOpenIssues(orgRepo);

        logger.infof("Currently %s open issues", openIssues.size());

        for (Processor processor : processors) {
            if (activeProcessors.contains(processor.id())) {
                for (GHIssue current : openIssues) {
                    if (processor.isActive(current)) {
                        logger.infof("#%s looking at Issue", current.getNumber());

                        processor.process(current, ldapMembers, supplementaryMembers, isDryRun, failNoVpn);
                    } else {
                        logger.infof("#%s Issue is not a %s change, ignoring", current.getNumber(), processor.id());
                    }
                }
            } else {
                logger.warnf("Processor %s is not active, ignoring", processor.id());
            }
        }
    }
}
