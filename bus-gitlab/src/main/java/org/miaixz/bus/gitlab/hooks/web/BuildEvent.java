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
package org.miaixz.bus.gitlab.hooks.web;

import java.util.Date;

import org.miaixz.bus.gitlab.models.Runner;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The documentation at: <a href="https://docs.gitlab.com/ee/user/project/integrations/webhook_events.html#job-events">
 * Job Events</a> is incorrect, this class represents the actual content of the Job Hook event.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BuildEvent extends AbstractEvent {

    private static final long serialVersionUID = 1L;
    /**
     * The job hook x gitlab event value.
     */

    public static final String JOB_HOOK_X_GITLAB_EVENT = "Job Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "build";

    private String ref;
    private Boolean tag;
    private String beforeSha;
    private String sha;
    private Integer retriesCount;
    private Long buildId;
    private String buildName;
    private String buildStage;
    private String buildStatus;
    private Date buildCreatedAt;
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
    private EventProject project;
    private Runner runner;

    private EventEnvironment environment;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Returns the tag.
     *
     * @return the result
     */

    public Boolean getTag() {
        return tag;
    }

    /**
     * Sets the tag.
     *
     * @param tag the tag value
     */

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    /**
     * Returns the before sha.
     *
     * @return the result
     */

    public String getBeforeSha() {
        return beforeSha;
    }

    /**
     * Sets the before sha.
     *
     * @param beforeSha the before sha value
     */

    public void setBeforeSha(String beforeSha) {
        this.beforeSha = beforeSha;
    }

    /**
     * Returns the sha.
     *
     * @return the result
     */

    public String getSha() {
        return sha;
    }

    /**
     * Sets the sha.
     *
     * @param sha the sha value
     */

    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * Returns the retries count.
     *
     * @return the result
     */

    public Integer getRetriesCount() {
        return retriesCount;
    }

    /**
     * Sets the retries count.
     *
     * @param retriesCount the retries count value
     */

    public void setRetriesCount(Integer retriesCount) {
        this.retriesCount = retriesCount;
    }

    /**
     * Returns the build id.
     *
     * @return the result
     */

    public Long getBuildId() {
        return buildId;
    }

    /**
     * Sets the build id.
     *
     * @param buildId the build id value
     */

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    /**
     * Returns the build name.
     *
     * @return the result
     */

    public String getBuildName() {
        return buildName;
    }

    /**
     * Sets the build name.
     *
     * @param buildName the build name value
     */

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    /**
     * Returns the build stage.
     *
     * @return the result
     */

    public String getBuildStage() {
        return buildStage;
    }

    /**
     * Sets the build stage.
     *
     * @param buildStage the build stage value
     */

    public void setBuildStage(String buildStage) {
        this.buildStage = buildStage;
    }

    /**
     * Returns the build status.
     *
     * @return the result
     */

    public String getBuildStatus() {
        return buildStatus;
    }

    /**
     * Sets the build status.
     *
     * @param buildStatus the build status value
     */

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    /**
     * Returns the build created at.
     *
     * @return the result
     */

    public Date getBuildCreatedAt() {
        return buildCreatedAt;
    }

    /**
     * Sets the build created at.
     *
     * @param buildCreatedAt the build created at value
     */

    public void setBuildCreatedAt(Date buildCreatedAt) {
        this.buildCreatedAt = buildCreatedAt;
    }

    /**
     * Returns the build started at.
     *
     * @return the result
     */

    public Date getBuildStartedAt() {
        return buildStartedAt;
    }

    /**
     * Sets the build started at.
     *
     * @param buildStartedAt the build started at value
     */

    public void setBuildStartedAt(Date buildStartedAt) {
        this.buildStartedAt = buildStartedAt;
    }

    /**
     * Returns the build finished at.
     *
     * @return the result
     */

    public Date getBuildFinishedAt() {
        return buildFinishedAt;
    }

    /**
     * Sets the build finished at.
     *
     * @param buildFinishedAt the build finished at value
     */

    public void setBuildFinishedAt(Date buildFinishedAt) {
        this.buildFinishedAt = buildFinishedAt;
    }

    /**
     * Returns the build duration.
     *
     * @return the result
     */

    public Float getBuildDuration() {
        return buildDuration;
    }

    /**
     * Sets the build duration.
     *
     * @param buildDuration the build duration value
     */

    public void setBuildDuration(Float buildDuration) {
        this.buildDuration = buildDuration;
    }

    /**
     * Returns the build queued duration.
     *
     * @return the result
     */

    public Float getBuildQueuedDuration() {
        return buildQueuedDuration;
    }

    /**
     * Sets the build queued duration.
     *
     * @param buildQueuedDuration the build queued duration value
     */

    public void setBuildQueuedDuration(Float buildQueuedDuration) {
        this.buildQueuedDuration = buildQueuedDuration;
    }

    /**
     * Returns the build allow failure.
     *
     * @return the result
     */

    public Boolean getBuildAllowFailure() {
        return buildAllowFailure;
    }

    /**
     * Sets the build allow failure.
     *
     * @param buildAllowFailure the build allow failure value
     */

    public void setBuildAllowFailure(Boolean buildAllowFailure) {
        this.buildAllowFailure = buildAllowFailure;
    }

    /**
     * Returns the build failure reason.
     *
     * @return the result
     */

    public String getBuildFailureReason() {
        return buildFailureReason;
    }

    /**
     * Sets the build failure reason.
     *
     * @param buildFailureReason the build failure reason value
     */

    public void setBuildFailureReason(String buildFailureReason) {
        this.buildFailureReason = buildFailureReason;
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
     * Returns the pipeline id.
     *
     * @return the result
     */

    public Long getPipelineId() {
        return pipelineId;
    }

    /**
     * Sets the pipeline id.
     *
     * @param pipelineId the pipeline id value
     */

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    /**
     * Returns the project name.
     *
     * @return the result
     */

    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name value
     */

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the user.
     *
     * @return the result
     */

    public EventUser getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(EventUser user) {
        this.user = user;
    }

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public BuildCommit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(BuildCommit commit) {
        this.commit = commit;
    }

    /**
     * Returns the repository.
     *
     * @return the result
     */

    public EventRepository getRepository() {
        return repository;
    }

    /**
     * Sets the repository.
     *
     * @param repository the repository value
     */

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the runner.
     *
     * @return the result
     */

    public Runner getRunner() {
        return runner;
    }

    /**
     * Sets the runner.
     *
     * @param runner the runner value
     */

    public void setRunner(Runner runner) {
        this.runner = runner;
    }

    /**
     * Returns the environment.
     *
     * @return the result
     */

    public EventEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment.
     *
     * @param environment the environment value
     */

    public void setEnvironment(EventEnvironment environment) {
        this.environment = environment;
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
