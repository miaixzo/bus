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

import org.miaixz.bus.gitlab.models.Constants.DeploymentStatus;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The deployable class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Deployable implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251089511L;

    private Long id;
    private DeploymentStatus status;
    private String stage;
    private String name;
    private String ref;
    private Boolean tag;
    private Float coverage;
    private Date createdAt;
    private Date startedAt;
    private Date finishedAt;
    private Double duration;
    private User user;
    private Commit commit;
    private Pipeline pipeline;
    private String webUrl;
    private List<Artifact> artifacts;
    private Runner runner;
    private Date artifactsExpireAt;

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
     * Returns the status.
     *
     * @return the result
     */

    public DeploymentStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(DeploymentStatus status) {
        this.status = status;
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
     * Returns the coverage.
     *
     * @return the result
     */

    public Float getCoverage() {
        return coverage;
    }

    /**
     * Sets the coverage.
     *
     * @param coverage the coverage value
     */

    public void setCoverage(Float coverage) {
        this.coverage = coverage;
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
     * Returns the duration.
     *
     * @return the result
     */

    public Double getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration the duration value
     */

    public void setDuration(Double duration) {
        this.duration = duration;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
