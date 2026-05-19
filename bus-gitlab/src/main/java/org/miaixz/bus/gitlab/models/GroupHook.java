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

/**
 * The group hook class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GroupHook implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256215172L;

    private Long id;
    private String url;
    private String name;
    private String description;
    private Long groupId;
    private Boolean pushEvents;
    private String pushEventsBranchFilter;
    private String branchFilterStrategy;
    private Boolean issuesEvents;
    private Boolean confidentialIssuesEvents;
    private Boolean mergeRequestsEvents;
    private Boolean tagPushEvents;
    private Boolean noteEvents;
    private Boolean confidentialNoteEvents;
    private Boolean jobEvents;
    private Boolean pipelineEvents;
    private Boolean wikiPageEvents;
    private Boolean deploymentEvents;
    private Boolean featureFlagEvents;
    private Boolean releasesEvents;
    private Boolean subgroupEvents;
    private Boolean memberEvents;
    private Boolean enableSslVerification;
    private String alertStatus;
    private Date disabledUntil;
    private Boolean repositoryUpdateEvents;
    private Date createdAt;
    private Boolean resourceAccessTokenEvents;
    private String customWebhookTemplate;

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
     * Returns the url.
     *
     * @return the result
     */

    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     */

    public void setUrl(String url) {
        this.url = url;
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
     * Returns the group id.
     *
     * @return the result
     */

    public Long getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the group id value
     */

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the push events.
     *
     * @return the result
     */

    public Boolean getPushEvents() {
        return pushEvents;
    }

    /**
     * Sets the push events.
     *
     * @param pushEvents the push events value
     */

    public void setPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
    }

    /**
     * Returns the push events branch filter.
     *
     * @return the result
     */

    public String getPushEventsBranchFilter() {
        return pushEventsBranchFilter;
    }

    /**
     * Sets the push events branch filter.
     *
     * @param pushEventsBranchFilter the push events branch filter value
     */

    public void setPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter;
    }

    /**
     * Returns the branch filter strategy.
     *
     * @return the result
     */

    public String getBranchFilterStrategy() {
        return branchFilterStrategy;
    }

    /**
     * Sets the branch filter strategy.
     *
     * @param branchFilterStrategy the branch filter strategy value
     */

    public void setBranchFilterStrategy(String branchFilterStrategy) {
        this.branchFilterStrategy = branchFilterStrategy;
    }

    /**
     * Returns the issues events.
     *
     * @return the result
     */

    public Boolean getIssuesEvents() {
        return issuesEvents;
    }

    /**
     * Sets the issues events.
     *
     * @param issuesEvents the issues events value
     */

    public void setIssuesEvents(Boolean issuesEvents) {
        this.issuesEvents = issuesEvents;
    }

    /**
     * Returns the confidential issues events.
     *
     * @return the result
     */

    public Boolean getConfidentialIssuesEvents() {
        return confidentialIssuesEvents;
    }

    /**
     * Sets the confidential issues events.
     *
     * @param confidentialIssuesEvents the confidential issues events value
     */

    public void setConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
    }

    /**
     * Returns the merge requests events.
     *
     * @return the result
     */

    public Boolean getMergeRequestsEvents() {
        return mergeRequestsEvents;
    }

    /**
     * Sets the merge requests events.
     *
     * @param mergeRequestsEvents the merge requests events value
     */

    public void setMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
    }

    /**
     * Returns the tag push events.
     *
     * @return the result
     */

    public Boolean getTagPushEvents() {
        return tagPushEvents;
    }

    /**
     * Sets the tag push events.
     *
     * @param tagPushEvents the tag push events value
     */

    public void setTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
    }

    /**
     * Returns the note events.
     *
     * @return the result
     */

    public Boolean getNoteEvents() {
        return noteEvents;
    }

    /**
     * Sets the note events.
     *
     * @param noteEvents the note events value
     */

    public void setNoteEvents(Boolean noteEvents) {
        this.noteEvents = noteEvents;
    }

    /**
     * Returns the confidential note events.
     *
     * @return the result
     */

    public Boolean getConfidentialNoteEvents() {
        return confidentialNoteEvents;
    }

    /**
     * Sets the confidential note events.
     *
     * @param confidentialNoteEvents the confidential note events value
     */

    public void setConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents;
    }

    /**
     * Returns the job events.
     *
     * @return the result
     */

    public Boolean getJobEvents() {
        return jobEvents;
    }

    /**
     * Sets the job events.
     *
     * @param jobEvents the job events value
     */

    public void setJobEvents(Boolean jobEvents) {
        this.jobEvents = jobEvents;
    }

    /**
     * Returns the pipeline events.
     *
     * @return the result
     */

    public Boolean getPipelineEvents() {
        return pipelineEvents;
    }

    /**
     * Sets the pipeline events.
     *
     * @param pipelineEvents the pipeline events value
     */

    public void setPipelineEvents(Boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents;
    }

    /**
     * Returns the wiki page events.
     *
     * @return the result
     */

    public Boolean getWikiPageEvents() {
        return wikiPageEvents;
    }

    /**
     * Sets the wiki page events.
     *
     * @param wikiPageEvents the wiki page events value
     */

    public void setWikiPageEvents(Boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents;
    }

    /**
     * Returns the deployment events.
     *
     * @return the result
     */

    public Boolean getDeploymentEvents() {
        return deploymentEvents;
    }

    /**
     * Sets the deployment events.
     *
     * @param deploymentEvents the deployment events value
     */

    public void setDeploymentEvents(Boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents;
    }

    /**
     * Returns the feature flag events.
     *
     * @return the result
     */

    public Boolean getFeatureFlagEvents() {
        return featureFlagEvents;
    }

    /**
     * Sets the feature flag events.
     *
     * @param featureFlagEvents the feature flag events value
     */

    public void setFeatureFlagEvents(Boolean featureFlagEvents) {
        this.featureFlagEvents = featureFlagEvents;
    }

    /**
     * Returns the releases events.
     *
     * @return the result
     */

    public Boolean getReleasesEvents() {
        return releasesEvents;
    }

    /**
     * Sets the releases events.
     *
     * @param releasesEvents the releases events value
     */

    public void setReleasesEvents(Boolean releasesEvents) {
        this.releasesEvents = releasesEvents;
    }

    /**
     * Returns the subgroup events.
     *
     * @return the result
     */

    public Boolean getSubgroupEvents() {
        return subgroupEvents;
    }

    /**
     * Sets the subgroup events.
     *
     * @param subgroupEvents the subgroup events value
     */

    public void setSubgroupEvents(Boolean subgroupEvents) {
        this.subgroupEvents = subgroupEvents;
    }

    /**
     * Returns the member events.
     *
     * @return the result
     */

    public Boolean getMemberEvents() {
        return memberEvents;
    }

    /**
     * Sets the member events.
     *
     * @param memberEvents the member events value
     */

    public void setMemberEvents(Boolean memberEvents) {
        this.memberEvents = memberEvents;
    }

    /**
     * Returns the enable ssl verification.
     *
     * @return the result
     */

    public Boolean getEnableSslVerification() {
        return enableSslVerification;
    }

    /**
     * Sets the enable ssl verification.
     *
     * @param enableSslVerification the enable ssl verification value
     */

    public void setEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
    }

    /**
     * Returns the alert status.
     *
     * @return the result
     */

    public String getAlertStatus() {
        return alertStatus;
    }

    /**
     * Sets the alert status.
     *
     * @param alertStatus the alert status value
     */

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    /**
     * Returns the disabled until.
     *
     * @return the result
     */

    public Date getDisabledUntil() {
        return disabledUntil;
    }

    /**
     * Sets the disabled until.
     *
     * @param disabledUntil the disabled until value
     */

    public void setDisabledUntil(Date disabledUntil) {
        this.disabledUntil = disabledUntil;
    }

    /**
     * Returns the repository update events.
     *
     * @return the result
     */

    public Boolean getRepositoryUpdateEvents() {
        return repositoryUpdateEvents;
    }

    /**
     * Sets the repository update events.
     *
     * @param repositoryUpdateEvents the repository update events value
     */

    public void setRepositoryUpdateEvents(Boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents;
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
     * Returns the resource access token events.
     *
     * @return the result
     */

    public Boolean getResourceAccessTokenEvents() {
        return resourceAccessTokenEvents;
    }

    /**
     * Sets the resource access token events.
     *
     * @param resourceAccessTokenEvents the resource access token events value
     */

    public void setResourceAccessTokenEvents(Boolean resourceAccessTokenEvents) {
        this.resourceAccessTokenEvents = resourceAccessTokenEvents;
    }

    /**
     * Returns the custom webhook template.
     *
     * @return the result
     */

    public String getCustomWebhookTemplate() {
        return customWebhookTemplate;
    }

    /**
     * Sets the custom webhook template.
     *
     * @param customWebhookTemplate the custom webhook template value
     */

    public void setCustomWebhookTemplate(String customWebhookTemplate) {
        this.customWebhookTemplate = customWebhookTemplate;
    }

}
