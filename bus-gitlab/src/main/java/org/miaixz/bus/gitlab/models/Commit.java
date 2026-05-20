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
 * The commit class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Commit implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852239586962L;

    private Author author;
    private Date authoredDate;
    private String authorEmail;
    private String authorName;
    private Date committedDate;
    private String committerEmail;
    private String committerName;
    private Date createdAt;
    private String id;
    private String message;
    private List<String> parentIds;
    private String shortId;
    private CommitStats stats;
    private String status;
    private Date timestamp;
    private String title;
    private String url;
    private String webUrl;
    private Long projectId;
    private Pipeline lastPipeline;

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
     * Returns the authored date.
     *
     * @return the result
     */

    public Date getAuthoredDate() {
        return authoredDate;
    }

    /**
     * Sets the authored date.
     *
     * @param authoredDate the authored date value
     */

    public void setAuthoredDate(Date authoredDate) {
        this.authoredDate = authoredDate;
    }

    /**
     * Returns the author email.
     *
     * @return the result
     */

    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * Sets the author email.
     *
     * @param authorEmail the author email value
     */

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    /**
     * Returns the author name.
     *
     * @return the result
     */

    public String getAuthorName() {
        return authorName;
    }

    /**
     * Sets the author name.
     *
     * @param authorName the author name value
     */

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Returns the committed date.
     *
     * @return the result
     */

    public Date getCommittedDate() {
        return committedDate;
    }

    /**
     * Sets the committed date.
     *
     * @param committedDate the committed date value
     */

    public void setCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
    }

    /**
     * Returns the committer email.
     *
     * @return the result
     */

    public String getCommitterEmail() {
        return committerEmail;
    }

    /**
     * Sets the committer email.
     *
     * @param committerEmail the committer email value
     */

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    /**
     * Returns the committer name.
     *
     * @return the result
     */

    public String getCommitterName() {
        return committerName;
    }

    /**
     * Sets the committer name.
     *
     * @param committerName the committer name value
     */

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
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
     * Returns the id.
     *
     * @return the result
     */

    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the message.
     *
     * @return the result
     */

    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message value
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the parent ids.
     *
     * @return the result
     */

    public List<String> getParentIds() {
        return parentIds;
    }

    /**
     * Sets the parent ids.
     *
     * @param parentIds the parent ids value
     */

    public void setParentIds(List<String> parentIds) {
        this.parentIds = parentIds;
    }

    /**
     * Returns the short id.
     *
     * @return the result
     */

    public String getShortId() {
        return shortId;
    }

    /**
     * Sets the short id.
     *
     * @param shortId the short id value
     */

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    /**
     * Returns the stats.
     *
     * @return the result
     */

    public CommitStats getStats() {
        return stats;
    }

    /**
     * Sets the stats.
     *
     * @param stats the stats value
     */

    public void setStats(CommitStats stats) {
        this.stats = stats;
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
     * Returns the timestamp.
     *
     * @return the result
     */

    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp value
     */

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(String title) {
        this.title = title;
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
     * Returns the last pipeline.
     *
     * @return the result
     */

    public Pipeline getLastPipeline() {
        return lastPipeline;
    }

    /**
     * Sets the last pipeline.
     *
     * @param lastPipeline the last pipeline value
     */

    public void setLastPipeline(Pipeline lastPipeline) {
        this.lastPipeline = lastPipeline;
    }

    /**
     * Sets the author and returns this instance.
     *
     * @param author the author value
     * @return the result
     */

    public Commit withAuthor(Author author) {
        this.author = author;
        return this;
    }

    /**
     * Sets the authored date and returns this instance.
     *
     * @param authoredDate the authored date value
     * @return the result
     */

    public Commit withAuthoredDate(Date authoredDate) {
        this.authoredDate = authoredDate;
        return this;
    }

    /**
     * Sets the author email and returns this instance.
     *
     * @param authorEmail the author email value
     * @return the result
     */

    public Commit withAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return this;
    }

    /**
     * Sets the author name and returns this instance.
     *
     * @param authorName the author name value
     * @return the result
     */

    public Commit withAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    /**
     * Sets the committed date and returns this instance.
     *
     * @param committedDate the committed date value
     * @return the result
     */

    public Commit withCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
        return this;
    }

    /**
     * Sets the committer email and returns this instance.
     *
     * @param committerEmail the committer email value
     * @return the result
     */

    public Commit withCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
        return this;
    }

    /**
     * Sets the committer name and returns this instance.
     *
     * @param committerName the committer name value
     * @return the result
     */

    public Commit withCommitterName(String committerName) {
        this.committerName = committerName;
        return this;
    }

    /**
     * Sets the created at and returns this instance.
     *
     * @param createdAt the created at value
     * @return the result
     */

    public Commit withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public Commit withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the message and returns this instance.
     *
     * @param message the message value
     * @return the result
     */

    public Commit withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the parent ids and returns this instance.
     *
     * @param parentIds the parent ids value
     * @return the result
     */

    public Commit withParentIds(List<String> parentIds) {
        this.parentIds = parentIds;
        return this;
    }

    /**
     * Sets the short id and returns this instance.
     *
     * @param shortId the short id value
     * @return the result
     */

    public Commit withShortId(String shortId) {
        this.shortId = shortId;
        return this;
    }

    /**
     * Sets the stats and returns this instance.
     *
     * @param stats the stats value
     * @return the result
     */

    public Commit withStats(CommitStats stats) {
        this.stats = stats;
        return this;
    }

    /**
     * Sets the status and returns this instance.
     *
     * @param status the status value
     * @return the result
     */

    public Commit withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the timestamp and returns this instance.
     *
     * @param timestamp the timestamp value
     * @return the result
     */

    public Commit withTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets the title and returns this instance.
     *
     * @param title the title value
     * @return the result
     */

    public Commit withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the url and returns this instance.
     *
     * @param url the url value
     * @return the result
     */

    public Commit withUrl(String url) {
        this.url = url;
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
