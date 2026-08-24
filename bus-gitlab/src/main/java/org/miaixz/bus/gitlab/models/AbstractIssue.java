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

import org.miaixz.bus.gitlab.models.Constants.IssueState;
import org.miaixz.bus.gitlab.support.JacksonJson;

import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.StringNode;
import tools.jackson.databind.node.ValueNode;

/**
 * The abstract issue class.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
public abstract class AbstractIssue implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852235017309L;

    /**
     * The references value.
     */
    private References references;

    /**
     * The assignee value.
     */
    private Assignee assignee;
    /**
     * The assignees value.
     */
    private List<Assignee> assignees;
    /**
     * The author value.
     */
    private Author author;
    /**
     * The confidential value.
     */
    private Boolean confidential;
    /**
     * The created at value.
     */
    private Date createdAt;
    /**
     * The updated at value.
     */
    private Date updatedAt;
    /**
     * The closed at value.
     */
    private Date closedAt;
    /**
     * The closed by value.
     */
    private User closedBy;
    /**
     * The description value.
     */
    private String description;
    /**
     * The due date value.
     */
    private Date dueDate;

    /**
     * The actual id value.
     */
    @JsonProperty("id")
    private ValueNode actualId;

    /**
     * The external id value.
     */
    @JsonIgnore
    private String externalId;

    /**
     * The id value.
     */
    @JsonIgnore
    private Long id;

    /**
     * The iid value.
     */
    private Long iid;
    /**
     * The labels value.
     */
    private List<String> labels;
    /**
     * The milestone value.
     */
    private Milestone milestone;
    /**
     * The project id value.
     */
    private Long projectId;
    /**
     * The state value.
     */
    private IssueState state;
    /**
     * The title value.
     */
    private String title;
    /**
     * The user notes count value.
     */
    private Integer userNotesCount;
    /**
     * The web url value.
     */
    private String webUrl;
    /**
     * The severity value.
     */
    private String severity;
    /**
     * The weight value.
     */
    private Integer weight;
    /**
     * The discussion locked value.
     */
    private Boolean discussionLocked;
    /**
     * The time stats value.
     */
    private TimeStats timeStats;
    /**
     * The issue type value.
     */
    private String issueType;
    /**
     * The epic value.
     */
    private IssueEpic epic;
    /**
     * The imported value.
     */
    private Boolean imported;

    /**
     * The upvotes value.
     */
    private Integer upvotes;
    /**
     * The downvotes value.
     */
    private Integer downvotes;
    /**
     * The merge requests count value.
     */
    private Integer mergeRequestsCount;
    /**
     * The has tasks value.
     */
    private Boolean hasTasks;
    /**
     * The task status value.
     */
    private String taskStatus;
    /**
     * The imported from value.
     */
    private String importedFrom;
    /**
     * The health status value.
     */
    private String healthStatus;
    /**
     * The iteration value.
     */
    private Iteration iteration;
    /**
     * The task completion status value.
     */
    private TaskCompletionStatus taskCompletionStatus;

    /**
     * Constructs a new AbstractIssue instance.
     */
    public AbstractIssue() {
        // No initialization required.
    }

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
        if (actualId instanceof StringNode) {
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
            actualId = new StringNode(externalId);
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
     */
    @Getter
    @Setter
    public static class TaskCompletionStatus implements Serializable {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852235115381L;

        /**
         * The count value.
         */
        private Integer count;
        /**
         * The completed count value.
         */
        private Integer completedCount;

        /**
         * Constructs a new TaskCompletionStatus instance.
         */
        public TaskCompletionStatus() {
            // No initialization required.
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

}
