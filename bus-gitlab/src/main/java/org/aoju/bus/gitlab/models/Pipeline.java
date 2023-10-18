/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org Greg Messner and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.aoju.bus.gitlab.support.JacksonJson;

import java.util.Date;

public class Pipeline {

    private Long id;
    private PipelineStatus status;
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
    private String webUrl;
    private DetailedStatus detailedStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getBeforeSha() {
        return beforeSha;
    }

    public void setBeforeSha(String beforeSha) {
        this.beforeSha = beforeSha;
    }

    public Boolean getTag() {
        return tag;
    }

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    public String getYamlErrors() {
        return yamlErrors;
    }

    public void setYamlErrors(String yamlErrors) {
        this.yamlErrors = yamlErrors;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updatedAt = updated_at;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date started_at) {
        this.startedAt = started_at;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finished_at) {
        this.finishedAt = finished_at;
    }

    public Date getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(Date committed_at) {
        this.committedAt = committed_at;
    }

    /**
     * @return the updated at Date
     * @deprecated Replaced by {@link #getUpdatedAt()}
     */
    @Deprecated
    @JsonIgnore
    public Date getUpdated_at() {
        return updatedAt;
    }

    /**
     * @param updatedAt new updated at value
     * @deprecated Replaced by {@link #setUpdatedAt(Date)}
     */
    @Deprecated
    @JsonIgnore
    public void setUpdated_at(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return the started at Date
     * @deprecated Replaced by {@link #getStartedAt()}
     */
    @Deprecated
    @JsonIgnore
    public Date getStarted_at() {
        return startedAt;
    }

    /**
     * @param startedAt new started at value
     * @deprecated Replaced by {@link #setStartedAt(Date)}
     */
    @Deprecated
    @JsonIgnore
    public void setStarted_at(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * @return the finished at Date
     * @deprecated Replaced by {@link #getFinishedAt()}
     */
    @Deprecated
    @JsonIgnore
    public Date getFinished_at() {
        return finishedAt;
    }

    /**
     * @param finishedAt new finished at value
     * @deprecated Replaced by {@link #setFinishedAt(Date)}
     */
    @Deprecated
    @JsonIgnore
    public void setFinished_at(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * @return the committed at Date
     * @deprecated Replaced by {@link #getCommittedAt()}
     */
    @Deprecated
    @JsonIgnore
    public Date getCommitted_at() {
        return committedAt;
    }

    /**
     * @param committedAt new committed at value
     * @deprecated Replaced by {@link #setCommittedAt(Date)}
     */
    @Deprecated
    @JsonIgnore
    public void setCommitted_at(Date committedAt) {
        this.committedAt = committedAt;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public DetailedStatus getDetailedStatus() {
        return detailedStatus;
    }

    public void setDetailedStatus(DetailedStatus detailedStatus) {
        this.detailedStatus = detailedStatus;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
