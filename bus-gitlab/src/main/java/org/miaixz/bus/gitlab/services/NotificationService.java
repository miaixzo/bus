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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.GitLabForm;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The notification service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class NotificationService implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852285332559L;
    /**
     * The webhook prop value.
     */

    public static final String WEBHOOK_PROP = "webhook";
    /**
     * The notify only broken pipelines prop value.
     */
    public static final String NOTIFY_ONLY_BROKEN_PIPELINES_PROP = "notify_only_broken_pipelines";
    /**
     * The notify only default branch prop value.
     */
    public static final String NOTIFY_ONLY_DEFAULT_BRANCH_PROP = "notify_only_default_branch";
    /**
     * The branches to be notified prop value.
     */
    public static final String BRANCHES_TO_BE_NOTIFIED_PROP = "branches_to_be_notified";
    /**
     * The push channel prop value.
     */
    public static final String PUSH_CHANNEL_PROP = "push_channel";
    /**
     * The issue channel prop value.
     */
    public static final String ISSUE_CHANNEL_PROP = "issue_channel";
    /**
     * The confidential issue channel prop value.
     */
    public static final String CONFIDENTIAL_ISSUE_CHANNEL_PROP = "confidential_issue_channel";
    /**
     * The merge request channel prop value.
     */
    public static final String MERGE_REQUEST_CHANNEL_PROP = "merge_request_channel";
    /**
     * The note channel prop value.
     */
    public static final String NOTE_CHANNEL_PROP = "note_channel";
    /**
     * The confidential note channel prop value.
     */
    public static final String CONFIDENTIAL_NOTE_CHANNEL_PROP = "confidential_note_channel";
    /**
     * The tag push channel prop value.
     */
    public static final String TAG_PUSH_CHANNEL_PROP = "tag_push_channel";
    /**
     * The pipeline channel prop value.
     */
    public static final String PIPELINE_CHANNEL_PROP = "pipeline_channel";
    /**
     * The wiki page channel prop value.
     */
    public static final String WIKI_PAGE_CHANNEL_PROP = "wiki_page_channel";
    /**
     * The username prop value.
     */
    public static final String USERNAME_PROP = "username";
    /**
     * The description prop value.
     */
    public static final String DESCRIPTION_PROP = "description";
    /**
     * The title prop value.
     */
    public static final String TITLE_PROP = "title";
    /**
     * The new issue url prop value.
     */
    public static final String NEW_ISSUE_URL_PROP = "new_issue_url";
    /**
     * The issues url prop value.
     */
    public static final String ISSUES_URL_PROP = "issues_url";
    /**
     * The project url prop value.
     */
    public static final String PROJECT_URL_PROP = "project_url";
    /**
     * The push events prop value.
     */
    public static final String PUSH_EVENTS_PROP = "push_events";

    private Long id;
    private String title;
    private String slug;
    private Date createdAt;
    private Date updatedAt;
    private Boolean active;

    private Boolean commitEvents;
    private Boolean pushEvents;
    private Boolean issuesEvents;
    private Boolean confidentialIssuesEvents;
    private Boolean mergeRequestsEvents;
    private Boolean tagPushEvents;
    private Boolean noteEvents;
    private Boolean confidentialNoteEvents;
    private Boolean pipelineEvents;
    private Boolean wikiPageEvents;
    private Boolean jobEvents;

    private Map<String, Object> properties;

    /**
     * Executes the service properties form operation.
     *
     * @return the result
     */

    public abstract GitLabForm servicePropertiesForm();

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
     * Returns the slug.
     *
     * @return the result
     */

    public String getSlug() {
        return slug;
    }

    /**
     * Sets the slug.
     *
     * @param slug the slug value
     */

    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(String title) {
        this.title = title;
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
     * Returns the active.
     *
     * @return the result
     */

    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the active value
     */

    public void setActive(Boolean active) {
        this.active = active;
    }

    // *******************************************************************************
    // The following methods can be used to configure the notification service
    // *******************************************************************************
    /**
     * Returns the commit events.
     *
     * @return the result
     */

    public Boolean getCommitEvents() {
        return commitEvents;
    }

    /**
     * Sets the commit events.
     *
     * @param commitEvents the commit events value
     */

    public void setCommitEvents(Boolean commitEvents) {
        this.commitEvents = commitEvents;
    }

    /**
     * Sets the commit events and returns this instance.
     *
     * @param commitEvents    the commit events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withCommitEvents(Boolean commitEvents, T derivedInstance) {
        this.commitEvents = commitEvents;
        return (derivedInstance);
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
     * Sets the push events and returns this instance.
     *
     * @param pushEvents      the push events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withPushEvents(Boolean pushEvents, T derivedInstance) {
        this.pushEvents = pushEvents;
        return (derivedInstance);
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
     * Sets the issues events and returns this instance.
     *
     * @param issuesEvents    the issues events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withIssuesEvents(Boolean issuesEvents, T derivedInstance) {
        this.issuesEvents = issuesEvents;
        return (derivedInstance);
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
     * Sets the confidential issues events and returns this instance.
     *
     * @param confidentialIssuesEvents the confidential issues events value
     * @param derivedInstance          the derived instance value
     * @return the result
     */

    protected <T> T withConfidentialIssuesEvents(Boolean confidentialIssuesEvents, T derivedInstance) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
        return (derivedInstance);
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
     * Sets the merge requests events and returns this instance.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @param derivedInstance     the derived instance value
     * @return the result
     */

    protected <T> T withMergeRequestsEvents(Boolean mergeRequestsEvents, T derivedInstance) {
        this.mergeRequestsEvents = mergeRequestsEvents;
        return (derivedInstance);
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
     * Sets the tag push events and returns this instance.
     *
     * @param tagPushEvents   the tag push events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withTagPushEvents(Boolean tagPushEvents, T derivedInstance) {
        this.tagPushEvents = tagPushEvents;
        return (derivedInstance);
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
     * Sets the note events and returns this instance.
     *
     * @param noteEvents      the note events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withNoteEvents(Boolean noteEvents, T derivedInstance) {
        this.noteEvents = noteEvents;
        return (derivedInstance);
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
     * Sets the confidential note events and returns this instance.
     *
     * @param confidentialNoteEvents the confidential note events value
     * @param derivedInstance        the derived instance value
     * @return the result
     */

    protected <T> T withConfidentialNoteEvents(Boolean confidentialNoteEvents, T derivedInstance) {
        this.confidentialNoteEvents = confidentialNoteEvents;
        return (derivedInstance);
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
     * Sets the pipeline events and returns this instance.
     *
     * @param pipelineEvents  the pipeline events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withPipelineEvents(Boolean pipelineEvents, T derivedInstance) {
        this.pipelineEvents = pipelineEvents;
        return (derivedInstance);
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
     * Sets the wiki page events and returns this instance.
     *
     * @param wikiPageEvents  the wiki page events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withWikiPageEvents(Boolean wikiPageEvents, T derivedInstance) {
        this.wikiPageEvents = wikiPageEvents;
        return (derivedInstance);
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
     * Sets the job events and returns this instance.
     *
     * @param jobEvents       the job events value
     * @param derivedInstance the derived instance value
     * @return the result
     */

    protected <T> T withJobEvents(Boolean jobEvents, T derivedInstance) {
        this.jobEvents = jobEvents;
        return (derivedInstance);
    }

    /**
     * Returns the properties.
     *
     * @return the result
     */

    public Map<String, Object> getProperties() {
        return (properties);
    }

    /**
     * Sets the properties.
     *
     * @param properties the properties value
     */

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Returns the property.
     *
     * @param prop the prop value
     * @return the result
     */

    @JsonIgnore
    protected String getProperty(String prop) {
        return (getProperty(prop, ""));
    }

    /**
     * Returns the property.
     *
     * @param prop         the prop value
     * @param defaultValue the default value value
     * @return the result
     */

    @JsonIgnore
    protected <T> T getProperty(String prop, T defaultValue) {

        Object value = (properties != null ? properties.get(prop) : null);

        // HACK: Sometimes GitLab returns "0" or "1" for true/false
        if (value != null && Boolean.class.isInstance(defaultValue)) {
            if ("0".equals(value)) {
                return ((T) Boolean.FALSE);
            } else if ("1".equals(value)) {
                return ((T) Boolean.TRUE);
            }
        }

        return ((T) (value != null ? value : defaultValue));
    }

    /**
     * Sets the property.
     *
     * @param prop  the prop value
     * @param value the value value
     */

    protected void setProperty(String prop, Object value) {
        if (properties == null) {
            properties = new HashMap<>(16);
        }

        properties.put(prop, value);
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
     * The branches to be notified enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum BranchesToBeNotified {

        /**
         * The all branches to be notified.
         */
        ALL,
        /**
         * The default branches to be notified.
         */
        DEFAULT,
        /**
         * The protected branches to be notified.
         */
        PROTECTED,
        /**
         * The default and protected branches to be notified.
         */
        DEFAULT_AND_PROTECTED;

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (name().toLowerCase());
        }

    }

}
