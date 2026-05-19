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
 * The snippet class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Snippet implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281375272L;

    private Author author;
    private Date createdAt;
    private Date expiresAt;
    private String fileName;
    private Long id;
    private String title;
    private String updatedAt;
    private String webUrl;
    private String content;
    private String rawUrl;
    private Visibility visibility;
    private String description;

    /**
     * Constructs a new {@code Snippet} instance.
     */

    public Snippet() {
        // No initialization required.
    }

    /**
     * Constructs a new {@code Snippet} instance.
     *
     * @param title       the title value
     * @param fileName    the file name value
     * @param content     the content value
     * @param visibility  the visibility value
     * @param description the description value
     */

    public Snippet(String title, String fileName, String content, Visibility visibility, String description) {
        this(title, fileName, content);
        this.visibility = visibility;
        this.description = description;
    }

    /**
     * Constructs a new {@code Snippet} instance.
     *
     * @param title    the title value
     * @param fileName the file name value
     * @param content  the content value
     */

    public Snippet(String title, String fileName, String content) {
        this.title = title;
        this.fileName = fileName;
        this.content = content;
    }

    /**
     * Returns the author.
     *
     * @return the result
     */

    public Author getAuthor() {
        return this.author;
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
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return this.expiresAt;
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
     * Returns the file name.
     *
     * @return the result
     */

    public String getFileName() {
        return this.fileName;
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
     * Returns the updated at.
     *
     * @return the result
     */

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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
     * Returns the content.
     *
     * @return the result
     */

    public String getContent() {
        return content;
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
     * Returns the raw url.
     *
     * @return the result
     */

    public String getRawUrl() {
        return rawUrl;
    }

    /**
     * Sets the raw url.
     *
     * @param rawUrl the raw url value
     */

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    /**
     * Returns the visibility.
     *
     * @return the result
     */

    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility.
     *
     * @param visibility the visibility value
     */

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
