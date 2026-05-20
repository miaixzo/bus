/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.models.Constants.*;
import org.miaixz.bus.gitlab.models.ImportStatus.Status;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The project class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852270183312L;

    private List<SharedGroup> sharedWithGroups;
    private Integer approvalsBeforeMerge;
    private Boolean archived;
    private String avatarUrl;
    private Boolean containerRegistryEnabled;
    private Date createdAt;
    private Long creatorId;
    private String defaultBranch;
    private String description;
    private Integer forksCount;
    private Project forkedFromProject;
    private String httpUrlToRepo;
    private Long id;
    private Boolean isPublic;
    private Boolean issuesEnabled;
    private Boolean jobsEnabled;
    private Date lastActivityAt;
    private Boolean lfsEnabled;
    private MergeMethod mergeMethod;
    private Boolean mergeRequestsEnabled;
    private String name;
    private Namespace namespace;
    private String nameWithNamespace;
    private Boolean onlyAllowMergeIfPipelineSucceeds;
    private Boolean allowMergeOnSkippedPipeline;
    private Boolean onlyAllowMergeIfAllDiscussionsAreResolved;
    private Integer openIssuesCount;
    private Owner owner;
    private String path;
    private String pathWithNamespace;
    private Permissions permissions;
    private Boolean publicJobs;
    private String repositoryStorage;
    private Boolean requestAccessEnabled;
    private String runnersToken;
    private Boolean sharedRunnersEnabled;
    private BuildGitStrategy buildGitStrategy;
    private Boolean snippetsEnabled;
    private String sshUrlToRepo;
    private Integer starCount;

    private List<String> tagList;
    private List<String> topics;
    private Integer visibilityLevel;
    private Visibility visibility;
    private Boolean wallEnabled;
    private String webUrl;
    private Boolean wikiEnabled;
    private Boolean printingMergeRequestLinkEnabled;
    private Boolean resolveOutdatedDiffDiscussions;
    private ProjectStatistics statistics;
    private Boolean initializeWithReadme;
    private Boolean packagesEnabled;
    private Boolean emptyRepo;
    private String licenseUrl;
    private ProjectLicense license;
    private List<CustomAttribute> customAttributes;
    private String buildCoverageRegex;
    private Status importStatus;
    private String readmeUrl;
    private Boolean canCreateMergeRequestIn;
    private AutoDevopsDeployStrategy autoDevopsDeployStrategy;
    private Integer ciDefaultGitDepth;
    private Boolean ciForwardDeploymentEnabled;
    private String ciConfigPath;
    private Boolean removeSourceBranchAfterMerge;
    private Boolean autoDevopsEnabled;
    private SquashOption squashOption;
    private Boolean autocloseReferencedIssues;
    private Boolean emailsDisabled;
    private String suggestionCommitMessage;
    private String mergeRequestsTemplate;
    private String mergeCommitTemplate;
    private String squashCommitTemplate;
    private String issueBranchTemplate;
    private Boolean useCustomTemplate;
    private String issuesTemplate;
    private String externalAuthorizationClassificationLabel;
    private Boolean groupRunnersEnabled;
    private Boolean showDefaultAwardEmojis;
    private Boolean warnAboutPotentiallyUnwantedCharacters;
    private Boolean mirrorTriggerBuilds;
    private AutoCancelPendingPipelines autoCancelPendingPipelines;
    private String repositoryObjectFormat;
    private Boolean onlyAllowMergeIfAllStatusChecksPassed;
    private Integer groupWithProjectTemplatesId;
    private Boolean publicBuilds;
    private Integer buildTimeout;
    private String templateName;
    private Boolean emailsEnabled;
    private Boolean mirror;
    private Date updatedAt;
    private String descriptionHtml;
    private String containerRegistryImagePrefix;
    private ContainerExpirationPolicy containerExpirationPolicy;
    private Boolean serviceDeskEnabled;
    private String importUrl;
    private String importType;
    private String importError;
    private Boolean ciForwardDeploymentRollbackAllowed;
    private Boolean ciAllowForkPipelinesToRunInParentProject;
    private List<String> ciIdTokenSubClaimComponents;
    private Boolean ciJobTokenScopeEnabled;
    private Boolean ciSeparatedCaches;
    private String ciRestrictPipelineCancellationRole;
    private String ciPipelineVariablesMinimumOverrideRole;
    private Boolean ciPushRepositoryForJobTokenAllowed;
    private Boolean allowPipelineTriggerApproveDeployment;
    private Boolean restrictUserDefinedVariables;
    private Boolean enforceAuthChecksOnUploads;
    private Boolean keepLatestArtifact;
    private Integer runnerTokenExpirationInterval;
    private Boolean requirementsEnabled;
    private Boolean securityAndComplianceEnabled;
    private Boolean secretPushProtectionEnabled;
    private List<String> complianceFrameworks;
    private ProjectFeatureVisibilityAccessLevel analyticsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel buildsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel containerRegistryAccessLevel;
    private ProjectFeatureVisibilityAccessLevel environmentsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel featureFlagsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel forkingAccessLevel;
    private ProjectFeatureVisibilityAccessLevel infrastructureAccessLevel;
    private ProjectFeatureVisibilityAccessLevel issuesAccessLevel;
    private ProjectFeatureVisibilityAccessLevel mergeRequestsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel modelExperimentsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel modelRegistryAccessLevel;
    private ProjectFeatureVisibilityAccessLevel monitorAccessLevel;
    private ProjectFeatureVisibilityAccessLevel pagesAccessLevel;
    private ProjectFeatureVisibilityAccessLevel releasesAccessLevel;
    private ProjectFeatureVisibilityAccessLevel repositoryAccessLevel;
    private ProjectFeatureVisibilityAccessLevel requirementsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel securityAndComplianceAccessLevel;
    private ProjectFeatureVisibilityAccessLevel snippetsAccessLevel;
    private ProjectFeatureVisibilityAccessLevel wikiAccessLevel;

    /**
     * Returns whether the valid is enabled.
     *
     * @param project the project value
     * @return the result
     */

    public static boolean isValid(Project project) {
        return (project != null && project.getId() != null);
    }

    @JsonProperty("_links")
    private Map<String, String> links;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date markedForDeletionOn;

    /**
     * Returns the approvals before merge.
     *
     * @return the result
     */

    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    /**
     * Sets the approvals before merge.
     *
     * @param approvalsBeforeMerge the approvals before merge value
     */

    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    /**
     * Formats a fully qualified project path based on the provided namespace and project path.
     *
     * @param namespace the namespace, either a user name or group name
     * @param path      the project path
     * @return a fully qualified project path based on the provided namespace and project path
     */
    public static final String getPathWithNammespace(String namespace, String path) {
        return (namespace.trim() + "/" + path.trim());
    }

    /**
     * Returns the archived.
     *
     * @return the result
     */

    public Boolean getArchived() {
        return archived;
    }

    /**
     * Sets the archived.
     *
     * @param archived the archived value
     */

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    /**
     * Returns the avatar url.
     *
     * @return the result
     */

    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the avatar url.
     *
     * @param avatarUrl the avatar url value
     */

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Returns the container registry enabled.
     *
     * @return the result
     */

    public Boolean getContainerRegistryEnabled() {
        return containerRegistryEnabled;
    }

    /**
     * Sets the container registry enabled.
     *
     * @param containerRegistryEnabled the container registry enabled value
     */

    public void setContainerRegistryEnabled(Boolean containerRegistryEnabled) {
        this.containerRegistryEnabled = containerRegistryEnabled;
    }

    /**
     * Sets the approvals before merge and returns this instance.
     *
     * @param approvalsBeforeMerge the approvals before merge value
     * @return the result
     */

    public Project withApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
        return this;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the creator id.
     *
     * @return the result
     */

    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the creator id.
     *
     * @param creatorId the creator id value
     */

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Returns the default branch.
     *
     * @return the result
     */

    public String getDefaultBranch() {
        return defaultBranch;
    }

    /**
     * Sets the default branch.
     *
     * @param defaultBranch the default branch value
     */

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    /**
     * Sets the container registry enabled and returns this instance.
     *
     * @param containerRegistryEnabled the container registry enabled value
     * @return the result
     */

    public Project withContainerRegistryEnabled(Boolean containerRegistryEnabled) {
        this.containerRegistryEnabled = containerRegistryEnabled;
        return this;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the default branch and returns this instance.
     *
     * @param defaultBranch the default branch value
     * @return the result
     */

    public Project withDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
        return this;
    }

    /**
     * Returns the forks count.
     *
     * @return the result
     */

    public Integer getForksCount() {
        return forksCount;
    }

    /**
     * Sets the forks count.
     *
     * @param forksCount the forks count value
     */

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    /**
     * Returns the forked from project.
     *
     * @return the result
     */

    public Project getForkedFromProject() {
        return forkedFromProject;
    }

    /**
     * Sets the forked from project.
     *
     * @param forkedFromProject the forked from project value
     */

    public void setForkedFromProject(Project forkedFromProject) {
        this.forkedFromProject = forkedFromProject;
    }

    /**
     * Returns the http url to repo.
     *
     * @return the result
     */

    public String getHttpUrlToRepo() {
        return httpUrlToRepo;
    }

    /**
     * Sets the http url to repo.
     *
     * @param httpUrlToRepo the http url to repo value
     */

    public void setHttpUrlToRepo(String httpUrlToRepo) {
        this.httpUrlToRepo = httpUrlToRepo;
    }

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public Project withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the issues enabled.
     *
     * @return the result
     */

    public Boolean getIssuesEnabled() {
        return issuesEnabled;
    }

    /**
     * Sets the issues enabled.
     *
     * @param issuesEnabled the issues enabled value
     */

    public void setIssuesEnabled(Boolean issuesEnabled) {
        this.issuesEnabled = issuesEnabled;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public Project withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the jobs enabled.
     *
     * @return the result
     */

    public Boolean getJobsEnabled() {
        return jobsEnabled;
    }

    /**
     * Sets the jobs enabled.
     *
     * @param jobsEnabled the jobs enabled value
     */

    public void setJobsEnabled(Boolean jobsEnabled) {
        this.jobsEnabled = jobsEnabled;
    }

    /**
     * Sets the issues enabled and returns this instance.
     *
     * @param issuesEnabled the issues enabled value
     * @return the result
     */

    public Project withIssuesEnabled(Boolean issuesEnabled) {
        this.issuesEnabled = issuesEnabled;
        return this;
    }

    /**
     * Returns the last activity at.
     *
     * @return the result
     */

    public Date getLastActivityAt() {
        return lastActivityAt;
    }

    /**
     * Sets the last activity at.
     *
     * @param lastActivityAt the last activity at value
     */

    public void setLastActivityAt(Date lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    /**
     * Returns the lfs enabled.
     *
     * @return the result
     */

    public Boolean getLfsEnabled() {
        return lfsEnabled;
    }

    /**
     * Sets the lfs enabled.
     *
     * @param lfsEnabled the lfs enabled value
     */

    public void setLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
    }

    /**
     * Sets the jobs enabled and returns this instance.
     *
     * @param jobsEnabled the jobs enabled value
     * @return the result
     */

    public Project withJobsEnabled(Boolean jobsEnabled) {
        this.jobsEnabled = jobsEnabled;
        return this;
    }

    /**
     * Returns the merge method.
     *
     * @return the result
     */

    public MergeMethod getMergeMethod() {
        return mergeMethod;
    }

    /**
     * Sets the merge method.
     *
     * @param mergeMethod the merge method value
     */

    public void setMergeMethod(MergeMethod mergeMethod) {
        this.mergeMethod = mergeMethod;
    }

    /**
     * Sets the lfs enabled and returns this instance.
     *
     * @param lfsEnabled the lfs enabled value
     * @return the result
     */

    public Project withLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
        return this;
    }

    /**
     * Returns the merge requests enabled.
     *
     * @return the result
     */

    public Boolean getMergeRequestsEnabled() {
        return mergeRequestsEnabled;
    }

    /**
     * Sets the merge requests enabled.
     *
     * @param mergeRequestsEnabled the merge requests enabled value
     */

    public void setMergeRequestsEnabled(Boolean mergeRequestsEnabled) {
        this.mergeRequestsEnabled = mergeRequestsEnabled;
    }

    /**
     * Sets the merge method and returns this instance.
     *
     * @param mergeMethod the merge method value
     * @return the result
     */

    public Project withMergeMethod(MergeMethod mergeMethod) {
        this.mergeMethod = mergeMethod;
        return this;
    }

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the merge requests enabled and returns this instance.
     *
     * @param mergeRequestsEnabled the merge requests enabled value
     * @return the result
     */

    public Project withMergeRequestsEnabled(Boolean mergeRequestsEnabled) {
        this.mergeRequestsEnabled = mergeRequestsEnabled;
        return this;
    }

    /**
     * Returns the namespace.
     *
     * @return the result
     */

    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     *
     * @param namespace the namespace value
     */

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Project withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the namespace and returns this instance.
     *
     * @param namespace the namespace value
     * @return the result
     */

    public Project withNamespace(Namespace namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Returns the name with namespace.
     *
     * @return the result
     */

    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    /**
     * Sets the name with namespace.
     *
     * @param nameWithNamespace the name with namespace value
     */

    public void setNameWithNamespace(String nameWithNamespace) {
        this.nameWithNamespace = nameWithNamespace;
    }

    /**
     * Returns the only allow merge if pipeline succeeds.
     *
     * @return the result
     */

    public Boolean getOnlyAllowMergeIfPipelineSucceeds() {
        return onlyAllowMergeIfPipelineSucceeds;
    }

    /**
     * Sets the only allow merge if pipeline succeeds.
     *
     * @param onlyAllowMergeIfPipelineSucceeds the only allow merge if pipeline succeeds value
     */

    public void setOnlyAllowMergeIfPipelineSucceeds(Boolean onlyAllowMergeIfPipelineSucceeds) {
        this.onlyAllowMergeIfPipelineSucceeds = onlyAllowMergeIfPipelineSucceeds;
    }

    /**
     * Sets the namespace id and returns this instance.
     *
     * @param namespaceId the namespace id value
     * @return the result
     */

    public Project withNamespaceId(long namespaceId) {
        this.namespace = new Namespace();
        this.namespace.setId(namespaceId);
        return this;
    }

    /**
     * Returns the allow merge on skipped pipeline.
     *
     * @return the result
     */

    public Boolean getAllowMergeOnSkippedPipeline() {
        return allowMergeOnSkippedPipeline;
    }

    /**
     * Sets the allow merge on skipped pipeline.
     *
     * @param allowMergeOnSkippedPipeline the allow merge on skipped pipeline value
     */

    public void setAllowMergeOnSkippedPipeline(Boolean allowMergeOnSkippedPipeline) {
        this.allowMergeOnSkippedPipeline = allowMergeOnSkippedPipeline;
    }

    /**
     * Sets the only allow merge if pipeline succeeds and returns this instance.
     *
     * @param onlyAllowMergeIfPipelineSucceeds the only allow merge if pipeline succeeds value
     * @return the result
     */

    public Project withOnlyAllowMergeIfPipelineSucceeds(Boolean onlyAllowMergeIfPipelineSucceeds) {
        this.onlyAllowMergeIfPipelineSucceeds = onlyAllowMergeIfPipelineSucceeds;
        return this;
    }

    /**
     * Returns the only allow merge if all discussions are resolved.
     *
     * @return the result
     */

    public Boolean getOnlyAllowMergeIfAllDiscussionsAreResolved() {
        return onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    /**
     * Sets the only allow merge if all discussions are resolved.
     *
     * @param onlyAllowMergeIfAllDiscussionsAreResolved the only allow merge if all discussions are resolved value
     */

    public void setOnlyAllowMergeIfAllDiscussionsAreResolved(Boolean onlyAllowMergeIfAllDiscussionsAreResolved) {
        this.onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    /**
     * Sets the allow merge on skipped pipeline and returns this instance.
     *
     * @param allowMergeOnSkippedPipeline the allow merge on skipped pipeline value
     * @return the result
     */

    public Project withAllowMergeOnSkippedPipeline(Boolean allowMergeOnSkippedPipeline) {
        this.allowMergeOnSkippedPipeline = allowMergeOnSkippedPipeline;
        return this;
    }

    /**
     * Returns the open issues count.
     *
     * @return the result
     */

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    /**
     * Sets the open issues count.
     *
     * @param openIssuesCount the open issues count value
     */

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    /**
     * Returns the owner.
     *
     * @return the result
     */

    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param owner the owner value
     */

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the path value
     */

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the only allow merge if all discussions are resolved and returns this instance.
     *
     * @param onlyAllowMergeIfAllDiscussionsAreResolved the only allow merge if all discussions are resolved value
     * @return the result
     */

    public Project withOnlyAllowMergeIfAllDiscussionsAreResolved(Boolean onlyAllowMergeIfAllDiscussionsAreResolved) {
        this.onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved;
        return this;
    }

    /**
     * Returns the path with namespace.
     *
     * @return the result
     */

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    /**
     * Sets the path with namespace.
     *
     * @param pathWithNamespace the path with namespace value
     */

    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    /**
     * Returns the permissions.
     *
     * @return the result
     */

    public Permissions getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the permissions value
     */

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    /**
     * Returns the public.
     *
     * @return the result
     */

    public Boolean getPublic() {
        return isPublic;
    }

    /**
     * Sets the public.
     *
     * @param isPublic the is public value
     */

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Sets the path and returns this instance.
     *
     * @param path the path value
     * @return the result
     */

    public Project withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Returns the public jobs.
     *
     * @return the result
     */

    public Boolean getPublicJobs() {
        return publicJobs;
    }

    /**
     * Sets the public jobs.
     *
     * @param publicJobs the public jobs value
     */

    public void setPublicJobs(Boolean publicJobs) {
        this.publicJobs = publicJobs;
    }

    /**
     * Sets the public and returns this instance.
     *
     * @param isPublic the is public value
     * @return the result
     */

    public Project withPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    /**
     * Returns the repository storage.
     *
     * @return the result
     */

    public String getRepositoryStorage() {
        return repositoryStorage;
    }

    /**
     * Sets the repository storage.
     *
     * @param repositoryStorage the repository storage value
     */

    public void setRepositoryStorage(String repositoryStorage) {
        this.repositoryStorage = repositoryStorage;
    }

    /**
     * Sets the public jobs and returns this instance.
     *
     * @param publicJobs the public jobs value
     * @return the result
     */

    public Project withPublicJobs(Boolean publicJobs) {
        this.publicJobs = publicJobs;
        return this;
    }

    /**
     * Returns the request access enabled.
     *
     * @return the result
     */

    public Boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    /**
     * Sets the request access enabled.
     *
     * @param request_access_enabled the request access enabled value
     */

    public void setRequestAccessEnabled(Boolean request_access_enabled) {
        this.requestAccessEnabled = request_access_enabled;
    }

    /**
     * Sets the repository storage and returns this instance.
     *
     * @param repositoryStorage the repository storage value
     * @return the result
     */

    public Project withRepositoryStorage(String repositoryStorage) {
        this.repositoryStorage = repositoryStorage;
        return this;
    }

    /**
     * Returns the runners token.
     *
     * @return the result
     */

    public String getRunnersToken() {
        return runnersToken;
    }

    /**
     * Sets the runners token.
     *
     * @param runnersToken the runners token value
     */

    public void setRunnersToken(String runnersToken) {
        this.runnersToken = runnersToken;
    }

    /**
     * Returns the shared runners enabled.
     *
     * @return the result
     */

    public Boolean getSharedRunnersEnabled() {
        return sharedRunnersEnabled;
    }

    /**
     * Sets the shared runners enabled.
     *
     * @param sharedRunnersEnabled the shared runners enabled value
     */

    public void setSharedRunnersEnabled(Boolean sharedRunnersEnabled) {
        this.sharedRunnersEnabled = sharedRunnersEnabled;
    }

    /**
     * Returns the shared with groups.
     *
     * @return the result
     */

    public List<SharedGroup> getSharedWithGroups() {
        return sharedWithGroups;
    }

    /**
     * Sets the shared with groups.
     *
     * @param sharedWithGroups the shared with groups value
     */

    public void setSharedWithGroups(List<SharedGroup> sharedWithGroups) {
        this.sharedWithGroups = sharedWithGroups;
    }

    /**
     * Sets the request access enabled and returns this instance.
     *
     * @param requestAccessEnabled the request access enabled value
     * @return the result
     */

    public Project withRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
        return this;
    }

    /**
     * Returns the snippets enabled.
     *
     * @return the result
     */

    public Boolean getSnippetsEnabled() {
        return snippetsEnabled;
    }

    /**
     * Sets the snippets enabled.
     *
     * @param snippetsEnabled the snippets enabled value
     */

    public void setSnippetsEnabled(Boolean snippetsEnabled) {
        this.snippetsEnabled = snippetsEnabled;
    }

    /**
     * Sets the shared runners enabled and returns this instance.
     *
     * @param sharedRunnersEnabled the shared runners enabled value
     * @return the result
     */

    public Project withSharedRunnersEnabled(Boolean sharedRunnersEnabled) {
        this.sharedRunnersEnabled = sharedRunnersEnabled;
        return this;
    }

    /**
     * Returns the ssh url to repo.
     *
     * @return the result
     */

    public String getSshUrlToRepo() {
        return sshUrlToRepo;
    }

    /**
     * Sets the ssh url to repo.
     *
     * @param sshUrlToRepo the ssh url to repo value
     */

    public void setSshUrlToRepo(String sshUrlToRepo) {
        this.sshUrlToRepo = sshUrlToRepo;
    }

    /**
     * Returns the star count.
     *
     * @return the result
     */

    public Integer getStarCount() {
        return starCount;
    }

    /**
     * Sets the star count.
     *
     * @param starCount the star count value
     */

    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    /**
     * Sets the snippets enabled and returns this instance.
     *
     * @param snippetsEnabled the snippets enabled value
     * @return the result
     */

    public Project withSnippetsEnabled(Boolean snippetsEnabled) {
        this.snippetsEnabled = snippetsEnabled;
        return this;
    }

    /**
     * Returns the topics.
     *
     * @return the result
     */

    public List<String> getTopics() {
        return topics;
    }

    /**
     * Sets the topics.
     *
     * @param topics the topics value
     */

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    /**
     * Returns the visibility.
     *
     * @return the result
     */

    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility.
     *
     * @param visibility the visibility value
     */

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * Sets the topics and returns this instance.
     *
     * @param topics the topics value
     * @return the result
     */

    public Project withTopics(List<String> topics) {
        this.topics = topics;
        return this;
    }

    /**
     * Returns the visibility level.
     *
     * @return the result
     */

    public Integer getVisibilityLevel() {
        return visibilityLevel;
    }

    /**
     * Sets the visibility level.
     *
     * @param visibilityLevel the visibility level value
     */

    public void setVisibilityLevel(Integer visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    /**
     * Sets the visibility and returns this instance.
     *
     * @param visibility the visibility value
     * @return the result
     */

    public Project withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * Returns the wall enabled.
     *
     * @return the result
     */

    public Boolean getWallEnabled() {
        return wallEnabled;
    }

    /**
     * Sets the wall enabled.
     *
     * @param wallEnabled the wall enabled value
     */

    public void setWallEnabled(Boolean wallEnabled) {
        this.wallEnabled = wallEnabled;
    }

    /**
     * Sets the visibility level and returns this instance.
     *
     * @param visibilityLevel the visibility level value
     * @return the result
     */

    public Project withVisibilityLevel(Integer visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
        return this;
    }

    /**
     * Returns the web url.
     *
     * @return the result
     */

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Sets the web url.
     *
     * @param webUrl the web url value
     */

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * Sets the wall enabled and returns this instance.
     *
     * @param wallEnabled the wall enabled value
     * @return the result
     */

    public Project withWallEnabled(Boolean wallEnabled) {
        this.wallEnabled = wallEnabled;
        return this;
    }

    /**
     * Returns the wiki enabled.
     *
     * @return the result
     */

    public Boolean getWikiEnabled() {
        return wikiEnabled;
    }

    /**
     * Sets the wiki enabled.
     *
     * @param wikiEnabled the wiki enabled value
     */

    public void setWikiEnabled(Boolean wikiEnabled) {
        this.wikiEnabled = wikiEnabled;
    }

    /**
     * Sets the web url and returns this instance.
     *
     * @param webUrl the web url value
     * @return the result
     */

    public Project withWebUrl(String webUrl) {
        this.webUrl = webUrl;
        return this;
    }

    /**
     * Returns the printing merge request link enabled.
     *
     * @return the result
     */

    public Boolean getPrintingMergeRequestLinkEnabled() {
        return printingMergeRequestLinkEnabled;
    }

    /**
     * Sets the printing merge request link enabled.
     *
     * @param printingMergeRequestLinkEnabled the printing merge request link enabled value
     */

    public void setPrintingMergeRequestLinkEnabled(Boolean printingMergeRequestLinkEnabled) {
        this.printingMergeRequestLinkEnabled = printingMergeRequestLinkEnabled;
    }

    /**
     * Sets the wiki enabled and returns this instance.
     *
     * @param wikiEnabled the wiki enabled value
     * @return the result
     */

    public Project withWikiEnabled(Boolean wikiEnabled) {
        this.wikiEnabled = wikiEnabled;
        return this;
    }

    /**
     * Returns the resolve outdated diff discussions.
     *
     * @return the result
     */

    public Boolean getResolveOutdatedDiffDiscussions() {
        return resolveOutdatedDiffDiscussions;
    }

    /**
     * Sets the resolve outdated diff discussions.
     *
     * @param resolveOutdatedDiffDiscussions the resolve outdated diff discussions value
     */

    public void setResolveOutdatedDiffDiscussions(Boolean resolveOutdatedDiffDiscussions) {
        this.resolveOutdatedDiffDiscussions = resolveOutdatedDiffDiscussions;
    }

    /**
     * Sets the printing merge request link enabled and returns this instance.
     *
     * @param printingMergeRequestLinkEnabled the printing merge request link enabled value
     * @return the result
     */

    public Project withPrintingMergeRequestLinkEnabled(Boolean printingMergeRequestLinkEnabled) {
        this.printingMergeRequestLinkEnabled = printingMergeRequestLinkEnabled;
        return this;
    }

    /**
     * Returns the initialize with readme.
     *
     * @return the result
     */

    public Boolean getInitializeWithReadme() {
        return initializeWithReadme;
    }

    /**
     * Sets the initialize with readme.
     *
     * @param initializeWithReadme the initialize with readme value
     */

    public void setInitializeWithReadme(Boolean initializeWithReadme) {
        this.initializeWithReadme = initializeWithReadme;
    }

    /**
     * Sets the resolve outdated diff discussions and returns this instance.
     *
     * @param resolveOutdatedDiffDiscussions the resolve outdated diff discussions value
     * @return the result
     */

    public Project withResolveOutdatedDiffDiscussions(Boolean resolveOutdatedDiffDiscussions) {
        this.resolveOutdatedDiffDiscussions = resolveOutdatedDiffDiscussions;
        return this;
    }

    /**
     * Returns the packages enabled.
     *
     * @return the result
     */

    public Boolean getPackagesEnabled() {
        return packagesEnabled;
    }

    /**
     * Sets the packages enabled.
     *
     * @param packagesEnabled the packages enabled value
     */

    public void setPackagesEnabled(Boolean packagesEnabled) {
        this.packagesEnabled = packagesEnabled;
    }

    /**
     * Sets the initialize with readme and returns this instance.
     *
     * @param initializeWithReadme the initialize with readme value
     * @return the result
     */

    public Project withInitializeWithReadme(Boolean initializeWithReadme) {
        this.initializeWithReadme = initializeWithReadme;
        return this;
    }

    /**
     * Returns the statistics.
     *
     * @return the result
     */

    public ProjectStatistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the statistics.
     *
     * @param statistics the statistics value
     */

    public void setStatistics(ProjectStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the empty repo.
     *
     * @return the result
     */

    public Boolean getEmptyRepo() {
        return emptyRepo;
    }

    /**
     * Sets the empty repo.
     *
     * @param emptyRepo the empty repo value
     */

    public void setEmptyRepo(Boolean emptyRepo) {
        this.emptyRepo = emptyRepo;
    }

    /**
     * Returns the marked for deletion on.
     *
     * @return the result
     */

    public Date getMarkedForDeletionOn() {
        return markedForDeletionOn;
    }

    /**
     * Sets the marked for deletion on.
     *
     * @param markedForDeletionOn the marked for deletion on value
     */

    public void setMarkedForDeletionOn(Date markedForDeletionOn) {
        this.markedForDeletionOn = markedForDeletionOn;
    }

    /**
     * Returns the license url.
     *
     * @return the result
     */

    public String getLicenseUrl() {
        return licenseUrl;
    }

    /**
     * Sets the license url.
     *
     * @param licenseUrl the license url value
     */

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    /**
     * Returns the license.
     *
     * @return the result
     */

    public ProjectLicense getLicense() {
        return license;
    }

    /**
     * Sets the license.
     *
     * @param license the license value
     */

    public void setLicense(ProjectLicense license) {
        this.license = license;
    }

    /**
     * Returns the custom attributes.
     *
     * @return the result
     */

    public List<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    /**
     * Sets the custom attributes.
     *
     * @param customAttributes the custom attributes value
     */

    public void setCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    /**
     * Sets the packages enabled and returns this instance.
     *
     * @param packagesEnabled the packages enabled value
     * @return the result
     */

    public Project withPackagesEnabled(Boolean packagesEnabled) {
        this.packagesEnabled = packagesEnabled;
        return this;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * Returns the build git strategy.
     *
     * @return the result
     */

    public BuildGitStrategy getBuildGitStrategy() {
        return buildGitStrategy;
    }

    /**
     * Returns the build coverage regex.
     *
     * @return the result
     */

    public String getBuildCoverageRegex() {
        return buildCoverageRegex;
    }

    /**
     * Sets the build coverage regex.
     *
     * @param buildCoverageRegex the build coverage regex value
     */

    public void setBuildCoverageRegex(String buildCoverageRegex) {
        this.buildCoverageRegex = buildCoverageRegex;
    }

    /**
     * Sets the build coverage regex and returns this instance.
     *
     * @param buildCoverageRegex the build coverage regex value
     * @return the result
     */

    public Project withBuildCoverageRegex(String buildCoverageRegex) {
        this.buildCoverageRegex = buildCoverageRegex;
        return this;
    }

    /**
     * Sets the build git strategy.
     *
     * @param buildGitStrategy the build git strategy value
     */

    public void setBuildGitStrategy(BuildGitStrategy buildGitStrategy) {
        this.buildGitStrategy = buildGitStrategy;
    }

    /**
     * Returns the import status.
     *
     * @return the result
     */

    public Status getImportStatus() {
        return importStatus;
    }

    /**
     * Sets the build git strategy and returns this instance.
     *
     * @param buildGitStrategy the build git strategy value
     * @return the result
     */

    public Project withBuildGitStrategy(BuildGitStrategy buildGitStrategy) {
        this.buildGitStrategy = buildGitStrategy;
        return this;
    }

    /**
     * Returns the readme url.
     *
     * @return the result
     */

    public String getReadmeUrl() {
        return readmeUrl;
    }

    /**
     * Sets the readme url.
     *
     * @param readmeUrl the readme url value
     */

    public void setReadmeUrl(String readmeUrl) {
        this.readmeUrl = readmeUrl;
    }

    /**
     * Returns the can create merge request in.
     *
     * @return the result
     */

    public Boolean getCanCreateMergeRequestIn() {
        return canCreateMergeRequestIn;
    }

    /**
     * Sets the can create merge request in.
     *
     * @param canCreateMergeRequestIn the can create merge request in value
     */

    public void setCanCreateMergeRequestIn(Boolean canCreateMergeRequestIn) {
        this.canCreateMergeRequestIn = canCreateMergeRequestIn;
    }

    /**
     * Sets the import status.
     *
     * @param importStatus the import status value
     */

    public void setImportStatus(Status importStatus) {
        this.importStatus = importStatus;
    }

    /**
     * Sets the ci config path and returns this instance.
     *
     * @param ciConfigPath the ci config path value
     * @return the result
     */

    public Project withCiConfigPath(String ciConfigPath) {
        this.ciConfigPath = ciConfigPath;
        return this;
    }

    /**
     * Returns the ci default git depth.
     *
     * @return the result
     */

    public Integer getCiDefaultGitDepth() {
        return ciDefaultGitDepth;
    }

    /**
     * Sets the ci default git depth.
     *
     * @param ciDefaultGitDepth the ci default git depth value
     */

    public void setCiDefaultGitDepth(Integer ciDefaultGitDepth) {
        this.ciDefaultGitDepth = ciDefaultGitDepth;
    }

    /**
     * Returns the ci forward deployment enabled.
     *
     * @return the result
     */

    public Boolean getCiForwardDeploymentEnabled() {
        return ciForwardDeploymentEnabled;
    }

    /**
     * Sets the ci forward deployment enabled.
     *
     * @param ciForwardDeploymentEnabled the ci forward deployment enabled value
     */

    public void setCiForwardDeploymentEnabled(Boolean ciForwardDeploymentEnabled) {
        this.ciForwardDeploymentEnabled = ciForwardDeploymentEnabled;
    }

    /**
     * Returns the ci config path.
     *
     * @return the result
     */

    public String getCiConfigPath() {
        return ciConfigPath;
    }

    /**
     * Sets the ci config path.
     *
     * @param ciConfigPath the ci config path value
     */

    public void setCiConfigPath(String ciConfigPath) {
        this.ciConfigPath = ciConfigPath;
    }

    /**
     * Sets the auto devops enabled and returns this instance.
     *
     * @param autoDevopsEnabled the auto devops enabled value
     * @return the result
     */

    public Project withAutoDevopsEnabled(Boolean autoDevopsEnabled) {
        this.autoDevopsEnabled = autoDevopsEnabled;
        return this;
    }

    /**
     * Returns the remove source branch after merge.
     *
     * @return the result
     */

    public Boolean getRemoveSourceBranchAfterMerge() {
        return removeSourceBranchAfterMerge;
    }

    /**
     * Sets the remove source branch after merge.
     *
     * @param removeSourceBranchAfterMerge the remove source branch after merge value
     */

    public void setRemoveSourceBranchAfterMerge(Boolean removeSourceBranchAfterMerge) {
        this.removeSourceBranchAfterMerge = removeSourceBranchAfterMerge;
    }

    /**
     * Sets the remove source branch after merge and returns this instance.
     *
     * @param removeSourceBranchAfterMerge the remove source branch after merge value
     * @return the result
     */

    public Project withRemoveSourceBranchAfterMerge(Boolean removeSourceBranchAfterMerge) {
        this.removeSourceBranchAfterMerge = removeSourceBranchAfterMerge;
        return this;
    }

    /**
     * Returns the auto devops enabled.
     *
     * @return the result
     */

    public Boolean getAutoDevopsEnabled() {
        return autoDevopsEnabled;
    }

    /**
     * Sets the auto devops enabled.
     *
     * @param autoDevopsEnabled the auto devops enabled value
     */

    public void setAutoDevopsEnabled(Boolean autoDevopsEnabled) {
        this.autoDevopsEnabled = autoDevopsEnabled;
    }

    /**
     * Returns the auto devops deploy strategy.
     *
     * @return the result
     */

    public AutoDevopsDeployStrategy getAutoDevopsDeployStrategy() {
        return autoDevopsDeployStrategy;
    }

    /**
     * Sets the auto devops deploy strategy.
     *
     * @param autoDevopsDeployStrategy the auto devops deploy strategy value
     */

    public void setAutoDevopsDeployStrategy(AutoDevopsDeployStrategy autoDevopsDeployStrategy) {
        this.autoDevopsDeployStrategy = autoDevopsDeployStrategy;
    }

    /**
     * Sets the autoclose referenced issues and returns this instance.
     *
     * @param autocloseReferencedIssues the autoclose referenced issues value
     * @return the result
     */

    public Project withAutocloseReferencedIssues(Boolean autocloseReferencedIssues) {
        this.autocloseReferencedIssues = autocloseReferencedIssues;
        return this;
    }

    /**
     * Returns the autoclose referenced issues.
     *
     * @return the result
     */

    public Boolean getAutocloseReferencedIssues() {
        return autocloseReferencedIssues;
    }

    /**
     * Sets the autoclose referenced issues.
     *
     * @param autocloseReferencedIssues the autoclose referenced issues value
     */

    public void setAutocloseReferencedIssues(Boolean autocloseReferencedIssues) {
        this.autocloseReferencedIssues = autocloseReferencedIssues;
    }

    /**
     * Sets the suggestion commit message.
     *
     * @param suggestionCommitMessage the suggestion commit message value
     */

    public void setSuggestionCommitMessage(String suggestionCommitMessage) {
        this.suggestionCommitMessage = suggestionCommitMessage;
    }

    /**
     * Returns the emails disabled.
     *
     * @return the result
     */

    public Boolean getEmailsDisabled() {
        return emailsDisabled;
    }

    /**
     * Sets the emails disabled.
     *
     * @param emailsDisabled the emails disabled value
     */

    public void setEmailsDisabled(Boolean emailsDisabled) {
        this.emailsDisabled = emailsDisabled;
    }

    /**
     * Sets the emails disabled and returns this instance.
     *
     * @param emailsDisabled the emails disabled value
     * @return the result
     */

    public Project withEmailsDisabled(Boolean emailsDisabled) {
        this.emailsDisabled = emailsDisabled;
        return this;
    }

    /**
     * Returns the suggestion commit message.
     *
     * @return the result
     */

    public String getSuggestionCommitMessage() {
        return this.suggestionCommitMessage;
    }

    /**
     * Sets the suggestion commit message and returns this instance.
     *
     * @param suggestionCommitMessage the suggestion commit message value
     * @return the result
     */

    public Project withSuggestionCommitMessage(String suggestionCommitMessage) {
        this.suggestionCommitMessage = suggestionCommitMessage;
        return this;
    }

    /**
     * Returns the merge requests template.
     *
     * @return the result
     */

    public String getMergeRequestsTemplate() {
        return mergeRequestsTemplate;
    }

    /**
     * Returns the squash option.
     *
     * @return the result
     */

    public SquashOption getSquashOption() {
        return squashOption;
    }

    /**
     * Sets the squash option.
     *
     * @param squashOption the squash option value
     */

    public void setSquashOption(SquashOption squashOption) {
        this.squashOption = squashOption;
    }

    /**
     * Sets the squash option and returns this instance.
     *
     * @param squashOption the squash option value
     * @return the result
     */

    public Project withSquashOption(SquashOption squashOption) {
        this.squashOption = squashOption;
        return this;
    }

    /**
     * Returns the merge commit template.
     *
     * @return the result
     */

    public String getMergeCommitTemplate() {
        return mergeCommitTemplate;
    }

    /**
     * Sets the merge commit template.
     *
     * @param mergeCommitTemplate the merge commit template value
     */

    public void setMergeCommitTemplate(String mergeCommitTemplate) {
        this.mergeCommitTemplate = mergeCommitTemplate;
    }

    /**
     * Returns the squash commit template.
     *
     * @return the result
     */

    public String getSquashCommitTemplate() {
        return squashCommitTemplate;
    }

    /**
     * Sets the squash commit template.
     *
     * @param squashCommitTemplate the squash commit template value
     */

    public void setSquashCommitTemplate(String squashCommitTemplate) {
        this.squashCommitTemplate = squashCommitTemplate;
    }

    /**
     * Returns the issue branch template.
     *
     * @return the result
     */

    public String getIssueBranchTemplate() {
        return issueBranchTemplate;
    }

    /**
     * Sets the issue branch template.
     *
     * @param issueBranchTemplate the issue branch template value
     */

    public void setIssueBranchTemplate(String issueBranchTemplate) {
        this.issueBranchTemplate = issueBranchTemplate;
    }

    /**
     * Sets the merge requests template.
     *
     * @param mergeRequestsTemplate the merge requests template value
     */

    public void setMergeRequestsTemplate(String mergeRequestsTemplate) {
        this.mergeRequestsTemplate = mergeRequestsTemplate;
    }

    /**
     * Returns the build timeout.
     *
     * @return the result
     */

    public Integer getBuildTimeout() {
        return buildTimeout;
    }

    /**
     * Returns the issues template.
     *
     * @return the result
     */

    public String getIssuesTemplate() {
        return issuesTemplate;
    }

    /**
     * Sets the issues template.
     *
     * @param issuesTemplate the issues template value
     */

    public void setIssuesTemplate(String issuesTemplate) {
        this.issuesTemplate = issuesTemplate;
    }

    /**
     * Returns the links.
     *
     * @return the result
     */

    public Map<String, String> getLinks() {
        return links;
    }

    /**
     * Sets the links.
     *
     * @param links the links value
     */

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    /**
     * Sets the build timeout.
     *
     * @param buildTimeout the build timeout value
     */

    public void setBuildTimeout(Integer buildTimeout) {
        this.buildTimeout = buildTimeout;
    }

    /**
     * Sets the build timeout and returns this instance.
     *
     * @param buildTimeout the build timeout value
     * @return the result
     */

    public Project withBuildTimeout(Integer buildTimeout) {
        this.buildTimeout = buildTimeout;
        return this;
    }

    /**
     * Returns the mirror trigger builds.
     *
     * @return the result
     */

    public Boolean getMirrorTriggerBuilds() {
        return mirrorTriggerBuilds;
    }

    /**
     * Sets the mirror trigger builds.
     *
     * @param mirrorTriggerBuilds the mirror trigger builds value
     */

    public void setMirrorTriggerBuilds(Boolean mirrorTriggerBuilds) {
        this.mirrorTriggerBuilds = mirrorTriggerBuilds;
    }

    /**
     * Sets the mirror trigger builds and returns this instance.
     *
     * @param mirrorTriggerBuilds the mirror trigger builds value
     * @return the result
     */

    public Project withMirrorTriggerBuilds(Boolean mirrorTriggerBuilds) {
        this.mirrorTriggerBuilds = mirrorTriggerBuilds;
        return this;
    }

    /**
     * Returns the group with project templates id.
     *
     * @return the result
     */

    public Integer getGroupWithProjectTemplatesId() {
        return groupWithProjectTemplatesId;
    }

    /**
     * Sets the group with project templates id.
     *
     * @param groupWithProjectTemplatesId the group with project templates id value
     */

    public void setGroupWithProjectTemplatesId(Integer groupWithProjectTemplatesId) {
        this.groupWithProjectTemplatesId = groupWithProjectTemplatesId;
    }

    /**
     * Sets the group with project templates id and returns this instance.
     *
     * @param groupWithProjectTemplatesId the group with project templates id value
     * @return the result
     */

    public Project withGroupWithProjectTemplatesId(Integer groupWithProjectTemplatesId) {
        this.groupWithProjectTemplatesId = groupWithProjectTemplatesId;
        return this;
    }

    /**
     * Returns the group runners enabled.
     *
     * @return the result
     */

    public Boolean getGroupRunnersEnabled() {
        return groupRunnersEnabled;
    }

    /**
     * Sets the group runners enabled.
     *
     * @param groupRunnersEnabled the group runners enabled value
     */

    public void setGroupRunnersEnabled(Boolean groupRunnersEnabled) {
        this.groupRunnersEnabled = groupRunnersEnabled;
    }

    /**
     * Sets the group runners enabled and returns this instance.
     *
     * @param groupRunnersEnabled the group runners enabled value
     * @return the result
     */

    public Project withGroupRunnersEnabled(Boolean groupRunnersEnabled) {
        this.groupRunnersEnabled = groupRunnersEnabled;
        return this;
    }

    /**
     * Returns the only allow merge if all status checks passed.
     *
     * @return the result
     */

    public Boolean getOnlyAllowMergeIfAllStatusChecksPassed() {
        return onlyAllowMergeIfAllStatusChecksPassed;
    }

    /**
     * Sets the only allow merge if all status checks passed.
     *
     * @param onlyAllowMergeIfAllStatusChecksPassed the only allow merge if all status checks passed value
     */

    public void setOnlyAllowMergeIfAllStatusChecksPassed(Boolean onlyAllowMergeIfAllStatusChecksPassed) {
        this.onlyAllowMergeIfAllStatusChecksPassed = onlyAllowMergeIfAllStatusChecksPassed;
    }

    /**
     * Sets the only allow merge if all status checks passed and returns this instance.
     *
     * @param onlyAllowMergeIfAllStatusChecksPassed the only allow merge if all status checks passed value
     * @return the result
     */

    public Project withOnlyAllowMergeIfAllStatusChecksPassed(Boolean onlyAllowMergeIfAllStatusChecksPassed) {
        this.onlyAllowMergeIfAllStatusChecksPassed = onlyAllowMergeIfAllStatusChecksPassed;
        return this;
    }

    /**
     * Returns the public builds.
     *
     * @return the result
     */

    public Boolean getPublicBuilds() {
        return publicBuilds;
    }

    /**
     * Sets the public builds.
     *
     * @param publicBuilds the public builds value
     */

    public void setPublicBuilds(Boolean publicBuilds) {
        this.publicBuilds = publicBuilds;
    }

    /**
     * Sets the public builds and returns this instance.
     *
     * @param publicBuilds the public builds value
     * @return the result
     */

    public Project withPublicBuilds(Boolean publicBuilds) {
        this.publicBuilds = publicBuilds;
        return this;
    }

    /**
     * Returns the repository object format.
     *
     * @return the result
     */

    public String getRepositoryObjectFormat() {
        return repositoryObjectFormat;
    }

    /**
     * Sets the repository object format.
     *
     * @param repositoryObjectFormat the repository object format value
     */

    public void setRepositoryObjectFormat(String repositoryObjectFormat) {
        this.repositoryObjectFormat = repositoryObjectFormat;
    }

    /**
     * Sets the repository object format and returns this instance.
     *
     * @param repositoryObjectFormat the repository object format value
     * @return the result
     */

    public Project withRepositoryObjectFormat(String repositoryObjectFormat) {
        this.repositoryObjectFormat = repositoryObjectFormat;
        return this;
    }

    /**
     * Returns the template name.
     *
     * @return the result
     */

    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the template name.
     *
     * @param templateName the template name value
     */

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Sets the template name and returns this instance.
     *
     * @param templateName the template name value
     * @return the result
     */

    public Project withTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    /**
     * Returns the external authorization classification label.
     *
     * @return the result
     */

    public String getExternalAuthorizationClassificationLabel() {
        return externalAuthorizationClassificationLabel;
    }

    /**
     * Sets the external authorization classification label.
     *
     * @param externalAuthorizationClassificationLabel the external authorization classification label value
     */

    public void setExternalAuthorizationClassificationLabel(String externalAuthorizationClassificationLabel) {
        this.externalAuthorizationClassificationLabel = externalAuthorizationClassificationLabel;
    }

    /**
     * Sets the external authorization classification label and returns this instance.
     *
     * @param externalAuthorizationClassificationLabel the external authorization classification label value
     * @return the result
     */

    public Project withExternalAuthorizationClassificationLabel(String externalAuthorizationClassificationLabel) {
        this.externalAuthorizationClassificationLabel = externalAuthorizationClassificationLabel;
        return this;
    }

    /**
     * Returns the auto cancel pending pipelines.
     *
     * @return the result
     */

    public AutoCancelPendingPipelines getAutoCancelPendingPipelines() {
        return autoCancelPendingPipelines;
    }

    /**
     * Sets the auto cancel pending pipelines.
     *
     * @param autoCancelPendingPipelines the auto cancel pending pipelines value
     */

    public void setAutoCancelPendingPipelines(AutoCancelPendingPipelines autoCancelPendingPipelines) {
        this.autoCancelPendingPipelines = autoCancelPendingPipelines;
    }

    /**
     * Sets the auto cancel pending pipelines and returns this instance.
     *
     * @param autoCancelPendingPipelines the auto cancel pending pipelines value
     * @return the result
     */

    public Project withAutoCancelPendingPipelines(AutoCancelPendingPipelines autoCancelPendingPipelines) {
        this.autoCancelPendingPipelines = autoCancelPendingPipelines;
        return this;
    }

    /**
     * Returns the use custom template.
     *
     * @return the result
     */

    public Boolean getUseCustomTemplate() {
        return useCustomTemplate;
    }

    /**
     * Sets the use custom template.
     *
     * @param useCustomTemplate the use custom template value
     */

    public void setUseCustomTemplate(Boolean useCustomTemplate) {
        this.useCustomTemplate = useCustomTemplate;
    }

    /**
     * Sets the use custom template and returns this instance.
     *
     * @param useCustomTemplate the use custom template value
     * @return the result
     */

    public Project withUseCustomTemplate(Boolean useCustomTemplate) {
        this.useCustomTemplate = useCustomTemplate;
        return this;
    }

    /**
     * Returns the emails enabled.
     *
     * @return the result
     */

    public Boolean getEmailsEnabled() {
        return emailsEnabled;
    }

    /**
     * Sets the emails enabled.
     *
     * @param emailsEnabled the emails enabled value
     */

    public void setEmailsEnabled(Boolean emailsEnabled) {
        this.emailsEnabled = emailsEnabled;
    }

    /**
     * Sets the emails enabled and returns this instance.
     *
     * @param emailsEnabled the emails enabled value
     * @return the result
     */

    public Project withEmailsEnabled(Boolean emailsEnabled) {
        this.emailsEnabled = emailsEnabled;
        return this;
    }

    /**
     * Returns the show default award emojis.
     *
     * @return the result
     */

    public Boolean getShowDefaultAwardEmojis() {
        return showDefaultAwardEmojis;
    }

    /**
     * Sets the show default award emojis.
     *
     * @param showDefaultAwardEmojis the show default award emojis value
     */

    public void setShowDefaultAwardEmojis(Boolean showDefaultAwardEmojis) {
        this.showDefaultAwardEmojis = showDefaultAwardEmojis;
    }

    /**
     * Sets the show default award emojis and returns this instance.
     *
     * @param showDefaultAwardEmojis the show default award emojis value
     * @return the result
     */

    public Project withShowDefaultAwardEmojis(Boolean showDefaultAwardEmojis) {
        this.showDefaultAwardEmojis = showDefaultAwardEmojis;
        return this;
    }

    /**
     * Returns the warn about potentially unwanted characters.
     *
     * @return the result
     */

    public Boolean getWarnAboutPotentiallyUnwantedCharacters() {
        return warnAboutPotentiallyUnwantedCharacters;
    }

    /**
     * Sets the warn about potentially unwanted characters.
     *
     * @param warnAboutPotentiallyUnwantedCharacters the warn about potentially unwanted characters value
     */

    public void setWarnAboutPotentiallyUnwantedCharacters(Boolean warnAboutPotentiallyUnwantedCharacters) {
        this.warnAboutPotentiallyUnwantedCharacters = warnAboutPotentiallyUnwantedCharacters;
    }

    /**
     * Sets the warn about potentially unwanted characters and returns this instance.
     *
     * @param warnAboutPotentiallyUnwantedCharacters the warn about potentially unwanted characters value
     * @return the result
     */

    public Project withWarnAboutPotentiallyUnwantedCharacters(Boolean warnAboutPotentiallyUnwantedCharacters) {
        this.warnAboutPotentiallyUnwantedCharacters = warnAboutPotentiallyUnwantedCharacters;
        return this;
    }

    /**
     * Returns the mirror.
     *
     * @return the result
     */

    public Boolean getMirror() {
        return mirror;
    }

    /**
     * Sets the mirror.
     *
     * @param mirror the mirror value
     */

    public void setMirror(Boolean mirror) {
        this.mirror = mirror;
    }

    /**
     * Sets the mirror and returns this instance.
     *
     * @param mirror the mirror value
     * @return the result
     */

    public Project withMirror(Boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the description html.
     *
     * @return the result
     */

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    /**
     * Sets the description html.
     *
     * @param descriptionHtml the description html value
     */

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    /**
     * Returns the container registry image prefix.
     *
     * @return the result
     */

    public String getContainerRegistryImagePrefix() {
        return containerRegistryImagePrefix;
    }

    /**
     * Sets the container registry image prefix.
     *
     * @param containerRegistryImagePrefix the container registry image prefix value
     */

    public void setContainerRegistryImagePrefix(String containerRegistryImagePrefix) {
        this.containerRegistryImagePrefix = containerRegistryImagePrefix;
    }

    /**
     * Returns the container expiration policy.
     *
     * @return the result
     */

    public ContainerExpirationPolicy getContainerExpirationPolicy() {
        return containerExpirationPolicy;
    }

    /**
     * Sets the container expiration policy.
     *
     * @param containerExpirationPolicy the container expiration policy value
     */

    public void setContainerExpirationPolicy(ContainerExpirationPolicy containerExpirationPolicy) {
        this.containerExpirationPolicy = containerExpirationPolicy;
    }

    /**
     * Sets the container expiration policy and returns this instance.
     *
     * @param containerExpirationPolicy the container expiration policy value
     * @return the result
     */

    public Project withContainerExpirationPolicy(ContainerExpirationPolicy containerExpirationPolicy) {
        this.containerExpirationPolicy = containerExpirationPolicy;
        return this;
    }

    /**
     * Returns the service desk enabled.
     *
     * @return the result
     */

    public Boolean getServiceDeskEnabled() {
        return serviceDeskEnabled;
    }

    /**
     * Sets the service desk enabled.
     *
     * @param serviceDeskEnabled the service desk enabled value
     */

    public void setServiceDeskEnabled(Boolean serviceDeskEnabled) {
        this.serviceDeskEnabled = serviceDeskEnabled;
    }

    /**
     * Returns the import url.
     *
     * @return the result
     */

    public String getImportUrl() {
        return importUrl;
    }

    /**
     * Sets the import url.
     *
     * @param importUrl the import url value
     */

    public void setImportUrl(String importUrl) {
        this.importUrl = importUrl;
    }

    /**
     * Returns the import type.
     *
     * @return the result
     */

    public String getImportType() {
        return importType;
    }

    /**
     * Sets the import type.
     *
     * @param importType the import type value
     */

    public void setImportType(String importType) {
        this.importType = importType;
    }

    /**
     * Returns the import error.
     *
     * @return the result
     */

    public String getImportError() {
        return importError;
    }

    /**
     * Sets the import error.
     *
     * @param importError the import error value
     */

    public void setImportError(String importError) {
        this.importError = importError;
    }

    /**
     * Returns the ci forward deployment rollback allowed.
     *
     * @return the result
     */

    public Boolean getCiForwardDeploymentRollbackAllowed() {
        return ciForwardDeploymentRollbackAllowed;
    }

    /**
     * Sets the ci forward deployment rollback allowed.
     *
     * @param ciForwardDeploymentRollbackAllowed the ci forward deployment rollback allowed value
     */

    public void setCiForwardDeploymentRollbackAllowed(Boolean ciForwardDeploymentRollbackAllowed) {
        this.ciForwardDeploymentRollbackAllowed = ciForwardDeploymentRollbackAllowed;
    }

    /**
     * Returns the ci allow fork pipelines to run in parent project.
     *
     * @return the result
     */

    public Boolean getCiAllowForkPipelinesToRunInParentProject() {
        return ciAllowForkPipelinesToRunInParentProject;
    }

    /**
     * Sets the ci allow fork pipelines to run in parent project.
     *
     * @param ciAllowForkPipelinesToRunInParentProject the ci allow fork pipelines to run in parent project value
     */

    public void setCiAllowForkPipelinesToRunInParentProject(Boolean ciAllowForkPipelinesToRunInParentProject) {
        this.ciAllowForkPipelinesToRunInParentProject = ciAllowForkPipelinesToRunInParentProject;
    }

    /**
     * Returns the ci id token sub claim components.
     *
     * @return the result
     */

    public List<String> getCiIdTokenSubClaimComponents() {
        return ciIdTokenSubClaimComponents;
    }

    /**
     * Sets the ci id token sub claim components.
     *
     * @param ciIdTokenSubClaimComponents the ci id token sub claim components value
     */

    public void setCiIdTokenSubClaimComponents(List<String> ciIdTokenSubClaimComponents) {
        this.ciIdTokenSubClaimComponents = ciIdTokenSubClaimComponents;
    }

    /**
     * Returns the ci job token scope enabled.
     *
     * @return the result
     */

    public Boolean getCiJobTokenScopeEnabled() {
        return ciJobTokenScopeEnabled;
    }

    /**
     * Sets the ci job token scope enabled.
     *
     * @param ciJobTokenScopeEnabled the ci job token scope enabled value
     */

    public void setCiJobTokenScopeEnabled(Boolean ciJobTokenScopeEnabled) {
        this.ciJobTokenScopeEnabled = ciJobTokenScopeEnabled;
    }

    /**
     * Returns the ci separated caches.
     *
     * @return the result
     */

    public Boolean getCiSeparatedCaches() {
        return ciSeparatedCaches;
    }

    /**
     * Sets the ci separated caches.
     *
     * @param ciSeparatedCaches the ci separated caches value
     */

    public void setCiSeparatedCaches(Boolean ciSeparatedCaches) {
        this.ciSeparatedCaches = ciSeparatedCaches;
    }

    /**
     * Returns the ci restrict pipeline cancellation role.
     *
     * @return the result
     */

    public String getCiRestrictPipelineCancellationRole() {
        return ciRestrictPipelineCancellationRole;
    }

    /**
     * Sets the ci restrict pipeline cancellation role.
     *
     * @param ciRestrictPipelineCancellationRole the ci restrict pipeline cancellation role value
     */

    public void setCiRestrictPipelineCancellationRole(String ciRestrictPipelineCancellationRole) {
        this.ciRestrictPipelineCancellationRole = ciRestrictPipelineCancellationRole;
    }

    /**
     * Returns the ci pipeline variables minimum override role.
     *
     * @return the result
     */

    public String getCiPipelineVariablesMinimumOverrideRole() {
        return ciPipelineVariablesMinimumOverrideRole;
    }

    /**
     * Sets the ci pipeline variables minimum override role.
     *
     * @param ciPipelineVariablesMinimumOverrideRole the ci pipeline variables minimum override role value
     */

    public void setCiPipelineVariablesMinimumOverrideRole(String ciPipelineVariablesMinimumOverrideRole) {
        this.ciPipelineVariablesMinimumOverrideRole = ciPipelineVariablesMinimumOverrideRole;
    }

    /**
     * Returns the ci push repository for job token allowed.
     *
     * @return the result
     */

    public Boolean getCiPushRepositoryForJobTokenAllowed() {
        return ciPushRepositoryForJobTokenAllowed;
    }

    /**
     * Sets the ci push repository for job token allowed.
     *
     * @param ciPushRepositoryForJobTokenAllowed the ci push repository for job token allowed value
     */

    public void setCiPushRepositoryForJobTokenAllowed(Boolean ciPushRepositoryForJobTokenAllowed) {
        this.ciPushRepositoryForJobTokenAllowed = ciPushRepositoryForJobTokenAllowed;
    }

    /**
     * Returns the allow pipeline trigger approve deployment.
     *
     * @return the result
     */

    public Boolean getAllowPipelineTriggerApproveDeployment() {
        return allowPipelineTriggerApproveDeployment;
    }

    /**
     * Sets the allow pipeline trigger approve deployment.
     *
     * @param allowPipelineTriggerApproveDeployment the allow pipeline trigger approve deployment value
     */

    public void setAllowPipelineTriggerApproveDeployment(Boolean allowPipelineTriggerApproveDeployment) {
        this.allowPipelineTriggerApproveDeployment = allowPipelineTriggerApproveDeployment;
    }

    /**
     * Returns the restrict user defined variables.
     *
     * @return the result
     */

    public Boolean getRestrictUserDefinedVariables() {
        return restrictUserDefinedVariables;
    }

    /**
     * Sets the restrict user defined variables.
     *
     * @param restrictUserDefinedVariables the restrict user defined variables value
     */

    public void setRestrictUserDefinedVariables(Boolean restrictUserDefinedVariables) {
        this.restrictUserDefinedVariables = restrictUserDefinedVariables;
    }

    /**
     * Returns the enforce auth checks on uploads.
     *
     * @return the result
     */

    public Boolean getEnforceAuthChecksOnUploads() {
        return enforceAuthChecksOnUploads;
    }

    /**
     * Sets the enforce auth checks on uploads.
     *
     * @param enforceAuthChecksOnUploads the enforce auth checks on uploads value
     */

    public void setEnforceAuthChecksOnUploads(Boolean enforceAuthChecksOnUploads) {
        this.enforceAuthChecksOnUploads = enforceAuthChecksOnUploads;
    }

    /**
     * Returns the keep latest artifact.
     *
     * @return the result
     */

    public Boolean getKeepLatestArtifact() {
        return keepLatestArtifact;
    }

    /**
     * Sets the keep latest artifact.
     *
     * @param keepLatestArtifact the keep latest artifact value
     */

    public void setKeepLatestArtifact(Boolean keepLatestArtifact) {
        this.keepLatestArtifact = keepLatestArtifact;
    }

    /**
     * Returns the runner token expiration interval.
     *
     * @return the result
     */

    public Integer getRunnerTokenExpirationInterval() {
        return runnerTokenExpirationInterval;
    }

    /**
     * Sets the runner token expiration interval.
     *
     * @param runnerTokenExpirationInterval the runner token expiration interval value
     */

    public void setRunnerTokenExpirationInterval(Integer runnerTokenExpirationInterval) {
        this.runnerTokenExpirationInterval = runnerTokenExpirationInterval;
    }

    /**
     * Returns the requirements enabled.
     *
     * @return the result
     */

    public Boolean getRequirementsEnabled() {
        return requirementsEnabled;
    }

    /**
     * Sets the requirements enabled.
     *
     * @param requirementsEnabled the requirements enabled value
     */

    public void setRequirementsEnabled(Boolean requirementsEnabled) {
        this.requirementsEnabled = requirementsEnabled;
    }

    /**
     * Returns the security and compliance enabled.
     *
     * @return the result
     */

    public Boolean getSecurityAndComplianceEnabled() {
        return securityAndComplianceEnabled;
    }

    /**
     * Sets the security and compliance enabled.
     *
     * @param securityAndComplianceEnabled the security and compliance enabled value
     */

    public void setSecurityAndComplianceEnabled(Boolean securityAndComplianceEnabled) {
        this.securityAndComplianceEnabled = securityAndComplianceEnabled;
    }

    /**
     * Returns the secret push protection enabled.
     *
     * @return the result
     */

    public Boolean getSecretPushProtectionEnabled() {
        return secretPushProtectionEnabled;
    }

    /**
     * Sets the secret push protection enabled.
     *
     * @param secretPushProtectionEnabled the secret push protection enabled value
     */

    public void setSecretPushProtectionEnabled(Boolean secretPushProtectionEnabled) {
        this.secretPushProtectionEnabled = secretPushProtectionEnabled;
    }

    /**
     * Returns the compliance frameworks.
     *
     * @return the result
     */

    public List<String> getComplianceFrameworks() {
        return complianceFrameworks;
    }

    /**
     * Sets the compliance frameworks.
     *
     * @param complianceFrameworks the compliance frameworks value
     */

    public void setComplianceFrameworks(List<String> complianceFrameworks) {
        this.complianceFrameworks = complianceFrameworks;
    }

    /**
     * Returns the analytics access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getAnalyticsAccessLevel() {
        return analyticsAccessLevel;
    }

    /**
     * Sets the analytics access level.
     *
     * @param analyticsAccessLevel the analytics access level value
     */

    public void setAnalyticsAccessLevel(ProjectFeatureVisibilityAccessLevel analyticsAccessLevel) {
        this.analyticsAccessLevel = analyticsAccessLevel;
    }

    /**
     * Sets the analytics access level and returns this instance.
     *
     * @param analyticsAccessLevel the analytics access level value
     * @return the result
     */

    public Project withAnalyticsAccessLevel(ProjectFeatureVisibilityAccessLevel analyticsAccessLevel) {
        this.analyticsAccessLevel = analyticsAccessLevel;
        return this;
    }

    /**
     * Returns the builds access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getBuildsAccessLevel() {
        return buildsAccessLevel;
    }

    /**
     * Sets the builds access level.
     *
     * @param buildsAccessLevel the builds access level value
     */

    public void setBuildsAccessLevel(ProjectFeatureVisibilityAccessLevel buildsAccessLevel) {
        this.buildsAccessLevel = buildsAccessLevel;
    }

    /**
     * Sets the builds access level and returns this instance.
     *
     * @param buildsAccessLevel the builds access level value
     * @return the result
     */

    public Project withBuildsAccessLevel(ProjectFeatureVisibilityAccessLevel buildsAccessLevel) {
        this.buildsAccessLevel = buildsAccessLevel;
        return this;
    }

    /**
     * Returns the container registry access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getContainerRegistryAccessLevel() {
        return containerRegistryAccessLevel;
    }

    /**
     * Sets the container registry access level.
     *
     * @param containerRegistryAccessLevel the container registry access level value
     */

    public void setContainerRegistryAccessLevel(ProjectFeatureVisibilityAccessLevel containerRegistryAccessLevel) {
        this.containerRegistryAccessLevel = containerRegistryAccessLevel;
    }

    /**
     * Sets the container registry access level and returns this instance.
     *
     * @param containerRegistryAccessLevel the container registry access level value
     * @return the result
     */

    public Project withContainerRegistryAccessLevel(ProjectFeatureVisibilityAccessLevel containerRegistryAccessLevel) {
        this.containerRegistryAccessLevel = containerRegistryAccessLevel;
        return this;
    }

    /**
     * Returns the environments access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getEnvironmentsAccessLevel() {
        return environmentsAccessLevel;
    }

    /**
     * Sets the environments access level.
     *
     * @param environmentsAccessLevel the environments access level value
     */

    public void setEnvironmentsAccessLevel(ProjectFeatureVisibilityAccessLevel environmentsAccessLevel) {
        this.environmentsAccessLevel = environmentsAccessLevel;
    }

    /**
     * Sets the environments access level and returns this instance.
     *
     * @param environmentsAccessLevel the environments access level value
     * @return the result
     */

    public Project withEnvironmentsAccessLevel(ProjectFeatureVisibilityAccessLevel environmentsAccessLevel) {
        this.environmentsAccessLevel = environmentsAccessLevel;
        return this;
    }

    /**
     * Returns the feature flags access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getFeatureFlagsAccessLevel() {
        return featureFlagsAccessLevel;
    }

    /**
     * Sets the feature flags access level.
     *
     * @param featureFlagsAccessLevel the feature flags access level value
     */

    public void setFeatureFlagsAccessLevel(ProjectFeatureVisibilityAccessLevel featureFlagsAccessLevel) {
        this.featureFlagsAccessLevel = featureFlagsAccessLevel;
    }

    /**
     * Sets the feature flags access level and returns this instance.
     *
     * @param featureFlagsAccessLevel the feature flags access level value
     * @return the result
     */

    public Project withFeatureFlagsAccessLevel(ProjectFeatureVisibilityAccessLevel featureFlagsAccessLevel) {
        this.featureFlagsAccessLevel = featureFlagsAccessLevel;
        return this;
    }

    /**
     * Returns the forking access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getForkingAccessLevel() {
        return forkingAccessLevel;
    }

    /**
     * Sets the forking access level.
     *
     * @param forkingAccessLevel the forking access level value
     */

    public void setForkingAccessLevel(ProjectFeatureVisibilityAccessLevel forkingAccessLevel) {
        this.forkingAccessLevel = forkingAccessLevel;
    }

    /**
     * Sets the forking access level and returns this instance.
     *
     * @param forkingAccessLevel the forking access level value
     * @return the result
     */

    public Project withForkingAccessLevel(ProjectFeatureVisibilityAccessLevel forkingAccessLevel) {
        this.forkingAccessLevel = forkingAccessLevel;
        return this;
    }

    /**
     * Returns the infrastructure access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getInfrastructureAccessLevel() {
        return infrastructureAccessLevel;
    }

    /**
     * Sets the infrastructure access level.
     *
     * @param infrastructureAccessLevel the infrastructure access level value
     */

    public void setInfrastructureAccessLevel(ProjectFeatureVisibilityAccessLevel infrastructureAccessLevel) {
        this.infrastructureAccessLevel = infrastructureAccessLevel;
    }

    /**
     * Sets the infrastructure access level and returns this instance.
     *
     * @param infrastructureAccessLevel the infrastructure access level value
     * @return the result
     */

    public Project withInfrastructureAccessLevel(ProjectFeatureVisibilityAccessLevel infrastructureAccessLevel) {
        this.infrastructureAccessLevel = infrastructureAccessLevel;
        return this;
    }

    /**
     * Returns the issues access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getIssuesAccessLevel() {
        return issuesAccessLevel;
    }

    /**
     * Sets the issues access level.
     *
     * @param issuesAccessLevel the issues access level value
     */

    public void setIssuesAccessLevel(ProjectFeatureVisibilityAccessLevel issuesAccessLevel) {
        this.issuesAccessLevel = issuesAccessLevel;
    }

    /**
     * Sets the issues access level and returns this instance.
     *
     * @param issuesAccessLevel the issues access level value
     * @return the result
     */

    public Project withIssuesAccessLevel(ProjectFeatureVisibilityAccessLevel issuesAccessLevel) {
        this.issuesAccessLevel = issuesAccessLevel;
        return this;
    }

    /**
     * Returns the merge requests access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getMergeRequestsAccessLevel() {
        return mergeRequestsAccessLevel;
    }

    /**
     * Sets the merge requests access level.
     *
     * @param mergeRequestsAccessLevel the merge requests access level value
     */

    public void setMergeRequestsAccessLevel(ProjectFeatureVisibilityAccessLevel mergeRequestsAccessLevel) {
        this.mergeRequestsAccessLevel = mergeRequestsAccessLevel;
    }

    /**
     * Sets the merge requests access level and returns this instance.
     *
     * @param mergeRequestsAccessLevel the merge requests access level value
     * @return the result
     */

    public Project withMergeRequestsAccessLevel(ProjectFeatureVisibilityAccessLevel mergeRequestsAccessLevel) {
        this.mergeRequestsAccessLevel = mergeRequestsAccessLevel;
        return this;
    }

    /**
     * Returns the model experiments access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getModelExperimentsAccessLevel() {
        return modelExperimentsAccessLevel;
    }

    /**
     * Sets the model experiments access level.
     *
     * @param modelExperimentsAccessLevel the model experiments access level value
     */

    public void setModelExperimentsAccessLevel(ProjectFeatureVisibilityAccessLevel modelExperimentsAccessLevel) {
        this.modelExperimentsAccessLevel = modelExperimentsAccessLevel;
    }

    /**
     * Sets the model experiments access level and returns this instance.
     *
     * @param modelExperimentsAccessLevel the model experiments access level value
     * @return the result
     */

    public Project withModelExperimentsAccessLevel(ProjectFeatureVisibilityAccessLevel modelExperimentsAccessLevel) {
        this.modelExperimentsAccessLevel = modelExperimentsAccessLevel;
        return this;
    }

    /**
     * Returns the model registry access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getModelRegistryAccessLevel() {
        return modelRegistryAccessLevel;
    }

    /**
     * Sets the model registry access level.
     *
     * @param modelRegistryAccessLevel the model registry access level value
     */

    public void setModelRegistryAccessLevel(ProjectFeatureVisibilityAccessLevel modelRegistryAccessLevel) {
        this.modelRegistryAccessLevel = modelRegistryAccessLevel;
    }

    /**
     * Sets the model registry access level and returns this instance.
     *
     * @param modelRegistryAccessLevel the model registry access level value
     * @return the result
     */

    public Project withModelRegistryAccessLevel(ProjectFeatureVisibilityAccessLevel modelRegistryAccessLevel) {
        this.modelRegistryAccessLevel = modelRegistryAccessLevel;
        return this;
    }

    /**
     * Returns the monitor access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getMonitorAccessLevel() {
        return monitorAccessLevel;
    }

    /**
     * Sets the monitor access level.
     *
     * @param monitorAccessLevel the monitor access level value
     */

    public void setMonitorAccessLevel(ProjectFeatureVisibilityAccessLevel monitorAccessLevel) {
        this.monitorAccessLevel = monitorAccessLevel;
    }

    /**
     * Sets the monitor access level and returns this instance.
     *
     * @param monitorAccessLevel the monitor access level value
     * @return the result
     */

    public Project withMonitorAccessLevel(ProjectFeatureVisibilityAccessLevel monitorAccessLevel) {
        this.monitorAccessLevel = monitorAccessLevel;
        return this;
    }

    /**
     * Returns the pages access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getPagesAccessLevel() {
        return pagesAccessLevel;
    }

    /**
     * Sets the pages access level.
     *
     * @param pagesAccessLevel the pages access level value
     */

    public void setPagesAccessLevel(ProjectFeatureVisibilityAccessLevel pagesAccessLevel) {
        this.pagesAccessLevel = pagesAccessLevel;
    }

    /**
     * Sets the pages access level and returns this instance.
     *
     * @param pagesAccessLevel the pages access level value
     * @return the result
     */

    public Project withPagesAccessLevel(ProjectFeatureVisibilityAccessLevel pagesAccessLevel) {
        this.pagesAccessLevel = pagesAccessLevel;
        return this;
    }

    /**
     * Returns the releases access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getReleasesAccessLevel() {
        return releasesAccessLevel;
    }

    /**
     * Sets the releases access level.
     *
     * @param releasesAccessLevel the releases access level value
     */

    public void setReleasesAccessLevel(ProjectFeatureVisibilityAccessLevel releasesAccessLevel) {
        this.releasesAccessLevel = releasesAccessLevel;
    }

    /**
     * Sets the releases access level and returns this instance.
     *
     * @param releasesAccessLevel the releases access level value
     * @return the result
     */

    public Project withReleasesAccessLevel(ProjectFeatureVisibilityAccessLevel releasesAccessLevel) {
        this.releasesAccessLevel = releasesAccessLevel;
        return this;
    }

    /**
     * Returns the repository access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getRepositoryAccessLevel() {
        return repositoryAccessLevel;
    }

    /**
     * Sets the repository access level.
     *
     * @param repositoryAccessLevel the repository access level value
     */

    public void setRepositoryAccessLevel(ProjectFeatureVisibilityAccessLevel repositoryAccessLevel) {
        this.repositoryAccessLevel = repositoryAccessLevel;
    }

    /**
     * Sets the repository access level and returns this instance.
     *
     * @param repositoryAccessLevel the repository access level value
     * @return the result
     */

    public Project withRepositoryAccessLevel(ProjectFeatureVisibilityAccessLevel repositoryAccessLevel) {
        this.repositoryAccessLevel = repositoryAccessLevel;
        return this;
    }

    /**
     * Returns the requirements access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getRequirementsAccessLevel() {
        return requirementsAccessLevel;
    }

    /**
     * Sets the requirements access level.
     *
     * @param requirementsAccessLevel the requirements access level value
     */

    public void setRequirementsAccessLevel(ProjectFeatureVisibilityAccessLevel requirementsAccessLevel) {
        this.requirementsAccessLevel = requirementsAccessLevel;
    }

    /**
     * Sets the requirements access level and returns this instance.
     *
     * @param requirementsAccessLevel the requirements access level value
     * @return the result
     */

    public Project withRequirementsAccessLevel(ProjectFeatureVisibilityAccessLevel requirementsAccessLevel) {
        this.requirementsAccessLevel = requirementsAccessLevel;
        return this;
    }

    /**
     * Returns the security and compliance access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getSecurityAndComplianceAccessLevel() {
        return securityAndComplianceAccessLevel;
    }

    /**
     * Sets the security and compliance access level.
     *
     * @param securityAndComplianceAccessLevel the security and compliance access level value
     */

    public void setSecurityAndComplianceAccessLevel(
            ProjectFeatureVisibilityAccessLevel securityAndComplianceAccessLevel) {
        this.securityAndComplianceAccessLevel = securityAndComplianceAccessLevel;
    }

    /**
     * Sets the security and compliance access level and returns this instance.
     *
     * @param securityAndComplianceAccessLevel the security and compliance access level value
     * @return the result
     */

    public Project withSecurityAndComplianceAccessLevel(
            ProjectFeatureVisibilityAccessLevel securityAndComplianceAccessLevel) {
        this.securityAndComplianceAccessLevel = securityAndComplianceAccessLevel;
        return this;
    }

    /**
     * Returns the snippets access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getSnippetsAccessLevel() {
        return snippetsAccessLevel;
    }

    /**
     * Sets the snippets access level.
     *
     * @param snippetsAccessLevel the snippets access level value
     */

    public void setSnippetsAccessLevel(ProjectFeatureVisibilityAccessLevel snippetsAccessLevel) {
        this.snippetsAccessLevel = snippetsAccessLevel;
    }

    /**
     * Sets the snippets access level and returns this instance.
     *
     * @param snippetsAccessLevel the snippets access level value
     * @return the result
     */

    public Project withSnippetsAccessLevel(ProjectFeatureVisibilityAccessLevel snippetsAccessLevel) {
        this.snippetsAccessLevel = snippetsAccessLevel;
        return this;
    }

    /**
     * Returns the wiki access level.
     *
     * @return the result
     */

    public ProjectFeatureVisibilityAccessLevel getWikiAccessLevel() {
        return wikiAccessLevel;
    }

    /**
     * Sets the wiki access level.
     *
     * @param wikiAccessLevel the wiki access level value
     */

    public void setWikiAccessLevel(ProjectFeatureVisibilityAccessLevel wikiAccessLevel) {
        this.wikiAccessLevel = wikiAccessLevel;
    }

    /**
     * Sets the wiki access level and returns this instance.
     *
     * @param wikiAccessLevel the wiki access level value
     * @return the result
     */

    public Project withWikiAccessLevel(ProjectFeatureVisibilityAccessLevel wikiAccessLevel) {
        this.wikiAccessLevel = wikiAccessLevel;
        return this;
    }

    // Enum for the merge_method of the Project instance.
    /**
     * The merge method enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeMethod {

        /**
         * The merge merge method.
         */
        MERGE,
        /**
         * The rebase merge merge method.
         */
        REBASE_MERGE,
        /**
         * The ff merge method.
         */
        FF;

        private static JacksonJsonEnumHelper<MergeMethod> enumHelper = new JacksonJsonEnumHelper<>(MergeMethod.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeMethod forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * Returns the link by name.
     *
     * @param name the name value
     * @return the result
     */

    @JsonIgnore
    public String getLinkByName(String name) {
        if (links == null || links.isEmpty()) {
            return (null);
        }

        return (links.get(name));
    }

}
