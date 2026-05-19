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
 * The commit status class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommitStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852250191072L;

    private Boolean allowFailure;
    private Author author;
    private Float coverage;
    private Date createdAt;
    private String description;
    private Date finishedAt;
    private Long id;
    private String name;
    private Long pipelineId;
    private String ref;
    private String sha;
    private Date startedAt;
    private String status;
    private String targetUrl;

    /**
     * Returns whether the allow failure is enabled.
     *
     * @return the result
     */

    public Boolean isAllowFailure() {
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
     * Returns the author.
     *
     * @return the result
     */

    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the author value
     */

    public void setAuthor(Author author) {
        this.author = author;
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
     * Returns the finished at.
     *
     * @return the result
     */

    public Date getFinishedAt() {
        return this.finishedAt;
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
     * Returns the status.
     *
     * @return the result
     */

    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the target url.
     *
     * @return the result
     */

    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * Sets the target url.
     *
     * @param targetUrl the target url value
     */

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * Sets the coverage and returns this instance.
     *
     * @param coverage the coverage value
     * @return the result
     */

    public CommitStatus withCoverage(Float coverage) {
        this.coverage = coverage;
        return this;
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public CommitStatus withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public CommitStatus withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the pipeline id and returns this instance.
     *
     * @param pipelineId the pipeline id value
     * @return the result
     */

    public CommitStatus withPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
        return this;
    }

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public CommitStatus withRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Sets the target url and returns this instance.
     *
     * @param targetUrl the target url value
     * @return the result
     */

    public CommitStatus withTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
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
