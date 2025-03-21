package com.garethahealy.githubstats.commands.users;

import com.garethahealy.githubstats.commands.BaseCommand;
import com.garethahealy.githubstats.model.users.OrgMember;
import com.garethahealy.githubstats.model.users.OrgMemberRepository;
import com.garethahealy.githubstats.predicates.OrgMemberFilters;
import com.garethahealy.githubstats.services.github.GitHubClient;
import com.garethahealy.githubstats.services.users.utils.OrgMemberCsvService;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class CreateWhoAreYouIssueCommandIT extends BaseCommand {

    private static class runReadWithoutMe {
        public static File LDAP = new File("target/CreateWhoAreYouIssue/ldap-members-runReadWithoutMe.csv");
        public static File SUPPLEMENTARY = new File("target/CreateWhoAreYouIssue/supplementary-runReadWithoutMe.csv");
    }

    private static class runUserMarkedForDeletion {
        public static File LDAP = new File("target/CreateWhoAreYouIssue/ldap-members-runUserMarkedForDeletion.csv");
        public static File SUPPLEMENTARY = new File("target/CreateWhoAreYouIssue/supplementary-runUserMarkedForDeletion.csv");
    }

    void preRunReadWithoutMe() throws IOException {
        System.out.println("-> preRunReadWithoutMe");

        FileUtils.copyFile(new File("ldap-members.csv"), runReadWithoutMe.LDAP);
        FileUtils.copyFile(new File("supplementary.csv"), runReadWithoutMe.SUPPLEMENTARY);

        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository members = csvService.parse(runReadWithoutMe.LDAP);
        members.remove("garethahealy");

        csvService.write(members);
    }

    @Test
    void runReadWithoutMe() throws IOException, InterruptedException, TimeoutException {
        preRunReadWithoutMe();

        System.out.println("-> runReadWithoutMe");

        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "create-who-are-you-issues", "--dry-run=true", "--organization=redhat-cop", "--issue-repo=org", "--ldap-members-csv=" + runReadWithoutMe.LDAP.getPath(), "--supplementary-csv=" + runReadWithoutMe.SUPPLEMENTARY.getPath(), "--fail-if-no-vpn=true", "--permission=read", "--guess=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());
    }

    @Test
    void runAdmin() throws IOException, InterruptedException, TimeoutException {
        System.out.println("-> runAdmin");

        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "create-who-are-you-issues", "--dry-run=true", "--organization=redhat-cop", "--issue-repo=org", "--ldap-members-csv=ldap-members.csv", "--supplementary-csv=supplementary.csv", "--fail-if-no-vpn=true", "--permission=admin", "--guess=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());
    }

    void preRunUserMarkedForDeletion() throws IOException {
        System.out.println("-> preRunUserMarkedForDeletion");

        FileUtils.copyFile(new File("ldap-members.csv"), runUserMarkedForDeletion.LDAP);
        FileUtils.copyFile(new File("supplementary.csv"), runUserMarkedForDeletion.SUPPLEMENTARY);

        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository members = csvService.parse(runUserMarkedForDeletion.LDAP);
        OrgMember me = members.filter(member -> member.gitHubUsername().equalsIgnoreCase("garethahealy")).getFirst();
        members.replace(List.of(me.withDeleteAfter(LocalDate.now().minusDays(1))));

        OrgMember andy = members.filter(member -> member.gitHubUsername().equalsIgnoreCase("sabre1041")).getFirst();
        members.replace(List.of(andy.withDeleteAfter(LocalDate.now().plusDays(1))));

        csvService.write(members);
    }

    @Test
    void runUserMarkedForDeletion() throws IOException, InterruptedException, TimeoutException {
        preRunUserMarkedForDeletion();

        System.out.println("-> runAdmin");

        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "create-who-are-you-issues", "--dry-run=true", "--organization=redhat-cop", "--issue-repo=org", "--ldap-members-csv=" + runUserMarkedForDeletion.LDAP.getPath(), "--supplementary-csv=" + runUserMarkedForDeletion.SUPPLEMENTARY.getPath(), "--fail-if-no-vpn=true", "--permission=admin", "--guess=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());

        postRunUserMarkedForDeletion();
    }

    void postRunUserMarkedForDeletion() throws IOException {
        System.out.println("-> postRunUserMarkedForDeletion");

        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository members = csvService.parse(runUserMarkedForDeletion.LDAP);
        List<OrgMember> markedWithDelete = members.filter(OrgMemberFilters.deleteAfterIsNotNull());

        assertNotNull(markedWithDelete);
        assertEquals(1, markedWithDelete.size());
        assertEquals("sabre1041", markedWithDelete.getFirst().gitHubUsername());
        assertFalse(members.containsKey("garethahealy"));
    }
}
