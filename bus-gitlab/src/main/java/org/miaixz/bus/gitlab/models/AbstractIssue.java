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

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import org.miaixz.bus.gitlab.models.Constants.IssueState;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The abstract issue class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public abstract class AbstractIssue implements Serializable {

    /**
     * Constructs a new AbstractIssue instance.
     */
    public AbstractIssue() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852235017309L;

    private References references;

    private Assignee assignee;
    private List<Assignee> assignees;
    private Author author;
    private Boolean confidential;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
    private User closedBy;
    private String description;
    private Date dueDate;

    @JsonProperty("id")
    private ValueNode actualId;

    @JsonIgnore
    private String externalId;

    @JsonIgnore
    private Long id;

    private Long iid;
    private List<String> labels;
    private Milestone milestone;
    private Long projectId;
    private IssueState state;
    private String title;
    private Integer userNotesCount;
    private String webUrl;
    private String severity;
    private Integer weight;
    private Boolean discussionLocked;
    private TimeStats timeStats;
    private String issueType;
    private IssueEpic epic;
    private Boolean imported;

    private Integer upvotes;
    private Integer downvotes;
    private Integer mergeRequestsCount;
    private Boolean hasTasks;
    private String taskStatus;
    private String importedFrom;
    private String healthStatus;
    private Iteration iteration;
    private TaskCompletionStatus taskCompletionStatus;

    /**
     * Returns the raw issue ID node.
     *
     * @return the raw issue ID node
     */
    public ValueNode getActualId() {
        return actualId;
    }

    /**
     * Sets the raw issue ID node and updates the numeric or external ID mirror.
     *
     * @param id the raw issue ID node
     */
    public void setActualId(ValueNode id) {
        actualId = id;
        if (actualId instanceof TextNode) {
            externalId = actualId.asText();
        } else if (actualId instanceof IntNode || actualId instanceof LongNode) {
            this.id = actualId.asLong();
        }
    }

    /**
     * Returns the numeric issue ID.
     *
     * @return the numeric issue ID
     */
    public Long getId() {
        return (id);
    }

    /**
     * Sets the numeric issue ID and updates the raw issue ID node.
     *
     * @param id the numeric issue ID
     */
    public void setId(Long id) {
        this.id = id;
        if (id != null) {
            actualId = new LongNode(id);
            externalId = null;
        }
    }

    /**
     * Returns the external issue ID.
     *
     * @return the external issue ID
     */
    public String getExternalId() {
        return (externalId);
    }

    /**
     * Sets the external issue ID and updates the raw issue ID node.
     *
     * @param externalId the external issue ID
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
        if (externalId != null) {
            actualId = new TextNode(externalId);
            id = null;
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

    /**
     * The task completion status class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class TaskCompletionStatus implements Serializable {

        /**
         * Constructs a new TaskCompletionStatus instance.
         */
        public TaskCompletionStatus() {
            // No initialization required.
        }

        @Serial
        private static final long serialVersionUID = 2852235115381L;

        private Integer count;
        private Integer completedCount;

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

}
