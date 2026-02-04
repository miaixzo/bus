/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;
import java.io.Serial;

public class ContainerExpirationPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852250662811L;

    private String cadence;
    private Boolean enabled;
    private Integer keepN;
    private String olderThan;
    private String nameRegex;
    private String nameRegexKeep;

    private String nextRunAt;

    public String getCadence() {
        return cadence;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public ContainerExpirationPolicy withCadence(String cadence) {
        this.cadence = cadence;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public ContainerExpirationPolicy withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getKeepN() {
        return keepN;
    }

    public void setKeepN(Integer keepN) {
        this.keepN = keepN;
    }

    public ContainerExpirationPolicy withKeepN(Integer keepN) {
        this.keepN = keepN;
        return this;
    }

    public String getOlderThan() {
        return olderThan;
    }

    public void setOlderThan(String olderThan) {
        this.olderThan = olderThan;
    }

    public ContainerExpirationPolicy withOlderThan(String olderThan) {
        this.olderThan = olderThan;
        return this;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    public ContainerExpirationPolicy withNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        return this;
    }

    public String getNameRegexKeep() {
        return nameRegexKeep;
    }

    public void setNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
    }

    public ContainerExpirationPolicy withNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
        return this;
    }

    public String getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(String nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
