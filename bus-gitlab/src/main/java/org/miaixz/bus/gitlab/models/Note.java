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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The note class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Note implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265960807L;

    /**
     * Enum to use for ordering the results.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum OrderBy {

        /**
         * The created at order by.
         */
        CREATED_AT,
        /**
         * The updated at order by.
         */
        UPDATED_AT;

        private static JacksonJsonEnumHelper<OrderBy> enumHelper = new JacksonJsonEnumHelper<>(OrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static OrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    // This is not used because the GitLab example JSON is using a funny string for the MERGE_REQUEST notable_type
    // ("Merge request").
    // Once they fix the bug, the notableType field can be changed from String to NotableType.
    /**
     * The noteable type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum NoteableType {

        /**
         * The commit noteable type.
         */
        COMMIT,
        /**
         * The epic noteable type.
         */
        EPIC,
        /**
         * The issue noteable type.
         */
        ISSUE,
        /**
         * The merge request noteable type.
         */
        MERGE_REQUEST,
        /**
         * The snippet noteable type.
         */
        SNIPPET;

        private static JacksonJsonEnumHelper<NoteableType> enumHelper = new JacksonJsonEnumHelper<>(NoteableType.class,
                true, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static NoteableType forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * The type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum Type {

        /**
         * The discussion note type.
         */
        DISCUSSION_NOTE,
        /**
         * The diff note type.
         */
        DIFF_NOTE;

        private static JacksonJsonEnumHelper<Type> enumHelper = new JacksonJsonEnumHelper<>(Type.class, true, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Type forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    private String attachment;
    private Author author;
    private String body;
    private Date createdAt;
    private Boolean downvote;
    private Date expiresAt;
    private String fileName;
    private Long id;
    private Long noteableId;

    // Use String for noteableType until the constant is fixed in the GitLab API
    private String noteableType;

    private Long noteableIid;
    private Boolean system;
    private String title;
    private Date updatedAt;
    private Boolean upvote;
    private Boolean resolved;
    private Boolean resolvable;
    private Participant resolvedBy;
    private Date resolvedAt;
    private Boolean internal;

    /**
     * The type field.
     */
    private Type type;

    private Position position;

    /**
     * Returns the attachment.
     *
     * @return the result
     */

    public String getAttachment() {
        return attachment;
    }

    /**
     * Sets the attachment.
     *
     * @param attachment the attachment value
     */

    public void setAttachment(String attachment) {
        this.attachment = attachment;
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
     * Returns the body.
     *
     * @return the result
     */

    public String getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body the body value
     */

    public void setBody(String body) {
        this.body = body;
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
     * Returns the downvote.
     *
     * @return the result
     */

    public Boolean getDownvote() {
        return downvote;
    }

    /**
     * Sets the downvote.
     *
     * @param downvote the downvote value
     */

    public void setDownvote(Boolean downvote) {
        this.downvote = downvote;
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
     * Returns the noteable id.
     *
     * @return the result
     */

    public Long getNoteableId() {
        return noteableId;
    }

    /**
     * Sets the noteable id.
     *
     * @param noteableId the noteable id value
     */

    public void setNoteableId(Long noteableId) {
        this.noteableId = noteableId;
    }

    /**
     * Returns the noteable type.
     *
     * @return the result
     */

    public String getNoteableType() {
        return noteableType;
    }

    /**
     * Sets the noteable type.
     *
     * @param noteableType the noteable type value
     */

    public void setNoteableType(String noteableType) {
        this.noteableType = noteableType;
    }

    /**
     * Returns the noteable iid.
     *
     * @return the result
     */

    public Long getNoteableIid() {
        return noteableIid;
    }

    /**
     * Sets the noteable iid.
     *
     * @param noteableIid the noteable iid value
     */

    public void setNoteableIid(Long noteableIid) {
        this.noteableIid = noteableIid;
    }

    /**
     * Returns the system.
     *
     * @return the result
     */

    public Boolean getSystem() {
        return system;
    }

    /**
     * Sets the system.
     *
     * @param system the system value
     */

    public void setSystem(Boolean system) {
        this.system = system;
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
     * Returns the upvote.
     *
     * @return the result
     */

    public Boolean getUpvote() {
        return upvote;
    }

    /**
     * Sets the upvote.
     *
     * @param upvote the upvote value
     */

    public void setUpvote(Boolean upvote) {
        this.upvote = upvote;
    }

    /**
     * Returns the resolved.
     *
     * @return the result
     */

    public Boolean getResolved() {
        return resolved;
    }

    /**
     * Sets the resolved.
     *
     * @param resolved the resolved value
     */

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Returns the resolvable.
     *
     * @return the result
     */

    public Boolean getResolvable() {
        return resolvable;
    }

    /**
     * Sets the resolvable.
     *
     * @param resolvable the resolvable value
     */

    public void setResolvable(Boolean resolvable) {
        this.resolvable = resolvable;
    }

    /**
     * Returns the resolved by.
     *
     * @return the result
     */

    public Participant getResolvedBy() {
        return resolvedBy;
    }

    /**
     * Sets the resolved by.
     *
     * @param resolvedBy the resolved by value
     */

    public void setResolvedBy(Participant resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    /**
     * Returns the resolved at.
     *
     * @return the result
     */

    public Date getResolvedAt() {
        return resolvedAt;
    }

    /**
     * Sets the resolved at.
     *
     * @param resolvedAt the resolved at value
     */

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    /**
     * Returns the type.
     *
     * @return the result
     */

    public Type getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type value
     */

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the position.
     *
     * @return the result
     */

    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the position value
     */

    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the internal.
     *
     * @return the result
     */

    public Boolean getInternal() {
        return internal;
    }

    /**
     * Sets the internal.
     *
     * @param internal the internal value
     */

    public void setInternal(Boolean internal) {
        this.internal = internal;
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
