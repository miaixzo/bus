/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
