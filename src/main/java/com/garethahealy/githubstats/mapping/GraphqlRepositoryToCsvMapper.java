package com.garethahealy.githubstats.mapping;

import com.garethahealy.githubstats.mapping.fields.*;
import com.garethahealy.githubstats.model.csv.Repository;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryCloneTraffic;
import org.kohsuke.github.GHRepositoryViewTraffic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA, uses = {
    CloneTrafficUniquesMapping.class,
    CommitCountMapping.class,
    ContributorCountMapping.class,
    CopLabelMapping.class,
    HasCodeOwnersMapping.class,
    HasOwnersMapping.class,
    HasRenovateMapping.class,
    HasTravisMapping.class,
    HasWorkflowsMapping.class,
    LastCommitAuthorMapping.class,
    LastCommitDateMapping.class,
    OpenIssueCountMapping.class,
    OpenPullRequestCountMapping.class,
    TopicsCopyMapping.class,
    ViewTrafficUniquesMapping.class
})
public interface GraphqlRepositoryToCsvMapper {

    @Mapping(target = "lastCommitAuthor", source = "graphql", qualifiedByName = "lastCommitAuthor")
    @Mapping(target = "lastCommitDate", source = "graphql", qualifiedByName = "lastCommitDate")
    @Mapping(target = "cop", source = "graphql", qualifiedByName = "copLabel")
    @Mapping(target = "contributorCount", source = "contributors", qualifiedByName = "contributorCount")
    @Mapping(target = "commitCount", source = "graphql", qualifiedByName = "commitCount")
    @Mapping(target = "openIssueCount", source = "graphql", qualifiedByName = "openIssueCount")
    @Mapping(target = "openPullRequestCount", source = "graphql", qualifiedByName = "openPullRequestCount")
    @Mapping(target = "topics", source = "graphql", qualifiedByName = "topicsCopy")
    @Mapping(target = "clonesInPast14Days", source = "cloneTraffic", qualifiedByName = "cloneTrafficUniques")
    @Mapping(target = "viewsInPast14Days", source = "viewTraffic", qualifiedByName = "viewTrafficUniques")
    @Mapping(target = "hasOwners", source = "graphql", qualifiedByName = "hasOwners")
    @Mapping(target = "hasCodeOwners", source = "graphql", qualifiedByName = "hasCodeOwners")
    @Mapping(target = "hasWorkflows", source = "graphql", qualifiedByName = "hasWorkflows")
    @Mapping(target = "hasTravis", source = "graphql", qualifiedByName = "hasTravis")
    @Mapping(target = "hasRenovate", source = "graphql", qualifiedByName = "hasRenovate")
    Repository toCsvRow(
        String repoName,
        com.garethahealy.githubstats.clients.graphql.generated.Repository graphql,
        List<GHRepository.Contributor> contributors,
        GHRepositoryCloneTraffic cloneTraffic,
        GHRepositoryViewTraffic viewTraffic,
        boolean inConfig,
        boolean isArchived,
        boolean inArchivedTeam);
}
