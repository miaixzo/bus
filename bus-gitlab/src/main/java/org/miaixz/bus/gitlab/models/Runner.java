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

import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;

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

    public RunnerType getRunnerType() {
        return runnerType;
    }

    public void setRunnerType(RunnerType runnerType) {
        this.runnerType = runnerType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIs_shared() {
        return isShared;
    }

    public void setIs_shared(Boolean is_shared) {
        this.isShared = is_shared;
    }

    /**
     * Enum to use for RunnersApi filtering on status.
     */
    public enum RunnerStatus {

        ACTIVE, ONLINE, PAUSED, OFFLINE;

        private static JacksonJsonEnumHelper<RunnerStatus> enumHelper = new JacksonJsonEnumHelper<>(RunnerStatus.class);

        @JsonCreator
        public static RunnerStatus forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for RunnersApi filtering on type.
     */
    public enum RunnerType {

        INSTANCE_TYPE, GROUP_TYPE, PROJECT_TYPE;

        private static JacksonJsonEnumHelper<RunnerType> enumHelper = new JacksonJsonEnumHelper<>(RunnerType.class);

        @JsonCreator
        public static RunnerType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOnline() {
        return this.online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public RunnerStatus getStatus() {
        return this.status;
    }

    public void setStatus(RunnerStatus status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Runner withId(Long id) {
        this.id = id;
        return this;
    }

    public Runner withDescription(String description) {
        this.description = description;
        return this;
    }

    public Runner withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public Runner withIsShared(Boolean isShared) {
        this.isShared = isShared;
        return this;
    }

    public Runner withName(String name) {
        this.name = name;
        return this;
    }

    public Runner withOnline(Boolean online) {
        this.online = online;
        return this;
    }

    public Runner withStatus(RunnerStatus status) {
        this.status = status;
        return this;
    }

    public Runner withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
