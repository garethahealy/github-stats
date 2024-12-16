package com.garethahealy.githubstats.model;

import com.garethahealy.githubstats.model.csv.Members;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.kohsuke.github.GHUser;

import java.io.IOException;

@RegisterForReflection
public record WhoAreYou(String name, String username, String repo, GHUser ghUser) implements Comparable<WhoAreYou> {

    @Override
    public int compareTo(WhoAreYou o) {
        return new CompareToBuilder().append(this.username().toLowerCase(), o.username().toLowerCase()).toComparison();
    }

    public static WhoAreYou from(GHUser member, String repo) throws IOException {
        return new WhoAreYou(member.getName(), member.getLogin(), repo, member);
    }

    public static WhoAreYou from(Members member) throws IOException {
        return new WhoAreYou(member.getEmailAddress(), member.getWhatIsYourGitHubUsername(), null, null);
    }
}
