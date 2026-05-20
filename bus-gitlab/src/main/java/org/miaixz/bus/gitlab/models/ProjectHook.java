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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The project hook class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProjectHook implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852273671982L;

    private Boolean buildEvents;
    private Date createdAt;
    private Boolean enableSslVerification;
    private Long id;
    private Boolean issuesEvents;
    private Boolean mergeRequestsEvents;
    private Boolean noteEvents;
    private Boolean jobEvents;
    private Boolean pipelineEvents;
    private Long projectId;
    private Boolean pushEvents;
    private Boolean tagPushEvents;
    private String url;
    private Boolean wikiPageEvents;
    private String token;

    private Boolean repositoryUpdateEvents;
    private Boolean confidentialIssuesEvents;
    private Boolean confidentialNoteEvents;
    private String pushEventsBranchFilter;

    private Boolean deploymentEvents;
    private Boolean releasesEvents;

    private String description;

    /**
     * Returns the build events.
     *
     * @return the result
     */

    public Boolean getBuildEvents() {
        return buildEvents;
    }

    /**
     * Sets the build events.
     *
     * @param buildEvents the build events value
     */

    public void setBuildEvents(Boolean buildEvents) {
        this.buildEvents = buildEvents;
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
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the project id.
     *
     * @param projectId the project id value
     */

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
     * Returns the token.
     *
     * @return the result
     */

    public String getToken() {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token the token value
     */

    public void setToken(String token) {
        this.token = token;
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
     * @param releasesEvents the releases events value
     */

    public void setDeploymentEvents(Boolean releasesEvents) {
        this.deploymentEvents = releasesEvents;
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
     * Sets the issues events and returns this instance.
     *
     * @param issuesEvents the issues events value
     * @return the result
     */

    public ProjectHook withIssuesEvents(Boolean issuesEvents) {
        this.issuesEvents = issuesEvents;
        return (this);
    }

    /**
     * Sets the merge requests events and returns this instance.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @return the result
     */

    public ProjectHook withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
        return (this);
    }

    /**
     * Sets the note events and returns this instance.
     *
     * @param noteEvents the note events value
     * @return the result
     */

    public ProjectHook withNoteEvents(Boolean noteEvents) {
        this.noteEvents = noteEvents;
        return (this);
    }

    /**
     * Sets the job events and returns this instance.
     *
     * @param jobEvents the job events value
     * @return the result
     */

    public ProjectHook withJobEvents(Boolean jobEvents) {
        this.jobEvents = jobEvents;
        return (this);
    }

    /**
     * Sets the pipeline events and returns this instance.
     *
     * @param pipelineEvents the pipeline events value
     * @return the result
     */

    public ProjectHook withPipelineEvents(Boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents;
        return (this);
    }

    /**
     * Sets the push events and returns this instance.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public ProjectHook withPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
        return (this);
    }

    /**
     * Sets the tag push events and returns this instance.
     *
     * @param tagPushEvents the tag push events value
     * @return the result
     */

    public ProjectHook withTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
        return (this);
    }

    /**
     * Sets the wiki page events and returns this instance.
     *
     * @param wikiPageEvents the wiki page events value
     * @return the result
     */

    public ProjectHook withWikiPageEvents(Boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents;
        return (this);
    }

    /**
     * Sets the repository update events and returns this instance.
     *
     * @param repositoryUpdateEvents the repository update events value
     * @return the result
     */

    public ProjectHook withRepositoryUpdateEvents(Boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents;
        return (this);
    }

    /**
     * Sets the confidential issues events and returns this instance.
     *
     * @param confidentialIssuesEvents the confidential issues events value
     * @return the result
     */

    public ProjectHook withConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents;
        return (this);
    }

    /**
     * Sets the confidential note events and returns this instance.
     *
     * @param confidentialNoteEvents the confidential note events value
     * @return the result
     */

    public ProjectHook withConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents;
        return (this);
    }

    /**
     * Sets the push events branch filter and returns this instance.
     *
     * @param pushEventsBranchFilter the push events branch filter value
     * @return the result
     */

    public ProjectHook withPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter;
        return (this);
    }

    /**
     * Sets the deployment events and returns this instance.
     *
     * @param deploymentEvents the deployment events value
     * @return the result
     */

    public ProjectHook withDeploymentEvents(Boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents;
        return (this);
    }

    /**
     * Sets the releases events and returns this instance.
     *
     * @param releasesEvents the releases events value
     * @return the result
     */

    public ProjectHook withReleasesEvents(Boolean releasesEvents) {
        this.releasesEvents = releasesEvents;
        return (this);
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

}
