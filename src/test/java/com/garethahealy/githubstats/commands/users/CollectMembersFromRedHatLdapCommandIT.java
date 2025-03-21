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
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class CollectMembersFromRedHatLdapCommandIT extends BaseCommand {

    private static class runWithLimit {
        public static File LDAP = new File("target/CollectMembersFromRedHatLdap/ldap-members-runWithLimit.csv");
        public static File SUPPLEMENTARY = new File("target/CollectMembersFromRedHatLdap/supplementary-runWithLimit.csv");
    }

    private static class runValidateMovingMembers {
        public static File LDAP = new File("target/CollectMembersFromRedHatLdap/ldap-members-runValidateMovingMembers.csv");
        public static File SUPPLEMENTARY = new File("target/CollectMembersFromRedHatLdap/supplementary-runValidateMovingMembers.csv");
    }

    private static class run {
        public static File LDAP = new File("target/CollectMembersFromRedHatLdap/ldap-members-runValidateMovingMembers.csv");
        public static File SUPPLEMENTARY = new File("target/CollectMembersFromRedHatLdap/supplementary-runValidateMovingMembers.csv");
    }

    void preRunWithLimit() throws IOException {
        System.out.println("-> preRunWithLimit");

        FileUtils.copyFile(new File("supplementary.csv"), runWithLimit.SUPPLEMENTARY);
    }

    /**
     * Collect only 5 members but using an empty LDAP input to start from and validate CSVs
     */
    @Test
    void runWithLimit() throws IOException, InterruptedException, TimeoutException {
        preRunWithLimit();

        System.out.println("-> runWithLimit");

        // Pre-Test Data
        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "collect-members-from-ldap", "--organization=redhat-cop", "--ldap-members-csv=" + runWithLimit.LDAP.getPath(), "--supplementary-csv=" + runWithLimit.SUPPLEMENTARY.getPath(), "--user-limit=5", "--fail-if-no-vpn=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        // Test
        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());

        postRunWithLimit();
    }

    void postRunWithLimit() throws IOException {
        System.out.println("-> postRunWithLimit");

        // Validate outputs
        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository ldapOutput = csvService.parse(runWithLimit.LDAP);
        OrgMemberRepository supplementaryOutput = csvService.parse(runWithLimit.SUPPLEMENTARY);

        assertNotNull(ldapOutput);
        assertEquals(5, ldapOutput.size());
        assertTrue(ldapOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());

        assertNotNull(supplementaryOutput);
        assertEquals(21, supplementaryOutput.size());
        assertTrue(supplementaryOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());
    }

    OrgMemberRepository preRunValidateMovingMembers() throws IOException {
        System.out.println("-> preRunValidateMovingMembers");

        FileUtils.copyFile(new File("ldap-members.csv"), runValidateMovingMembers.LDAP);
        FileUtils.copyFile(new File("supplementary.csv"), runValidateMovingMembers.SUPPLEMENTARY);

        // Pre-Test Data
        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository ldapInput = csvService.parse(runValidateMovingMembers.LDAP);
        OrgMemberRepository supplementaryInput = csvService.parse(runValidateMovingMembers.SUPPLEMENTARY);

        OrgMember me = ldapInput.filter(member -> member.gitHubUsername().equalsIgnoreCase("garethahealy")).getFirst();
        ldapInput.remove(me);
        supplementaryInput.put(me);

        csvService.write(ldapInput);
        csvService.write(supplementaryInput);

        return ldapInput;
    }

    /**
     * Validate a member in the supplementary can be moved to the LDAP CSV
     */
    @Test
    void runValidateMovingMembers() throws IOException, InterruptedException, TimeoutException {
        OrgMemberRepository ldapInput = preRunValidateMovingMembers();

        System.out.println("-> runValidateMovingMembers");

        // Test
        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "collect-members-from-ldap", "--organization=redhat-cop", "--ldap-members-csv=" + runValidateMovingMembers.LDAP.getPath(), "--supplementary-csv=" + runValidateMovingMembers.SUPPLEMENTARY.getPath(), "--validate-csv=true", "--fail-if-no-vpn=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());

        postRunValidateMovingMembers(ldapInput);
    }

    void postRunValidateMovingMembers(OrgMemberRepository ldapInput) throws IOException {
        System.out.println("-> postRunValidateMovingMembers");

        // Validate outputs
        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository ldapOutput = csvService.parse(runValidateMovingMembers.LDAP);
        OrgMemberRepository supplementaryOutput = csvService.parse(runValidateMovingMembers.SUPPLEMENTARY);

        assertNotNull(ldapOutput);
        assertEquals(ldapInput.size() + 1, ldapOutput.size());
        assertTrue(ldapOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());

        assertNotNull(supplementaryOutput);
        assertEquals(21, supplementaryOutput.size());
        assertTrue(supplementaryOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());
    }

    void preRun() throws IOException {
        System.out.println("-> run");

        FileUtils.copyFile(new File("ldap-members.csv"), run.LDAP);
        FileUtils.copyFile(new File("supplementary.csv"), run.SUPPLEMENTARY);
    }

    /**
     * Collect with the current CSVs - tests the daily run
     */
    @Test
    void run() throws IOException, InterruptedException, TimeoutException {
        preRun();

        System.out.println("-> run");

        // Test
        ProcessExecutor executor = new ProcessExecutor()
                .command(getRunner(), "users", "collect-members-from-ldap", "--organization=redhat-cop", "--ldap-members-csv=" + run.LDAP.getPath(), "--supplementary-csv=" + run.SUPPLEMENTARY.getPath(), "--fail-if-no-vpn=true")
                .redirectError(System.err)
                .redirectOutput(System.out);

        String command = String.join(" ", executor.getCommand());
        System.out.println("Executing \"" + command + "\"");

        ProcessResult result = executor.execute();

        assertEquals(0, result.getExitValue());

        postRun();
    }

    void postRun() throws IOException {
        System.out.println("-> postRun");

        // Validate outputs
        GitHubClient client = new GitHubClient();
        OrgMemberCsvService csvService = new OrgMemberCsvService(Logger.getLogger(OrgMemberCsvService.class), client.getClient());
        OrgMemberRepository ldapOutput = csvService.parse(run.LDAP);
        OrgMemberRepository supplementaryOutput = csvService.parse(run.SUPPLEMENTARY);

        assertNotNull(ldapOutput);
        assertTrue(ldapOutput.size() > 200);
        assertTrue(ldapOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());

        assertNotNull(supplementaryOutput);
        assertEquals(21, supplementaryOutput.size());
        assertTrue(supplementaryOutput.filter(OrgMemberFilters.deleteAfterIsNotNull()).isEmpty());
    }
}
