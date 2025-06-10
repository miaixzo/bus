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
import java.io.Serial;

public class GroupHookParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256256678L;

    private String url;
    private String name;
    private String description;
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
    private String token;
    private Boolean resourceAccessTokenEvents;
    private String customWebhookTemplate;

    public GitLabForm getForm() {

        return new GitLabForm().withParam("url", url, true).withParam("name", name)
                .withParam("description", description).withParam("push_events", pushEvents)
                .withParam("push_events_branch_filter", pushEventsBranchFilter)
                .withParam("branch_filter_strategy", branchFilterStrategy).withParam("issues_events", issuesEvents)
                .withParam("confidential_issues_events", confidentialIssuesEvents)
                .withParam("merge_requests_events", mergeRequestsEvents).withParam("tag_push_events", tagPushEvents)
                .withParam("note_events", noteEvents).withParam("confidential_note_events", confidentialNoteEvents)
                .withParam("job_events", jobEvents).withParam("pipeline_events", pipelineEvents)
                .withParam("wiki_page_events", wikiPageEvents).withParam("deployment_events", deploymentEvents)
                .withParam("feature_flag_events", featureFlagEvents).withParam("releases_events", releasesEvents)
                .withParam("subgroup_events", subgroupEvents).withParam("member_events", memberEvents)
                .withParam("enable_ssl_verification", enableSslVerification).withParam("token", token)
                .withParam("resource_access_token_events", resourceAccessTokenEvents)
                .withParam("custom_webhook_template", customWebhookTemplate);
    }

    public GroupHookParams setBranchFilterStrategy(String branchFilterStrategy) {
        this.branchFilterStrategy = branchFilterStrategy;
        return this;
    }

    public GroupHookParams setUrl(String url) {
        this.url = url;
        return this;
    }

    public GroupHookParams setName(String name) {
        this.name = name;
        return this;
    }

    public GroupHookParams setDescription(String description) {
        this.description = description;
        return this;
    }

    public GroupHookParams setPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
        return this;
    }

    public GroupHookParams setPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter;
        return this;
    }

    public GroupHookParams setIssuesEvents(Boolean issuesEvents) {
        this.issuesEvents = issuesEvents;
        return this;
    }

    public GroupHookParams setConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
        return this;
    }

    public GroupHookParams setMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
        return this;
    }

    public GroupHookParams setTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
        return this;
    }

    public GroupHookParams setNoteEvents(Boolean noteEvents) {
        this.noteEvents = noteEvents;
        return this;
    }

    public GroupHookParams setConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents;
        return this;
    }

    public GroupHookParams setJobEvents(Boolean jobEvents) {
        this.jobEvents = jobEvents;
        return this;
    }

    public GroupHookParams setPipelineEvents(Boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents;
        return this;
    }

    public GroupHookParams setWikiPageEvents(Boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents;
        return this;
    }

    public GroupHookParams setDeploymentEvents(Boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents;
        return this;
    }

    public GroupHookParams setFeatureFlagEvents(Boolean featureFlagEvents) {
        this.featureFlagEvents = featureFlagEvents;
        return this;
    }

    public GroupHookParams setReleasesEvents(Boolean releasesEvents) {
        this.releasesEvents = releasesEvents;
        return this;
    }

    public GroupHookParams setSubgroupEvents(Boolean subgroupEvents) {
        this.subgroupEvents = subgroupEvents;
        return this;
    }

    public GroupHookParams setMemberEvents(Boolean memberEvents) {
        this.memberEvents = memberEvents;
        return this;
    }

    public GroupHookParams setEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
        return this;
    }

    public GroupHookParams setToken(String token) {
        this.token = token;
        return this;
    }

    public GroupHookParams setResourceAccessTokenEvents(Boolean resourceAccessTokenEvents) {
        this.resourceAccessTokenEvents = resourceAccessTokenEvents;
        return this;
    }

    public GroupHookParams setCustomWebhookTemplate(String customWebhookTemplate) {
        this.customWebhookTemplate = customWebhookTemplate;
        return this;
    }

}
