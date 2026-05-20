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
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The event label class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventLabel implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852231062892L;

    /**
     * The label type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum LabelType {

        /**
         * The project label label type.
         */
        PROJECT_LABEL,
        /**
         * The group label label type.
         */
        GROUP_LABEL;

        private static JacksonJsonEnumHelper<LabelType> enumHelper = new JacksonJsonEnumHelper<>(LabelType.class, true,
                true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static LabelType forValue(String value) {
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

    private Long id;
    private String title;
    private String color;
    private Long projectId;
    private Date createdAt;
    private Date updatedAt;
    private Boolean template;
    private String description;

    /**
     * The type field.
     */
    private LabelType type;
    private Long groupId;

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
     * Returns the template.
     *
     * @return the result
     */

    public Boolean getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     *
     * @param template the template value
     */

    public void setTemplate(Boolean template) {
        this.template = template;
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
     * Returns the type.
     *
     * @return the result
     */

    public LabelType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type value
     */

    public void setType(LabelType type) {
        this.type = type;
    }

    /**
     * Returns the group id.
     *
     * @return the result
     */

    public Long getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the group id value
     */

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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
