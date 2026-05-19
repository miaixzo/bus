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
 * The pipeline class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Pipeline implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852269321958L;

    private Long id;
    private Long iid;
    private Long projectId;
    private PipelineStatus status;
    private String source;
    private String ref;
    private String sha;
    private String beforeSha;
    private Boolean tag;
    private String yamlErrors;
    private User user;
    private Date createdAt;
    private Date updatedAt;
    private Date startedAt;
    private Date finishedAt;
    private Date committedAt;
    private String coverage;
    private Integer duration;
    private Float queuedDuration;
    private String webUrl;
    private DetailedStatus detailedStatus;
    private String name;

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
     * Returns the iid.
     *
     * @return the result
     */

    public Long getIid() {
        return iid;
    }

    /**
     * Sets the iid.
     *
     * @param iid the iid value
     */

    public void setIid(Long iid) {
        this.iid = iid;
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
     * Returns the status.
     *
     * @return the result
     */

    public PipelineStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    /**
     * Returns the source.
     *
     * @return the result
     */

    public String getSource() {
        return source;
    }

    /**
     * Sets the source.
     *
     * @param source the source value
     */

    public void setSource(String source) {
        this.source = source;
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
     * Returns the yaml errors.
     *
     * @return the result
     */

    public String getYamlErrors() {
        return yamlErrors;
    }

    /**
     * Sets the yaml errors.
     *
     * @param yamlErrors the yaml errors value
     */

    public void setYamlErrors(String yamlErrors) {
        this.yamlErrors = yamlErrors;
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
     * Returns the committed at.
     *
     * @return the result
     */

    public Date getCommittedAt() {
        return committedAt;
    }

    /**
     * Sets the committed at.
     *
     * @param committedAt the committed at value
     */

    public void setCommittedAt(Date committedAt) {
        this.committedAt = committedAt;
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
     * Returns the duration.
     *
     * @return the result
     */

    public Integer getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration the duration value
     */

    public void setDuration(Integer duration) {
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
     * Returns the detailed status.
     *
     * @return the result
     */

    public DetailedStatus getDetailedStatus() {
        return detailedStatus;
    }

    /**
     * Sets the detailed status.
     *
     * @param detailedStatus the detailed status value
     */

    public void setDetailedStatus(DetailedStatus detailedStatus) {
        this.detailedStatus = detailedStatus;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
