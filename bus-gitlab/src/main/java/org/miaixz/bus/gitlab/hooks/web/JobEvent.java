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

import java.io.Serial;
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The job event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JobEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852232651592L;
    /**
     * The job hook x gitlab event value.
     */

    public static final String JOB_HOOK_X_GITLAB_EVENT = "Job Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "job";

    private String ref;
    private Boolean tag;
    private String beforeSha;
    private String sha;
    private Long jobId;
    private String jobName;
    private String jobStage;
    private String jobStatus;
    private Date jobStartedAt;
    private Date jobFinishedAt;
    private Integer jobDuration;
    private Boolean jobAllowFailure;
    private String jobFailureReason;
    private Long projectId;
    private String projectName;
    private EventUser user;
    private BuildCommit commit;
    private EventRepository repository;

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
     * Returns the job id.
     *
     * @return the result
     */

    public Long getJobId() {
        return jobId;
    }

    /**
     * Sets the job id.
     *
     * @param jobId the job id value
     */

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * Returns the job name.
     *
     * @return the result
     */

    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the job name.
     *
     * @param jobName the job name value
     */

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Returns the job stage.
     *
     * @return the result
     */

    public String getJobStage() {
        return jobStage;
    }

    /**
     * Sets the job stage.
     *
     * @param jobStage the job stage value
     */

    public void setJobStage(String jobStage) {
        this.jobStage = jobStage;
    }

    /**
     * Returns the job status.
     *
     * @return the result
     */

    public String getJobStatus() {
        return jobStatus;
    }

    /**
     * Sets the job status.
     *
     * @param jobStatus the job status value
     */

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    /**
     * Returns the job started at.
     *
     * @return the result
     */

    public Date getJobStartedAt() {
        return jobStartedAt;
    }

    /**
     * Sets the job started at.
     *
     * @param jobStartedAt the job started at value
     */

    public void setJobStartedAt(Date jobStartedAt) {
        this.jobStartedAt = jobStartedAt;
    }

    /**
     * Returns the job finished at.
     *
     * @return the result
     */

    public Date getJobFinishedAt() {
        return jobFinishedAt;
    }

    /**
     * Sets the job finished at.
     *
     * @param jobFinishedAt the job finished at value
     */

    public void setJobFinishedAt(Date jobFinishedAt) {
        this.jobFinishedAt = jobFinishedAt;
    }

    /**
     * Returns the job duration.
     *
     * @return the result
     */

    public Integer getJobDuration() {
        return jobDuration;
    }

    /**
     * Sets the job duration.
     *
     * @param jobDuration the job duration value
     */

    public void setJobDuration(Integer jobDuration) {
        this.jobDuration = jobDuration;
    }

    /**
     * Returns the job allow failure.
     *
     * @return the result
     */

    public Boolean getJobAllowFailure() {
        return jobAllowFailure;
    }

    /**
     * Sets the job allow failure.
     *
     * @param jobAllowFailure the job allow failure value
     */

    public void setJobAllowFailure(Boolean jobAllowFailure) {
        this.jobAllowFailure = jobAllowFailure;
    }

    /**
     * Returns the job failure reason.
     *
     * @return the result
     */

    public String getJobFailureReason() {
        return jobFailureReason;
    }

    /**
     * Sets the job failure reason.
     *
     * @param jobFailureReason the job failure reason value
     */

    public void setJobFailureReason(String jobFailureReason) {
        this.jobFailureReason = jobFailureReason;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
