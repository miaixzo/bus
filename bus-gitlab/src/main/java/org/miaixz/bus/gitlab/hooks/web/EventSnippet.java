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

import org.miaixz.bus.gitlab.models.AccessLevel;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event snippet class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventSnippet {

    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private Long projectId;
    private Date createdAt;
    private Date updatedAt;
    private String fileName;
    private Date expiresAt;

    /**
     * The type field.
     */
    private String type;
    private AccessLevel visibilityLevel;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return this.id;
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
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return this.title;
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
     * Returns the content.
     *
     * @return the result
     */

    public String getContent() {
        return this.content;
    }

    /**
     * Sets the content.
     *
     * @param content the content value
     */

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the author id.
     *
     * @return the result
     */

    public Long getAuthorId() {
        return this.authorId;
    }

    /**
     * Sets the author id.
     *
     * @param authorId the author id value
     */

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return this.projectId;
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
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return this.createdAt;
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
        return this.updatedAt;
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
     * Returns the file name.
     *
     * @return the result
     */

    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name value
     */

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expires at.
     *
     * @param expiresAt the expires at value
     */

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the type.
     *
     * @return the result
     */

    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type value
     */

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the visibility level.
     *
     * @return the result
     */

    public AccessLevel getVisibilityLevel() {
        return visibilityLevel;
    }

    /**
     * Sets the visibility level.
     *
     * @param visibilityLevel the visibility level value
     */

    public void setVisibilityLevel(AccessLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
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
