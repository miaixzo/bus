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

import org.miaixz.bus.gitlab.models.Constants.TargetType;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253230058L;

    private Long id;
    private String actionName;
    private Author author;
    private Long authorId;
    private String authorUsername;
    private EventData data;
    private Long projectId;
    private Long targetId;
    private Long targetIid;
    private String targetTitle;
    private TargetType targetType;
    private String title;
    private Date createdAt;

    private Note note;
    private PushData pushData;

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
     * Returns the action name.
     *
     * @return the result
     */

    public String getActionName() {
        return actionName;
    }

    /**
     * Sets the action name.
     *
     * @param actionName the action name value
     */

    public void setActionName(String actionName) {
        this.actionName = actionName;
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
     * Returns the author id.
     *
     * @return the result
     */

    public Long getAuthorId() {
        return authorId;
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
     * Returns the author username.
     *
     * @return the result
     */

    public String getAuthorUsername() {
        return authorUsername;
    }

    /**
     * Sets the author username.
     *
     * @param authorUsername the author username value
     */

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    /**
     * Returns the data.
     *
     * @return the result
     */

    public EventData getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the data value
     */

    public void setData(EventData data) {
        this.data = data;
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
     * Returns the target id.
     *
     * @return the result
     */

    public Long getTargetId() {
        return targetId;
    }

    /**
     * Sets the target id.
     *
     * @param targetId the target id value
     */

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    /**
     * Returns the target iid.
     *
     * @return the result
     */

    public Long getTargetIid() {
        return targetIid;
    }

    /**
     * Sets the target iid.
     *
     * @param targetIid the target iid value
     */

    public void setTargetIid(Long targetIid) {
        this.targetIid = targetIid;
    }

    /**
     * Returns the target title.
     *
     * @return the result
     */

    public String getTargetTitle() {
        return targetTitle;
    }

    /**
     * Sets the target title.
     *
     * @param targetTitle the target title value
     */

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }

    /**
     * Returns the target type.
     *
     * @return the result
     */

    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * Sets the target type.
     *
     * @param targetType the target type value
     */

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
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
     * Returns the note.
     *
     * @return the result
     */

    public Note getNote() {
        return note;
    }

    /**
     * Sets the note.
     *
     * @param note the note value
     */

    public void setNote(Note note) {
        this.note = note;
    }

    /**
     * Returns the push data.
     *
     * @return the result
     */

    public PushData getPushData() {
        return pushData;
    }

    /**
     * Sets the push data.
     *
     * @param pushData the push data value
     */

    public void setPushData(PushData pushData) {
        this.pushData = pushData;
    }

    /**
     * Sets the action name and returns this instance.
     *
     * @param actionName the action name value
     * @return the result
     */

    public Event withActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    /**
     * Sets the author and returns this instance.
     *
     * @param author the author value
     * @return the result
     */

    public Event withAuthor(Author author) {
        this.author = author;
        return this;
    }

    /**
     * Sets the author id and returns this instance.
     *
     * @param authorId the author id value
     * @return the result
     */

    public Event withAuthorId(Long authorId) {
        this.authorId = authorId;
        return this;
    }

    /**
     * Sets the author username and returns this instance.
     *
     * @param authorUsername the author username value
     * @return the result
     */

    public Event withAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
        return this;
    }

    /**
     * Sets the data and returns this instance.
     *
     * @param data the data value
     * @return the result
     */

    public Event withData(EventData data) {
        this.data = data;
        return this;
    }

    /**
     * Sets the project id and returns this instance.
     *
     * @param projectId the project id value
     * @return the result
     */

    public Event withProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * Sets the target id and returns this instance.
     *
     * @param targetId the target id value
     * @return the result
     */

    public Event withTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    /**
     * Sets the target iid and returns this instance.
     *
     * @param targetIid the target iid value
     * @return the result
     */

    public Event withTargetIid(Long targetIid) {
        this.targetIid = targetIid;
        return this;
    }

    /**
     * Sets the target title and returns this instance.
     *
     * @param targetTitle the target title value
     * @return the result
     */

    public Event withTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
        return this;
    }

    /**
     * Sets the target type and returns this instance.
     *
     * @param targetType the target type value
     * @return the result
     */

    public Event withTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    /**
     * Sets the title and returns this instance.
     *
     * @param title the title value
     * @return the result
     */

    public Event withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the created at and returns this instance.
     *
     * @param createdAt the created at value
     * @return the result
     */

    public Event withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
