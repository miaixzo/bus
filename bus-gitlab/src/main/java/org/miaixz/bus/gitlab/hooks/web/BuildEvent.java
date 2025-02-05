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
package org.miaixz.bus.gitlab.hooks.web;

import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The documentation at: <a href="https://docs.gitlab.com/ee/user/project/integrations/webhook_events.html#job-events">
 * Job Events</a> is incorrect, this class represents the actual content of the Job Hook event.
 */
public class BuildEvent extends AbstractEvent {

    public static final String JOB_HOOK_X_GITLAB_EVENT = "Job Hook";
    public static final String OBJECT_KIND = "build";
    private static final long serialVersionUID = -1L;
    private String ref;
    private Boolean tag;
    private String beforeSha;
    private String sha;
    private Long buildId;
    private String buildName;
    private String buildStage;
    private String buildStatus;
    private Date buildStartedAt;
    private Date buildFinishedAt;
    private Float buildDuration;

    private Float buildQueuedDuration;
    private Boolean buildAllowFailure;
    private String buildFailureReason;
    private Long projectId;

    private Long pipelineId;
    private String projectName;
    private EventUser user;
    private BuildCommit commit;
    private EventRepository repository;

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Boolean getTag() {
        return tag;
    }

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    public String getBeforeSha() {
        return beforeSha;
    }

    public void setBeforeSha(String beforeSha) {
        this.beforeSha = beforeSha;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildStage() {
        return buildStage;
    }

    public void setBuildStage(String buildStage) {
        this.buildStage = buildStage;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public Date getBuildStartedAt() {
        return buildStartedAt;
    }

    public void setBuildStartedAt(Date buildStartedAt) {
        this.buildStartedAt = buildStartedAt;
    }

    public Date getBuildFinishedAt() {
        return buildFinishedAt;
    }

    public void setBuildFinishedAt(Date buildFinishedAt) {
        this.buildFinishedAt = buildFinishedAt;
    }

    public Float getBuildDuration() {
        return buildDuration;
    }

    public void setBuildDuration(Float buildDuration) {
        this.buildDuration = buildDuration;
    }

    public Float getBuildQueuedDuration() {
        return buildQueuedDuration;
    }

    public void setBuildQueuedDuration(Float buildQueuedDuration) {
        this.buildQueuedDuration = buildQueuedDuration;
    }

    public Boolean getBuildAllowFailure() {
        return buildAllowFailure;
    }

    public void setBuildAllowFailure(Boolean buildAllowFailure) {
        this.buildAllowFailure = buildAllowFailure;
    }

    public String getBuildFailureReason() {
        return buildFailureReason;
    }

    public void setBuildFailureReason(String buildFailureReason) {
        this.buildFailureReason = buildFailureReason;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public EventUser getUser() {
        return user;
    }

    public void setUser(EventUser user) {
        this.user = user;
    }

    public BuildCommit getCommit() {
        return commit;
    }

    public void setCommit(BuildCommit commit) {
        this.commit = commit;
    }

    public EventRepository getRepository() {
        return repository;
    }

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
