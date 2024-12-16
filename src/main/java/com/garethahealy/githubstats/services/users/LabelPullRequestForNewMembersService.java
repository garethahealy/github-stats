package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.WhoAreYou;
import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.GitHubService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jboss.logging.Logger;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@ApplicationScoped
public class LabelPullRequestForNewMembersService {

    @Inject
    Logger logger;

    @Inject
    @TemplatePath("RequiresManualLdap.ftl")
    Template requiresManualLdap;

    @Inject
    @TemplatePath("UserInLdap.ftl")
    Template userInLdap;

    @Inject
    @TemplatePath("CreateWhoAreYouIssueRead.ftl")
    Template createWhoAreYouIssueRead;

    private final GitHubService gitHubService;
    private final ConfigYamlMemberInRedHatLdapService configYamlMemberInRedHatLdapService;
    private final Set<String> ignoreLabelKeys = new HashSet<>(List.of("merge-ok/user-in-ldap", "merge-ok/requires-manual-ldap"));

    @Inject
    public LabelPullRequestForNewMembersService(GitHubService gitHubService, ConfigYamlMemberInRedHatLdapService configYamlMemberInRedHatLdapService) {
        this.gitHubService = gitHubService;
        this.configYamlMemberInRedHatLdapService = configYamlMemberInRedHatLdapService;
    }

    public void run(String organization, String issueRepo, boolean isDryRun, String ldapMembersCsv, String supplementaryCsv, boolean failNoVpn) throws IOException, LdapException, TemplateException {
        GitHub gitHub = gitHubService.getGitHub();
        GHOrganization org = gitHubService.getOrganization(gitHub, organization);
        GHRepository orgRepo = gitHubService.getRepository(org, issueRepo);

        Map<GHPullRequest, List<WhoAreYou>> filteredPullRequests = filterPullRequests(orgRepo, ldapMembersCsv, supplementaryCsv, failNoVpn);
        labelPullRequests(filteredPullRequests, isDryRun);

        logger.info("Finished.");
    }

    private Map<GHPullRequest, List<WhoAreYou>> filterPullRequests(GHRepository orgRepo, String ldapMembersCsv, String supplementaryCsv, boolean failNoVpn) throws IOException, TemplateException, LdapException {
        Map<GHPullRequest, List<WhoAreYou>> answer = new TreeMap<>(Comparator.comparing(GHPullRequest::getNumber));

        List<GHPullRequest> pullRequests = gitHubService.getOpenPullRequests(orgRepo);
        logger.infof("Currently %s open pull-requests", pullRequests.size());

        for (GHPullRequest current : pullRequests) {
            if (isPullRequestConfigChange(current)) {
                Optional<GHLabel> isLabeledToIgnore = getLabel(current.getLabels(), ignoreLabelKeys);
                if (isLabeledToIgnore.isPresent()) {
                    logger.infof("%s has 'merge-ok/user-in-ldap' or 'dont-merge/requires-manual-ldap', ignoring", current.getNumber());
                } else {
                    try {
                        List<WhoAreYou> allMembers = convertMembersToWhoAreYou(configYamlMemberInRedHatLdapService.run(current.getHead().getRepository(), current.getHead().getCommit().getSHA1(), ldapMembersCsv, supplementaryCsv, failNoVpn));
                        answer.put(current, allMembers);
                    } catch (Exception ex) {
                        logger.warnf("Did not find commit from %s - maybe branch has been deleted", current.getNumber());
                        logger.warn("Failure", ex);

                        answer.put(current, Collections.emptyList());
                    }
                }
            } else {
                logger.infof("Pull request %s is not a config.yaml change, ignoring", current.getNumber());
            }
        }

        return answer;
    }

    private Optional<GHLabel> getLabel(Collection<GHLabel> labels, Set<String> keys) {
        return labels.stream().filter(label -> keys.contains(label.getName())).findFirst();
    }

    private List<WhoAreYou> convertMembersToWhoAreYou(List<Members> allMembers) throws IOException {
        List<WhoAreYou> answer = new ArrayList<>();
        for (Members current : allMembers) {
            answer.add(WhoAreYou.from(current));
        }

        return answer;
    }

    private boolean isPullRequestConfigChange(GHPullRequest current) throws IOException {
        boolean answer = false;
        List<GHPullRequestFileDetail> filesChanged = current.listFiles().toList();
        for (GHPullRequestFileDetail file : filesChanged) {
            if (file.getFilename().equalsIgnoreCase("config.yaml")) {
                answer = true;
                break;
            }
        }

        return answer;
    }

    private void labelPullRequests(Map<GHPullRequest, List<WhoAreYou>> filteredPullRequests, boolean isDryRun) throws IOException, TemplateException {
        if (!filteredPullRequests.isEmpty()) {
            logger.infof("There are %s pull-requests that need validating", filteredPullRequests.size());

            for (Map.Entry<GHPullRequest, List<WhoAreYou>> current : filteredPullRequests.entrySet()) {
                GHPullRequest pullRequest = current.getKey();
                List<WhoAreYou> allMembers = current.getValue();

                logger.infof("Working on pull-request %s", pullRequest.getNumber());

                // Something went wrong validating the config.yaml members
                if (allMembers.isEmpty()) {
                    StringWriter stringWriter = new StringWriter();
                    requiresManualLdap.process(new HashMap<>(), stringWriter);

                    if (isDryRun) {
                        logger.warnf("DRY-RUN: Would have labeled 'dont-merge/requires-manual-ldap' pull request %s and added below comment", pullRequest.getNumber());
                        logger.warnf(stringWriter.toString());
                    } else {
                        pullRequest.addLabels("dont-merge/requires-manual-ldap");
                        pullRequest.comment(stringWriter.toString());

                        logger.infof("Labeled (dont-merge/requires-manual-ldap) and commented: %s", pullRequest.getNumber());
                    }
                } else {
                    List<WhoAreYou> unknownMembers = allMembers.stream().filter(member -> member.name().isEmpty()).toList();
                    if (unknownMembers.isEmpty()) {
                        StringWriter stringWriter = new StringWriter();
                        userInLdap.process(new HashMap<>(), stringWriter);

                        if (isDryRun) {
                            logger.warnf("DRY-RUN: Would have labeled 'merge-ok/user-in-ldap' pull request %s and added below comment", pullRequest.getNumber());
                            logger.warnf(stringWriter.toString());
                        } else {
                            pullRequest.removeLabels("dont-merge/cant-find-user-in-ldap");
                            pullRequest.addLabels("merge-ok/user-in-ldap");
                            pullRequest.comment(stringWriter.toString());

                            logger.infof("Labeled (merge-ok/user-in-ldap) and commented: %s", pullRequest.getNumber());
                        }
                    } else {
                        Map<String, Object> root = new HashMap<>();
                        root.put("users", unknownMembers);

                        StringWriter stringWriter = new StringWriter();
                        createWhoAreYouIssueRead.process(root, stringWriter);

                        if (isDryRun) {
                            logger.warnf("DRY-RUN: Would have labeled 'dont-merge/cant-find-user-in-ldap' pull request %s and added below comment", pullRequest.getNumber());
                            logger.warnf(stringWriter.toString());
                        } else {
                            pullRequest.addLabels("dont-merge/cant-find-user-in-ldap");
                            pullRequest.comment(stringWriter.toString());

                            logger.infof("Labeled (dont-merge/cant-find-user-in-ldap) and commented: %s", pullRequest.getNumber());
                        }
                    }
                }
            }
        }
    }
}
