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

import java.io.Serial;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.models.Diff;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The note event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NoteEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852232895012L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Note Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "note";

    private EventUser user;
    private Long projectId;
    private EventProject project;
    private EventRepository repository;
    private ObjectAttributes objectAttributes;
    private EventCommit commit;
    private EventIssue issue;
    private EventMergeRequest mergeRequest;
    private EventSnippet snippet;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    /**
     * Returns the user.
     *
     * @return the result
     */

    public EventUser getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(EventUser user) {
        this.user = user;
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
     * Returns the project.
     *
     * @return the result
     */

    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the repository.
     *
     * @return the result
     */

    public EventRepository getRepository() {
        return repository;
    }

    /**
     * Sets the repository.
     *
     * @param repository the repository value
     */

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the object attributes.
     *
     * @return the result
     */

    public ObjectAttributes getObjectAttributes() {
        return this.objectAttributes;
    }

    /**
     * Sets the object attributes.
     *
     * @param objectAttributes the object attributes value
     */

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public EventCommit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(EventCommit commit) {
        this.commit = commit;
    }

    /**
     * Returns the issue.
     *
     * @return the result
     */

    public EventIssue getIssue() {
        return issue;
    }

    /**
     * Sets the issue.
     *
     * @param issue the issue value
     */

    public void setIssue(EventIssue issue) {
        this.issue = issue;
    }

    /**
     * Returns the merge request.
     *
     * @return the result
     */

    public EventMergeRequest getMergeRequest() {
        return mergeRequest;
    }

    /**
     * Sets the merge request.
     *
     * @param mergeRequest the merge request value
     */

    public void setMergeRequest(EventMergeRequest mergeRequest) {
        this.mergeRequest = mergeRequest;
    }

    /**
     * Returns the snippet.
     *
     * @return the result
     */

    public EventSnippet getSnippet() {
        return snippet;
    }

    /**
     * Sets the snippet.
     *
     * @param snippet the snippet value
     */

    public void setSnippet(EventSnippet snippet) {
        this.snippet = snippet;
    }

    /**
     * The noteable type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum NoteableType {

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
        SNIPPET,
        /**
         * The commit noteable type.
         */
        COMMIT;

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
     * The object attributes class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ObjectAttributes {

        private Long id;
        private String note;
        private String discussionId;

        /**
         * The type field.
         */
        private String type;
        private NoteableType noteableType;
        private Long authorId;
        private Date createdAt;
        private Date updatedAt;
        private Long projectId;
        private String attachment;
        private String lineCode;
        private String commitId;
        private Long noteableId;
        private Boolean system;
        private Diff stDiff;
        private String url;

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
         * Returns the note.
         *
         * @return the result
         */

        public String getNote() {
            return note;
        }

        /**
         * Sets the note.
         *
         * @param note the note value
         */

        public void setNote(String note) {
            this.note = note;
        }

        /**
         * Returns the discussion id.
         *
         * @return the result
         */

        public String getDiscussionId() {
            return discussionId;
        }

        /**
         * Sets the discussion id.
         *
         * @param discussionId the discussion id value
         */

        public void setDiscussionId(String discussionId) {
            this.discussionId = discussionId;
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
         * Returns the noteable type.
         *
         * @return the result
         */

        public NoteableType getNoteableType() {
            return noteableType;
        }

        /**
         * Executes the noteable type operation.
         *
         * @param notableType the notable type value
         */

        public void NoteableType(NoteableType notableType) {
            this.noteableType = notableType;
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
         * Returns the line code.
         *
         * @return the result
         */

        public String getLineCode() {
            return lineCode;
        }

        /**
         * Sets the line code.
         *
         * @param lineCode the line code value
         */

        public void setLineCode(String lineCode) {
            this.lineCode = lineCode;
        }

        /**
         * Returns the commit id.
         *
         * @return the result
         */

        public String getCommitId() {
            return commitId;
        }

        /**
         * Sets the commit id.
         *
         * @param commitId the commit id value
         */

        public void setCommitId(String commitId) {
            this.commitId = commitId;
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
         * Returns the st diff.
         *
         * @return the result
         */

        public Diff getStDiff() {
            return stDiff;
        }

        /**
         * Sets the st diff.
         *
         * @param stDiff the st diff value
         */

        public void setStDiff(Diff stDiff) {
            this.stDiff = stDiff;
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
