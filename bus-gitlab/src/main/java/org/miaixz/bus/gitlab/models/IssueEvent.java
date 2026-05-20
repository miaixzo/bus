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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The issue event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssueEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852257573191L;

    /**
     * Enum to use for specifying the state events resource type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ResourceType {

        ISSUE;

        private static JacksonJsonEnumHelper<ResourceType> enumHelper = new JacksonJsonEnumHelper<>(ResourceType.class,
                true, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ResourceType forValue(String value) {
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
    private User user;
    private String createdAt;
    private ResourceType resourceType;
    private Long resourceId;
    private String state;

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
     * Returns the user.
     *
     * @return the result
     */

    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the resource type.
     *
     * @return the result
     */

    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the resource type value
     */

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Returns the resource id.
     *
     * @return the result
     */

    public Long getResourceId() {
        return resourceId;
    }

    /**
     * Sets the resource id.
     *
     * @param resourceId the resource id value
     */

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
