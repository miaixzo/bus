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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The board list class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BoardList implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238305607L;

    private Long id;
    private Label label;
    private Integer position;
    private Assignee assignee;
    private Milestone milestone;
    private Iteration iteration;
    private Integer maxIssueCount;
    private Integer maxIssueWeight;
    private Integer limitMetric;
    private String listType;

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
     * Returns the label.
     *
     * @return the result
     */

    public Label getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the label value
     */

    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * Returns the position.
     *
     * @return the result
     */

    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the position value
     */

    public void setPosition(Integer position) {
        this.position = position;
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
     * Returns the iteration.
     *
     * @return the result
     */

    public Iteration getIteration() {
        return iteration;
    }

    /**
     * Sets the iteration.
     *
     * @param iteration the iteration value
     */

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
    }

    /**
     * Returns the max issue count.
     *
     * @return the result
     */

    public Integer getMaxIssueCount() {
        return maxIssueCount;
    }

    /**
     * Sets the max issue count.
     *
     * @param maxIssueCount the max issue count value
     */

    public void setMaxIssueCount(Integer maxIssueCount) {
        this.maxIssueCount = maxIssueCount;
    }

    /**
     * Returns the max issue weight.
     *
     * @return the result
     */

    public Integer getMaxIssueWeight() {
        return maxIssueWeight;
    }

    /**
     * Sets the max issue weight.
     *
     * @param maxIssueWeight the max issue weight value
     */

    public void setMaxIssueWeight(Integer maxIssueWeight) {
        this.maxIssueWeight = maxIssueWeight;
    }

    /**
     * Returns the limit metric.
     *
     * @return the result
     */

    public Integer getLimitMetric() {
        return limitMetric;
    }

    /**
     * Sets the limit metric.
     *
     * @param limitMetric the limit metric value
     */

    public void setLimitMetric(Integer limitMetric) {
        this.limitMetric = limitMetric;
    }

    /**
     * Returns the list type.
     *
     * @return the result
     */

    public String getListType() {
        return listType;
    }

    /**
     * Sets the list type.
     *
     * @param listType the list type value
     */

    public void setListType(String listType) {
        this.listType = listType;
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
