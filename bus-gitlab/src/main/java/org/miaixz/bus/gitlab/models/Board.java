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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The board class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Board implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238266102L;

    private Long id;
    private String name;
    private Boolean hideBacklogList;
    private Boolean hideClosedList;
    private Project project;
    private List<BoardList> lists;
    private Group group;
    private Milestone milestone;
    private Assignee assignee;
    private List<Label> labels;
    private Integer weight;

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
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the hide backlog list.
     *
     * @return the result
     */

    public Boolean getHideBacklogList() {
        return hideBacklogList;
    }

    /**
     * Sets the hide backlog list.
     *
     * @param hideBacklogList the hide backlog list value
     */

    public void setHideBacklogList(Boolean hideBacklogList) {
        this.hideBacklogList = hideBacklogList;
    }

    /**
     * Returns the hide closed list.
     *
     * @return the result
     */

    public Boolean getHideClosedList() {
        return hideClosedList;
    }

    /**
     * Sets the hide closed list.
     *
     * @param hideClosedList the hide closed list value
     */

    public void setHideClosedList(Boolean hideClosedList) {
        this.hideClosedList = hideClosedList;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public Project getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Returns the milestone.
     *
     * @return the result
     */

    public Milestone getMilestone() {
        return milestone;
    }

    /**
     * Sets the milestone.
     *
     * @param milestone the milestone value
     */

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    /**
     * Returns the lists.
     *
     * @return the result
     */

    public List<BoardList> getLists() {
        return lists;
    }

    /**
     * Sets the lists.
     *
     * @param lists the lists value
     */

    public void setLists(List<BoardList> lists) {
        this.lists = lists;
    }

    /**
     * Returns the group.
     *
     * @return the result
     */

    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group the group value
     */

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Returns the assignee.
     *
     * @return the result
     */

    public Assignee getAssignee() {
        return assignee;
    }

    /**
     * Sets the assignee.
     *
     * @param assignee the assignee value
     */

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(List<Label> labels) {
        this.labels = labels;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
