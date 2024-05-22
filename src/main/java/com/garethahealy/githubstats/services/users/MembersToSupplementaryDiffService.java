package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import com.garethahealy.githubstats.services.GitHubService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MembersToSupplementaryDiffService {

    @Inject
    Logger logger;

    private final GitHubService gitHubService;
    private final CsvService csvService;

    @Inject
    public MembersToSupplementaryDiffService(GitHubService gitHubService, CsvService csvService) {
        this.gitHubService = gitHubService;
        this.csvService = csvService;
    }

    public void run(String organization, String membersCsv, String supplementaryCsv) throws IOException {
        GHOrganization org = gitHubService.getOrganization(gitHubService.getGitHub(), organization);
        Map<String, GHUser> members = gitHubService.mapMembers(org);

        List<Members> found = collect(members, membersCsv, supplementaryCsv);
        log(found);

        //csvService.writeSupplementaryCsv("gh-members-cutdown.csv", found, found.isEmpty());
    }

    private List<Members> collect(Map<String, GHUser> members, String membersCsv, String supplementaryCsv) throws IOException {
        List<Members> answer = new ArrayList<>();

        Map<String, Members> knownMembers = csvService.getKnownMembers(membersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        for (Members current : knownMembers.values()) {
            boolean isStillInGithubOrg = members.containsKey(current.getWhatIsYourGitHubUsername());
            if (isStillInGithubOrg) {
                if (!supplementaryMembers.containsKey(current.getWhatIsYourGitHubUsername())) {
                    answer.add(current);
                }
            }
        }

        return answer;
    }

    private void log(List<Members> found) {
        if (!found.isEmpty()) {
            Collections.sort(found);

            StringBuilder emailList = new StringBuilder();
            for (Members current : found) {
                emailList.append(current.getEmailAddress()).append(",");
                logger.infof("%s is not in supplementary list", current.getEmailAddress());
            }

            logger.infof("Email list dump: %s", emailList);
        }
    }
}
