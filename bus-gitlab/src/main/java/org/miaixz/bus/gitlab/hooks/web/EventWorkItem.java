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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event work item class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventWorkItem {

    private Long authorId;
    private Date closedAt;
    private Boolean confidential;
    private Date createdAt;
    private String description;
    private Date dueDate;
    private Long id;
    private Long iid;
    private Date lastEditedAt;
    private Long lastEditedById;
    private Long milestoneId;
    private Long projectId;
    private Long relativePosition;
    private Long stateId;
    private Integer timeEstimate;
    private String title;
    private Date updatedAt;
    private Long updatedById;
    private Integer weight;
    private String healthStatus;

    /**
     * The type field.
     */
    private String type;
    private String url;
    private Integer totalTimeSpent;
    private Integer timeChange;
    private List<Long> assigneeIds;
    private Long assigneeId;
    private List<EventLabel> labels;
    private String state;
    private String severity;
    private String action;

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
     * Returns the closed at.
     *
     * @return the result
     */

    public Date getClosedAt() {
        return closedAt;
    }

    /**
     * Sets the closed at.
     *
     * @param closedAt the closed at value
     */

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * Returns the confidential.
     *
     * @return the result
     */

    public Boolean getConfidential() {
        return confidential;
    }

    /**
     * Sets the confidential.
     *
     * @param confidential the confidential value
     */

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
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
     * Returns the due date.
     *
     * @return the result
     */

    public Date getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date.
     *
     * @param dueDate the due date value
     */

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
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
     * Returns the iid.
     *
     * @return the result
     */

    public Long getIid() {
        return iid;
    }

    /**
     * Sets the iid.
     *
     * @param iid the iid value
     */

    public void setIid(Long iid) {
        this.iid = iid;
    }

    /**
     * Returns the last edited at.
     *
     * @return the result
     */

    public Date getLastEditedAt() {
        return lastEditedAt;
    }

    /**
     * Sets the last edited at.
     *
     * @param lastEditedAt the last edited at value
     */

    public void setLastEditedAt(Date lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    /**
     * Returns the last edited by id.
     *
     * @return the result
     */

    public Long getLastEditedById() {
        return lastEditedById;
    }

    /**
     * Sets the last edited by id.
     *
     * @param lastEditedById the last edited by id value
     */

    public void setLastEditedById(Long lastEditedById) {
        this.lastEditedById = lastEditedById;
    }

    /**
     * Returns the milestone id.
     *
     * @return the result
     */

    public Long getMilestoneId() {
        return milestoneId;
    }

    /**
     * Sets the milestone id.
     *
     * @param milestoneId the milestone id value
     */

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
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
     * Returns the relative position.
     *
     * @return the result
     */

    public Long getRelativePosition() {
        return relativePosition;
    }

    /**
     * Sets the relative position.
     *
     * @param relativePosition the relative position value
     */

    public void setRelativePosition(Long relativePosition) {
        this.relativePosition = relativePosition;
    }

    /**
     * Returns the state id.
     *
     * @return the result
     */

    public Long getStateId() {
        return stateId;
    }

    /**
     * Sets the state id.
     *
     * @param stateId the state id value
     */

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    /**
     * Returns the time estimate.
     *
     * @return the result
     */

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    /**
     * Sets the time estimate.
     *
     * @param timeEstimate the time estimate value
     */

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
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
     * Returns the updated by id.
     *
     * @return the result
     */

    public Long getUpdatedById() {
        return updatedById;
    }

    /**
     * Sets the updated by id.
     *
     * @param updatedById the updated by id value
     */

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    /**
     * Returns the weight.
     *
     * @return the result
     */

    public Integer getWeight() {
        return weight;
    }

    /**
     * Sets the weight.
     *
     * @param weight the weight value
     */

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    /**
     * Returns the health status.
     *
     * @return the result
     */

    public String getHealthStatus() {
        return healthStatus;
    }

    /**
     * Sets the health status.
     *
     * @param healthStatus the health status value
     */

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
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
     * Returns the total time spent.
     *
     * @return the result
     */

    public Integer getTotalTimeSpent() {
        return totalTimeSpent;
    }

    /**
     * Sets the total time spent.
     *
     * @param totalTimeSpent the total time spent value
     */

    public void setTotalTimeSpent(Integer totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    /**
     * Returns the time change.
     *
     * @return the result
     */

    public Integer getTimeChange() {
        return timeChange;
    }

    /**
     * Sets the time change.
     *
     * @param timeChange the time change value
     */

    public void setTimeChange(Integer timeChange) {
        this.timeChange = timeChange;
    }

    /**
     * Returns the assignee ids.
     *
     * @return the result
     */

    public List<Long> getAssigneeIds() {
        return assigneeIds;
    }

    /**
     * Sets the assignee ids.
     *
     * @param assigneeIds the assignee ids value
     */

    public void setAssigneeIds(List<Long> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

    /**
     * Returns the assignee id.
     *
     * @return the result
     */

    public Long getAssigneeId() {
        return assigneeId;
    }

    /**
     * Sets the assignee id.
     *
     * @param assigneeId the assignee id value
     */

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public List<EventLabel> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(List<EventLabel> labels) {
        this.labels = labels;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the severity.
     *
     * @return the result
     */

    public String getSeverity() {
        return severity;
    }

    /**
     * Sets the severity.
     *
     * @param severity the severity value
     */

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * Returns the action.
     *
     * @return the result
     */

    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action the action value
     */

    public void setAction(String action) {
        this.action = action;
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
