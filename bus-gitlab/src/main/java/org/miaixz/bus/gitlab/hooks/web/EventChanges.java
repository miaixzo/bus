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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import org.miaixz.bus.gitlab.models.Assignee;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.logger.Logger;

/**
 * The event changes class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class EventChanges {

    private ChangeContainer<Long> authorId;
    private ChangeContainer<Date> createdAt;
    private ChangeContainer<Date> updatedAt;
    private ChangeContainer<Long> updatedById;
    private ChangeContainer<String> title;
    private ChangeContainer<String> description;
    private ChangeContainer<String> state;
    private ChangeContainer<Long> milestoneId;
    private ChangeContainer<List<EventLabel>> labels;
    private ChangeContainer<List<Assignee>> assignees;
    private ChangeContainer<Integer> totalTimeSpent;
    private Map<String, ChangeContainer<Object>> otherProperties = new LinkedHashMap<>();

    /**
     * Returns the author id.
     *
     * @return the result
     */

    public ChangeContainer<Long> getAuthorId() {
        return authorId;
    }

    /**
     * Sets the author id.
     *
     * @param authorId the author id value
     */

    public void setAuthorId(ChangeContainer<Long> authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public ChangeContainer<Date> getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(ChangeContainer<Date> createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public ChangeContainer<Date> getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(ChangeContainer<Date> updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the updated by id.
     *
     * @return the result
     */

    public ChangeContainer<Long> getUpdatedById() {
        return updatedById;
    }

    /**
     * Sets the updated by id.
     *
     * @param updatedById the updated by id value
     */

    public void setUpdatedById(ChangeContainer<Long> updatedById) {
        this.updatedById = updatedById;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public ChangeContainer<String> getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(ChangeContainer<String> title) {
        this.title = title;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public ChangeContainer<String> getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(ChangeContainer<String> description) {
        this.description = description;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public ChangeContainer<String> getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(ChangeContainer<String> state) {
        this.state = state;
    }

    /**
     * Returns the milestone id.
     *
     * @return the result
     */

    public ChangeContainer<Long> getMilestoneId() {
        return milestoneId;
    }

    /**
     * Sets the milestone id.
     *
     * @param milestoneId the milestone id value
     */

    public void setMilestoneId(ChangeContainer<Long> milestoneId) {
        this.milestoneId = milestoneId;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public ChangeContainer<List<EventLabel>> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(ChangeContainer<List<EventLabel>> labels) {
        this.labels = labels;
    }

    /**
     * Returns the assignees.
     *
     * @return the result
     */

    public ChangeContainer<List<Assignee>> getAssignees() {
        return assignees;
    }

    /**
     * Sets the assignees.
     *
     * @param assignees the assignees value
     */

    public void setAssignees(ChangeContainer<List<Assignee>> assignees) {
        this.assignees = assignees;
    }

    /**
     * Returns the total time spent.
     *
     * @return the result
     */

    public ChangeContainer<Integer> getTotalTimeSpent() {
        return totalTimeSpent;
    }

    /**
     * Sets the total time spent.
     *
     * @param totalTimeSpent the total time spent value
     */

    public void setTotalTimeSpent(ChangeContainer<Integer> totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    /**
     * Returns the value.
     *
     * @param property the property value
     * @return the result
     */

    public <T> ChangeContainer<T> get(String property) {

        if (otherProperties.containsKey(property)) {
            try {
                final ChangeContainer<Object> container = otherProperties.get(property);
                // noinspection unchecked : It's duty from caller to be sure to do that
                return container != null ? (ChangeContainer<T>) container : null;
            } catch (ClassCastException e) {
                Logger.debug(
                        false,
                        "GitLab",
                        "GitLab webhook change container cast failed: property={}, containerPresent={}, exception={}",
                        property,
                        otherProperties.get(property) != null,
                        e.getClass().getSimpleName());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Executes the any operation.
     *
     * @return the result
     */

    @JsonAnyGetter
    public Map<String, ChangeContainer<Object>> any() {
        return this.otherProperties;
    }

    /**
     * Sets the value.
     *
     * @param name  the name value
     * @param value the value value
     */

    @JsonAnySetter
    public void set(String name, ChangeContainer<Object> value) {
        otherProperties.put(name, value);
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
