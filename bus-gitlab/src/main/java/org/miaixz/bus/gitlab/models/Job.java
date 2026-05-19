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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The job class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Job implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852259736306L;

    private Long id;
    private Commit commit;
    private String coverage;
    private Date createdAt;
    private Date finishedAt;
    private Date erasedAt;
    private Date artifactsExpireAt;
    private String name;
    private Pipeline pipeline;
    private String ref;
    private Runner runner;
    private User user;
    private Date startedAt;
    private ArtifactsFile artifactsFile;
    private List<Artifact> artifacts;
    private Boolean tag;
    private List<String> tagList;
    private String webUrl;
    private String stage;
    private JobStatus status;
    private String failureReason;
    private String when;
    private Boolean manual;
    private Boolean allowFailure;
    private Float duration;
    private Float queuedDuration;
    private Project project;

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
     * Returns the commit.
     *
     * @return the result
     */

    public Commit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(Commit commit) {
        this.commit = commit;
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
     * Returns the finished at.
     *
     * @return the result
     */

    public Date getFinishedAt() {
        return finishedAt;
    }

    /**
     * Sets the finished at.
     *
     * @param finishedAt the finished at value
     */

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * When someone deletes job using <a href="https://docs.gitlab.com/ee/api/jobs.html#erase-a-job">job erase api</a>,
     * you can detect it using this field. Normally erasing job does mean only that job artifacts and a job logs gets
     * removed. Job metadata (started_at, duration, ....) stays in place.
     * <p>
     * You can use this attribute to filter out such jobs, that have erased at non-null if you need to.
     *
     * @return the result
     */
    public Date getErasedAt() {
        return erasedAt;
    }

    /**
     * Sets the erased at.
     *
     * @param erasedAt the erased at value
     */

    public void setErasedAt(Date erasedAt) {
        this.erasedAt = erasedAt;
    }

    /**
     * Returns the artifacts expire at.
     *
     * @return the result
     */

    public Date getArtifactsExpireAt() {
        return artifactsExpireAt;
    }

    /**
     * Sets the artifacts expire at.
     *
     * @param artifactsExpireAt the artifacts expire at value
     */

    public void setArtifactsExpireAt(Date artifactsExpireAt) {
        this.artifactsExpireAt = artifactsExpireAt;
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
     * Returns the pipeline.
     *
     * @return the result
     */

    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Sets the pipeline.
     *
     * @param pipeline the pipeline value
     */

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
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
     * Returns the user.
     *
     * @return the result
     */

    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the started at.
     *
     * @return the result
     */

    public Date getStartedAt() {
        return startedAt;
    }

    /**
     * Sets the started at.
     *
     * @param startedAt the started at value
     */

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
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
     * Returns the tag list.
     *
     * @return the result
     */

    public List<String> getTagList() {
        return tagList;
    }

    /**
     * Sets the tag list.
     *
     * @param tagList the tag list value
     */

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
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
     * Returns the stage.
     *
     * @return the result
     */

    public String getStage() {
        return stage;
    }

    /**
     * Sets the stage.
     *
     * @param stage the stage value
     */

    public void setStage(String stage) {
        this.stage = stage;
    }

    /**
     * Returns the status.
     *
     * @return the result
     */

    public JobStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * Returns the failure reason.
     *
     * @return the result
     */

    public String getFailureReason() {
        return this.failureReason;
    }

    /**
     * Sets the failure reason.
     *
     * @param failureReason the failure reason value
     */

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * Returns the coverage.
     *
     * @return the result
     */

    public String getCoverage() {
        return coverage;
    }

    /**
     * Sets the coverage.
     *
     * @param coverage the coverage value
     */

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    /**
     * Returns the artifacts file.
     *
     * @return the result
     */

    public ArtifactsFile getArtifactsFile() {
        return artifactsFile;
    }

    /**
     * Sets the artifacts file.
     *
     * @param artifactsFile the artifacts file value
     */

    public void setArtifactsFile(ArtifactsFile artifactsFile) {
        this.artifactsFile = artifactsFile;
    }

    /**
     * Returns the artifacts.
     *
     * @return the result
     */

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    /**
     * Sets the artifacts.
     *
     * @param artifacts the artifacts value
     */

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
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
     * Returns the when.
     *
     * @return the result
     */

    public String getWhen() {
        return when;
    }

    /**
     * Sets the when.
     *
     * @param when the when value
     */

    public void setWhen(String when) {
        this.when = when;
    }

    /**
     * Returns the manual.
     *
     * @return the result
     */

    public Boolean getManual() {
        return manual;
    }

    /**
     * Sets the manual.
     *
     * @param manual the manual value
     */

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    /**
     * Returns the allow failure.
     *
     * @return the result
     */

    public Boolean getAllowFailure() {
        return allowFailure;
    }

    /**
     * Sets the allow failure.
     *
     * @param allowFailure the allow failure value
     */

    public void setAllowFailure(Boolean allowFailure) {
        this.allowFailure = allowFailure;
    }

    /**
     * Returns the duration.
     *
     * @return the result
     */

    public Float getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration the duration value
     */

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    /**
     * Returns the queued duration.
     *
     * @return the result
     */

    public Float getQueuedDuration() {
        return queuedDuration;
    }

    /**
     * Sets the queued duration.
     *
     * @param queuedDuration the queued duration value
     */

    public void setQueuedDuration(Float queuedDuration) {
        this.queuedDuration = queuedDuration;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public Project getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public Job withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the commit and returns this instance.
     *
     * @param commit the commit value
     * @return the result
     */

    public Job withCommit(Commit commit) {
        this.commit = commit;
        return this;
    }

    /**
     * Sets the coverage and returns this instance.
     *
     * @param coverage the coverage value
     * @return the result
     */

    public Job withCoverage(String coverage) {
        this.coverage = coverage;
        return this;
    }

    /**
     * Sets the created at and returns this instance.
     *
     * @param createdAt the created at value
     * @return the result
     */

    public Job withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Sets the finished at and returns this instance.
     *
     * @param finishedAt the finished at value
     * @return the result
     */

    public Job withFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    /**
     * Sets the erased at and returns this instance.
     *
     * @param erasedAt the erased at value
     * @return the result
     */

    public Job withErasedAt(Date erasedAt) {
        this.erasedAt = erasedAt;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Job withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the pipeline and returns this instance.
     *
     * @param pipeline the pipeline value
     * @return the result
     */

    public Job withPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public Job withRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Sets the runner and returns this instance.
     *
     * @param runner the runner value
     * @return the result
     */

    public Job withRunner(Runner runner) {
        this.runner = runner;
        return this;
    }

    /**
     * Sets the user and returns this instance.
     *
     * @param user the user value
     * @return the result
     */

    public Job withUser(User user) {
        this.user = user;
        return this;
    }

    /**
     * Sets the started at and returns this instance.
     *
     * @param startedAt the started at value
     * @return the result
     */

    public Job withStartedAt(Date startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    /**
     * Sets the artifacts file and returns this instance.
     *
     * @param artifactsFile the artifacts file value
     * @return the result
     */

    public Job withArtifactsFile(ArtifactsFile artifactsFile) {
        this.artifactsFile = artifactsFile;
        return this;
    }

    /**
     * Sets the tag and returns this instance.
     *
     * @param tag the tag value
     * @return the result
     */

    public Job withTag(Boolean tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Sets the stage and returns this instance.
     *
     * @param stage the stage value
     * @return the result
     */

    public Job withStage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * Sets the status and returns this instance.
     *
     * @param status the status value
     * @return the result
     */

    public Job withStatus(JobStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the when and returns this instance.
     *
     * @param when the when value
     * @return the result
     */

    public Job withWhen(String when) {
        this.when = when;
        return this;
    }

    /**
     * Sets the manual and returns this instance.
     *
     * @param manual the manual value
     * @return the result
     */

    public Job withManual(Boolean manual) {
        this.manual = manual;
        return this;
    }

    /**
     * Sets the allow failure and returns this instance.
     *
     * @param allowFailure the allow failure value
     * @return the result
     */

    public Job withAllowFailure(Boolean allowFailure) {
        this.allowFailure = allowFailure;
        return this;
    }

    /**
     * Sets the duration and returns this instance.
     *
     * @param duration the duration value
     * @return the result
     */

    public Job withDuration(Float duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Sets the queued duration and returns this instance.
     *
     * @param queuedDuration the queued duration value
     * @return the result
     */

    public Job withQueuedDuration(Float queuedDuration) {
        this.queuedDuration = queuedDuration;
        return this;
    }

    /**
     * Sets the project and returns this instance.
     *
     * @param project the project value
     * @return the result
     */

    public Job withProject(Project project) {
        this.project = project;
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

}
