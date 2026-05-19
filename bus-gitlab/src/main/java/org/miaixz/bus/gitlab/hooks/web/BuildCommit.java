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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The build commit class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BuildCommit {

    private Long id;
    private String name;
    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private String authorUrl;
    private String status;
    private Float duration;
    private Date startedAt;
    private Date finishedAt;

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
     * Returns the author url.
     *
     * @return the result
     */

    public String getAuthorUrl() {
        return authorUrl;
    }

    /**
     * Sets the author url.
     *
     * @param authorUrl the author url value
     */

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
