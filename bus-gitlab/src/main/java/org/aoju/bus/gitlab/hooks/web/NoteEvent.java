/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org Greg Messner and other contributors.         *
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
package org.aoju.bus.gitlab.hooks.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.gitlab.JacksonJson;
import org.aoju.bus.gitlab.JacksonJsonEnumHelper;
import org.aoju.bus.gitlab.models.Diff;
import org.aoju.bus.gitlab.models.User;

import java.util.Date;

/**
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public class NoteEvent extends AbstractEvent {

    public static final String X_GITLAB_EVENT = "Note Hook";
    public static final String OBJECT_KIND = "note";

    private User user;
    private Integer projectId;
    private EventProject project;
    private EventRepository repository;
    private ObjectAttributes objectAttributes;
    private EventCommit commit;
    private EventIssue issue;
    private EventMergeRequest mergeRequest;
    private EventSnippet snippet;

    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public EventProject getProject() {
        return project;
    }

    public void setProject(EventProject project) {
        this.project = project;
    }

    public EventRepository getRepository() {
        return repository;
    }

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    public ObjectAttributes getObjectAttributes() {
        return this.objectAttributes;
    }

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    public EventCommit getCommit() {
        return commit;
    }

    public void setCommit(EventCommit commit) {
        this.commit = commit;
    }

    public EventIssue getIssue() {
        return issue;
    }

    public void setIssue(EventIssue issue) {
        this.issue = issue;
    }

    public EventMergeRequest getMergeRequest() {
        return mergeRequest;
    }

    public void setMergeRequest(EventMergeRequest mergeRequest) {
        this.mergeRequest = mergeRequest;
    }

    public EventSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(EventSnippet snippet) {
        this.snippet = snippet;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public static enum NoteableType {

        ISSUE, MERGE_REQUEST, SNIPPET, COMMIT;
        private static JacksonJsonEnumHelper<NoteableType> enumHelper = new JacksonJsonEnumHelper<>(NoteableType.class, true, true);

        @JsonCreator
        public static NoteableType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    public static class ObjectAttributes {

        private Integer id;
        private String note;
        private NoteableType noteableType;
        private Integer authorId;
        private Date createdAt;
        private Date updatedAt;
        private Integer projectId;
        private String attachment;
        private String lineCode;
        private String commitId;
        private Integer noteableId;
        private Boolean system;
        private Diff stDiff;
        private String url;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public NoteableType getNoteableType() {
            return noteableType;
        }

        public void NoteableType(NoteableType notableType) {
            this.noteableType = notableType;
        }

        public Integer getAuthorId() {
            return this.authorId;
        }

        public void setAuthorId(Integer authorId) {
            this.authorId = authorId;
        }

        public Date getCreatedAt() {
            return this.createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getUpdatedAt() {
            return this.updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Integer getProjectId() {
            return this.projectId;
        }

        public void setProjectId(Integer projectId) {
            this.projectId = projectId;
        }

        public String getAttachment() {
            return attachment;
        }

        public void setAttachment(String attachment) {
            this.attachment = attachment;
        }

        public String getLineCode() {
            return lineCode;
        }

        public void setLineCode(String lineCode) {
            this.lineCode = lineCode;
        }

        public String getCommitId() {
            return commitId;
        }

        public void setCommitId(String commitId) {
            this.commitId = commitId;
        }

        public Integer getNoteableId() {
            return noteableId;
        }

        public void setNoteableId(Integer noteableId) {
            this.noteableId = noteableId;
        }

        public Boolean getSystem() {
            return system;
        }

        public void setSystem(Boolean system) {
            this.system = system;
        }

        public Diff getStDiff() {
            return stDiff;
        }

        public void setStDiff(Diff stDiff) {
            this.stDiff = stDiff;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
