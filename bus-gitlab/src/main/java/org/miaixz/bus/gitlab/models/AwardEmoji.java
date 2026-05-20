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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The award emoji class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AwardEmoji implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238050300L;

    private Long id;
    private String name;
    private User user;
    private Date createdAt;
    private Date updatedAt;
    private Long awardableId;
    private AwardableType awardableType;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return this.id;
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
        return this.name;
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
     * Returns the awardable id.
     *
     * @return the result
     */

    public Long getAwardableId() {
        return awardableId;
    }

    /**
     * Sets the awardable id.
     *
     * @param awardableId the awardable id value
     */

    public void setAwardableId(Long awardableId) {
        this.awardableId = awardableId;
    }

    /**
     * Returns the awardable type.
     *
     * @return the result
     */

    public AwardableType getAwardableType() {
        return awardableType;
    }

    /**
     * Sets the awardable type.
     *
     * @param awardableType the awardable type value
     */

    public void setAwardableType(AwardableType awardableType) {
        this.awardableType = awardableType;
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
     * The awardable type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum AwardableType {

        /**
         * The issue awardable type.
         */
        ISSUE,
        /**
         * The merge request awardable type.
         */
        MERGE_REQUEST,
        /**
         * The note awardable type.
         */
        NOTE,
        /**
         * The snippet awardable type.
         */
        SNIPPET;

        private static JacksonJsonEnumHelper<AwardableType> enumHelper = new JacksonJsonEnumHelper<>(
                AwardableType.class, true);

        /**
         * Executes the for value operation.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static AwardableType forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Executes the to value operation.
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

}
