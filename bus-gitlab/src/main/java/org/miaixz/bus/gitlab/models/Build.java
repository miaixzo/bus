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

import java.util.Date;

import org.miaixz.bus.gitlab.hooks.web.EventEnvironment;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The build class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Build {

    private Long id;
    private String stage;
    private String name;
    private BuildStatus status;
    private Date createdAt;
    private Date startedAt;
    private Date finishedAt;
    private Float duration;
    private Float queuedDuration;
    private String failureReason;
    private String when;
    private Boolean manual;
    private Boolean allowFailure;
    private User user;
    private Runner runner;
    private ArtifactsFile artifactsFile;
    private EventEnvironment environment;

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
     * Returns the status.
     *
     * @return the result
     */

    public BuildStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(BuildStatus status) {
        this.status = status;
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
     * Returns the failure reason.
     *
     * @return the result
     */

    public String getFailureReason() {
        return failureReason;
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
