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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The runner class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Runner implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280665555L;

    private Long id;
    private String description;
    private RunnerType runnerType;
    private Boolean active;
    private Boolean isShared;
    private List<String> tags;
    private String name;
    private Boolean online;
    private RunnerStatus status;
    private String ipAddress;

    /**
     * Returns the runner type.
     *
     * @return the result
     */

    public RunnerType getRunnerType() {
        return runnerType;
    }

    /**
     * Sets the runner type.
     *
     * @param runnerType the runner type value
     */

    public void setRunnerType(RunnerType runnerType) {
        this.runnerType = runnerType;
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
     * Returns the tags.
     *
     * @return the result
     */

    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     *
     * @param tags the tags value
     */

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the active.
     *
     * @return the result
     */

    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the active value
     */

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Returns the is shared.
     *
     * @return the result
     */

    public Boolean getIs_shared() {
        return isShared;
    }

    /**
     * Sets the is shared.
     *
     * @param is_shared the is shared value
     */

    public void setIs_shared(Boolean is_shared) {
        this.isShared = is_shared;
    }

    /**
     * Enum to use for RunnersApi filtering on status.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum RunnerStatus {

        /**
         * The active runner status.
         */
        ACTIVE,
        /**
         * The online runner status.
         */
        ONLINE,
        /**
         * The paused runner status.
         */
        PAUSED,
        /**
         * The offline runner status.
         */
        OFFLINE;

        private static JacksonJsonEnumHelper<RunnerStatus> enumHelper = new JacksonJsonEnumHelper<>(RunnerStatus.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static RunnerStatus forValue(String value) {
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

    /**
     * Enum to use for RunnersApi filtering on type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum RunnerType {

        /**
         * The instance type runner type.
         */
        INSTANCE_TYPE,
        /**
         * The group type runner type.
         */
        GROUP_TYPE,
        /**
         * The project type runner type.
         */
        PROJECT_TYPE;

        private static JacksonJsonEnumHelper<RunnerType> enumHelper = new JacksonJsonEnumHelper<>(RunnerType.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static RunnerType forValue(String value) {
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
     * Returns the online.
     *
     * @return the result
     */

    public Boolean getOnline() {
        return this.online;
    }

    /**
     * Sets the online.
     *
     * @param online the online value
     */

    public void setOnline(Boolean online) {
        this.online = online;
    }

    /**
     * Returns the status.
     *
     * @return the result
     */

    public RunnerStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(RunnerStatus status) {
        this.status = status;
    }

    /**
     * Returns the ip address.
     *
     * @return the result
     */

    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the ip address.
     *
     * @param ipAddress the ip address value
     */

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public Runner withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public Runner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the active and returns this instance.
     *
     * @param active the active value
     * @return the result
     */

    public Runner withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * Sets the is shared and returns this instance.
     *
     * @param isShared the is shared value
     * @return the result
     */

    public Runner withIsShared(Boolean isShared) {
        this.isShared = isShared;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Runner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the online and returns this instance.
     *
     * @param online the online value
     * @return the result
     */

    public Runner withOnline(Boolean online) {
        this.online = online;
        return this;
    }

    /**
     * Sets the status and returns this instance.
     *
     * @param status the status value
     * @return the result
     */

    public Runner withStatus(RunnerStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the ip address and returns this instance.
     *
     * @param ipAddress the ip address value
     * @return the result
     */

    public Runner withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
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
