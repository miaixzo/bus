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

/**
 * The group hook params class.
 *
 * @author Kimi Liu
 */
public class GroupHookParams implements Serializable {

    /**
     * Constructs a new {@code GroupHookParams} instance.
     */
    public GroupHookParams() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852256256678L;

    /**
     * The url value.
     */
    private String url;
    /**
     * The name value.
     */
    private String name;
    /**
     * The description value.
     */
    private String description;
    /**
     * The push events value.
     */
    private Boolean pushEvents;
    /**
     * The push events branch filter value.
     */
    private String pushEventsBranchFilter;
    /**
     * The branch filter strategy value.
     */
    private String branchFilterStrategy;
    /**
     * The issues events value.
     */
    private Boolean issuesEvents;
    /**
     * The confidential issues events value.
     */
    private Boolean confidentialIssuesEvents;
    /**
     * The merge requests events value.
     */
    private Boolean mergeRequestsEvents;
    /**
     * The tag push events value.
     */
    private Boolean tagPushEvents;
    /**
     * The note events value.
     */
    private Boolean noteEvents;
    /**
     * The confidential note events value.
     */
    private Boolean confidentialNoteEvents;
    /**
     * The job events value.
     */
    private Boolean jobEvents;
    /**
     * The pipeline events value.
     */
    private Boolean pipelineEvents;
    /**
     * The wiki page events value.
     */
    private Boolean wikiPageEvents;
    /**
     * The deployment events value.
     */
    private Boolean deploymentEvents;
    /**
     * The feature flag events value.
     */
    private Boolean featureFlagEvents;
    /**
     * The releases events value.
     */
    private Boolean releasesEvents;
    /**
     * The subgroup events value.
     */
    private Boolean subgroupEvents;
    /**
     * The member events value.
     */
    private Boolean memberEvents;
    /**
     * The enable ssl verification value.
     */
    private Boolean enableSslVerification;
    /**
     * The token value.
     */
    private String token;
    /**
     * The resource access token events value.
     */
    private Boolean resourceAccessTokenEvents;
    /**
     * The custom webhook template value.
     */
    private String customWebhookTemplate;

    /**
     * Returns the form.
     *
     * @return the result
     */

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

    /**
     * Sets the branch filter strategy.
     *
     * @param branchFilterStrategy the branch filter strategy value
     * @return the result
     */

    public GroupHookParams setBranchFilterStrategy(String branchFilterStrategy) {
        this.branchFilterStrategy = branchFilterStrategy;
        return this;
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     * @return the result
     */

    public GroupHookParams setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     * @return the result
     */

    public GroupHookParams setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     * @return the result
     */

    public GroupHookParams setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the push events.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public GroupHookParams setPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
        return this;
    }

    /**
     * Sets the push events branch filter.
     *
     * @param pushEventsBranchFilter the push events branch filter value
     * @return the result
     */

    public GroupHookParams setPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter;
        return this;
    }

    /**
     * Sets the issues events.
     *
     * @param issuesEvents the issues events value
     * @return the result
     */

    public GroupHookParams setIssuesEvents(Boolean issuesEvents) {
        this.issuesEvents = issuesEvents;
        return this;
    }

    /**
     * Sets the confidential issues events.
     *
     * @param confidentialIssuesEvents the confidential issues events value
     * @return the result
     */

    public GroupHookParams setConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
        return this;
    }

    /**
     * Sets the merge requests events.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @return the result
     */

    public GroupHookParams setMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
        return this;
    }

    /**
     * Sets the tag push events.
     *
     * @param tagPushEvents the tag push events value
     * @return the result
     */

    public GroupHookParams setTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
        return this;
    }

    /**
     * Sets the note events.
     *
     * @param noteEvents the note events value
     * @return the result
     */

    public GroupHookParams setNoteEvents(Boolean noteEvents) {
        this.noteEvents = noteEvents;
        return this;
    }

    /**
     * Sets the confidential note events.
     *
     * @param confidentialNoteEvents the confidential note events value
     * @return the result
     */

    public GroupHookParams setConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents;
        return this;
    }

    /**
     * Sets the job events.
     *
     * @param jobEvents the job events value
     * @return the result
     */

    public GroupHookParams setJobEvents(Boolean jobEvents) {
        this.jobEvents = jobEvents;
        return this;
    }

    /**
     * Sets the pipeline events.
     *
     * @param pipelineEvents the pipeline events value
     * @return the result
     */

    public GroupHookParams setPipelineEvents(Boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents;
        return this;
    }

    /**
     * Sets the wiki page events.
     *
     * @param wikiPageEvents the wiki page events value
     * @return the result
     */

    public GroupHookParams setWikiPageEvents(Boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents;
        return this;
    }

    /**
     * Sets the deployment events.
     *
     * @param deploymentEvents the deployment events value
     * @return the result
     */

    public GroupHookParams setDeploymentEvents(Boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents;
        return this;
    }

    /**
     * Sets the feature flag events.
     *
     * @param featureFlagEvents the feature flag events value
     * @return the result
     */

    public GroupHookParams setFeatureFlagEvents(Boolean featureFlagEvents) {
        this.featureFlagEvents = featureFlagEvents;
        return this;
    }

    /**
     * Sets the releases events.
     *
     * @param releasesEvents the releases events value
     * @return the result
     */

    public GroupHookParams setReleasesEvents(Boolean releasesEvents) {
        this.releasesEvents = releasesEvents;
        return this;
    }

    /**
     * Sets the subgroup events.
     *
     * @param subgroupEvents the subgroup events value
     * @return the result
     */

    public GroupHookParams setSubgroupEvents(Boolean subgroupEvents) {
        this.subgroupEvents = subgroupEvents;
        return this;
    }

    /**
     * Sets the member events.
     *
     * @param memberEvents the member events value
     * @return the result
     */

    public GroupHookParams setMemberEvents(Boolean memberEvents) {
        this.memberEvents = memberEvents;
        return this;
    }

    /**
     * Sets the enable ssl verification.
     *
     * @param enableSslVerification the enable ssl verification value
     * @return the result
     */

    public GroupHookParams setEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
        return this;
    }

    /**
     * Sets the token.
     *
     * @param token the token value
     * @return the result
     */

    public GroupHookParams setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Sets the resource access token events.
     *
     * @param resourceAccessTokenEvents the resource access token events value
     * @return the result
     */

    public GroupHookParams setResourceAccessTokenEvents(Boolean resourceAccessTokenEvents) {
        this.resourceAccessTokenEvents = resourceAccessTokenEvents;
        return this;
    }

    /**
     * Sets the custom webhook template.
     *
     * @param customWebhookTemplate the custom webhook template value
     * @return the result
     */

    public GroupHookParams setCustomWebhookTemplate(String customWebhookTemplate) {
        this.customWebhookTemplate = customWebhookTemplate;
        return this;
    }

}
