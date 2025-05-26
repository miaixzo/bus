/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;
import java.util.Date;
import java.io.Serial;

public class GroupHook implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852278505675L;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Boolean getPushEvents() {
        return pushEvents;
    }

    public void setPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
    }

    public String getPushEventsBranchFilter() {
        return pushEventsBranchFilter;
    }

    public void setPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter;
    }

    public String getBranchFilterStrategy() {
        return branchFilterStrategy;
    }

    public void setBranchFilterStrategy(String branchFilterStrategy) {
        this.branchFilterStrategy = branchFilterStrategy;
    }

    public Boolean getIssuesEvents() {
        return issuesEvents;
    }

    public void setIssuesEvents(Boolean issuesEvents) {
        this.issuesEvents = issuesEvents;
    }

    public Boolean getConfidentialIssuesEvents() {
        return confidentialIssuesEvents;
    }

    public void setConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
    }

    public Boolean getMergeRequestsEvents() {
        return mergeRequestsEvents;
    }

    public void setMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
    }

    public Boolean getTagPushEvents() {
        return tagPushEvents;
    }

    public void setTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
    }

    public Boolean getNoteEvents() {
        return noteEvents;
    }

    public void setNoteEvents(Boolean noteEvents) {
        this.noteEvents = noteEvents;
    }

    public Boolean getConfidentialNoteEvents() {
        return confidentialNoteEvents;
    }

    public void setConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents;
    }

    public Boolean getJobEvents() {
        return jobEvents;
    }

    public void setJobEvents(Boolean jobEvents) {
        this.jobEvents = jobEvents;
    }

    public Boolean getPipelineEvents() {
        return pipelineEvents;
    }

    public void setPipelineEvents(Boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents;
    }

    public Boolean getWikiPageEvents() {
        return wikiPageEvents;
    }

    public void setWikiPageEvents(Boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents;
    }

    public Boolean getDeploymentEvents() {
        return deploymentEvents;
    }

    public void setDeploymentEvents(Boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents;
    }

    public Boolean getFeatureFlagEvents() {
        return featureFlagEvents;
    }

    public void setFeatureFlagEvents(Boolean featureFlagEvents) {
        this.featureFlagEvents = featureFlagEvents;
    }

    public Boolean getReleasesEvents() {
        return releasesEvents;
    }

    public void setReleasesEvents(Boolean releasesEvents) {
        this.releasesEvents = releasesEvents;
    }

    public Boolean getSubgroupEvents() {
        return subgroupEvents;
    }

    public void setSubgroupEvents(Boolean subgroupEvents) {
        this.subgroupEvents = subgroupEvents;
    }

    public Boolean getMemberEvents() {
        return memberEvents;
    }

    public void setMemberEvents(Boolean memberEvents) {
        this.memberEvents = memberEvents;
    }

    public Boolean getEnableSslVerification() {
        return enableSslVerification;
    }

    public void setEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Date getDisabledUntil() {
        return disabledUntil;
    }

    public void setDisabledUntil(Date disabledUntil) {
        this.disabledUntil = disabledUntil;
    }

    public Boolean getRepositoryUpdateEvents() {
        return repositoryUpdateEvents;
    }

    public void setRepositoryUpdateEvents(Boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getResourceAccessTokenEvents() {
        return resourceAccessTokenEvents;
    }

    public void setResourceAccessTokenEvents(Boolean resourceAccessTokenEvents) {
        this.resourceAccessTokenEvents = resourceAccessTokenEvents;
    }

    public String getCustomWebhookTemplate() {
        return customWebhookTemplate;
    }

    public void setCustomWebhookTemplate(String customWebhookTemplate) {
        this.customWebhookTemplate = customWebhookTemplate;
    }

}
