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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The label class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Label implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260153906L;

    private Long id;
    private String name;
    private String color;
    private String description;
    private String descriptionHtml;
    private String textColor;
    private Integer openIssuesCount;
    private Integer closedIssuesCount;
    private Integer openMergeRequestsCount;
    private Boolean subscribed;
    private Integer priority;
    private Boolean isProjectLabel;

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
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Label withName(String name) {
        this.name = name;
        return (this);
    }

    /**
     * Returns the color.
     *
     * @return the result
     */

    public String getColor() {
        return color;
    }

    /**
     * Sets the color.
     *
     * @param color the color value
     */

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Sets the color and returns this instance.
     *
     * @param color the color value
     * @return the result
     */

    public Label withColor(String color) {
        this.color = color;
        return (this);
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
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public Label withDescription(String description) {
        this.description = description;
        return (this);
    }

    /**
     * Returns the description html.
     *
     * @return the result
     */

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    /**
     * Sets the description html.
     *
     * @param descriptionHtml the description html value
     */

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    /**
     * Returns the text color.
     *
     * @return the result
     */

    public String getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color.
     *
     * @param textColor the text color value
     */

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    /**
     * Returns the open issues count.
     *
     * @return the result
     */

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    /**
     * Sets the open issues count.
     *
     * @param openIssuesCount the open issues count value
     */

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    /**
     * Returns the closed issues count.
     *
     * @return the result
     */

    public Integer getClosedIssuesCount() {
        return closedIssuesCount;
    }

    /**
     * Sets the closed issues count.
     *
     * @param closedIssuesCount the closed issues count value
     */

    public void setClosedIssuesCount(Integer closedIssuesCount) {
        this.closedIssuesCount = closedIssuesCount;
    }

    /**
     * Returns the open merge requests count.
     *
     * @return the result
     */

    public Integer getOpenMergeRequestsCount() {
        return openMergeRequestsCount;
    }

    /**
     * Sets the open merge requests count.
     *
     * @param openMergeRequestsCount the open merge requests count value
     */

    public void setOpenMergeRequestsCount(Integer openMergeRequestsCount) {
        this.openMergeRequestsCount = openMergeRequestsCount;
    }

    /**
     * Returns whether the subscribed is enabled.
     *
     * @return the result
     */

    public Boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Sets the subscribed.
     *
     * @param subscribed the subscribed value
     */

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * Returns the priority.
     *
     * @return the result
     */

    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     *
     * @param priority the priority value
     */

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Sets the priority and returns this instance.
     *
     * @param priority the priority value
     * @return the result
     */

    public Label withPriority(Integer priority) {
        this.priority = priority;
        return (this);
    }

    /**
     * Returns the is project label.
     *
     * @return the result
     */

    public Boolean getIsProjectLabel() {
        return isProjectLabel;
    }

    /**
     * Sets the is project label.
     *
     * @param isProjectLabel the is project label value
     */

    public void setIsProjectLabel(Boolean isProjectLabel) {
        this.isProjectLabel = isProjectLabel;
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
     * Get the form params specified by this instance.
     *
     * @param isCreate set to true if the params are for a create label call, false for an update
     * @return a GitLabApiForm instance holding the form parameters for this LabelParams instance
     */
    @JsonIgnore
    public GitLabForm getForm(boolean isCreate) {
        GitLabForm form = new GitLabForm().withParam("description", description).withParam("color", color, isCreate)
                .withParam("priority", priority);

        if (isCreate) {
            form.withParam("name", name, true);
        } else {
            form.withParam("new_name", name);
        }

        return (form);
    }

}
