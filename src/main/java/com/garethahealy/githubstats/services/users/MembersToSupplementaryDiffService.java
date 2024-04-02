package com.garethahealy.githubstats.services.users;

import com.garethahealy.githubstats.model.csv.Members;
import com.garethahealy.githubstats.services.CsvService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class MembersToSupplementaryDiffService {

    @Inject
    Logger logger;

    private final CsvService csvService;

    @Inject
    public MembersToSupplementaryDiffService(CsvService csvService) {
        this.csvService = csvService;
    }

    public void run(String membersCsv, String supplementaryCsv) throws IOException {
        Map<String, Members> knownMembers = csvService.getKnownMembers(membersCsv);
        Map<String, Members> supplementaryMembers = csvService.getKnownMembers(supplementaryCsv);

        StringBuilder emailList = new StringBuilder();
        for (Members current : knownMembers.values()) {
            if (!supplementaryMembers.containsKey(current.getWhatIsYourGitHubUsername())) {
                emailList.append(current.getEmailAddress()).append(",");
                logger.infof("%s is not in supplementary list", current.getEmailAddress());
            }
        }

        logger.infof("%s", emailList);
    }
}
