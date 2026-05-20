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
import java.util.List;

import org.miaixz.bus.gitlab.models.Author;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event commit class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventCommit {

    private String id;
    private String message;
    private String title;
    private Date timestamp;
    private String url;
    private Author author;
    private List<String> added;
    private List<String> modified;
    private List<String> removed;

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
     * Returns the added.
     *
     * @return the result
     */

    public List<String> getAdded() {
        return added;
    }

    /**
     * Sets the added.
     *
     * @param added the added value
     */

    public void setAdded(List<String> added) {
        this.added = added;
    }

    /**
     * Returns the modified.
     *
     * @return the result
     */

    public List<String> getModified() {
        return modified;
    }

    /**
     * Sets the modified.
     *
     * @param modified the modified value
     */

    public void setModified(List<String> modified) {
        this.modified = modified;
    }

    /**
     * Returns the removed.
     *
     * @return the result
     */

    public List<String> getRemoved() {
        return removed;
    }

    /**
     * Sets the removed.
     *
     * @param removed the removed value
     */

    public void setRemoved(List<String> removed) {
        this.removed = removed;
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
