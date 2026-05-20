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
 * The container expiration policy class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
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

    /**
     * Returns the cadence.
     *
     * @return the result
     */

    public String getCadence() {
        return cadence;
    }

    /**
     * Sets the cadence.
     *
     * @param cadence the cadence value
     */

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    /**
     * Sets the cadence and returns this instance.
     *
     * @param cadence the cadence value
     * @return the result
     */

    public ContainerExpirationPolicy withCadence(String cadence) {
        this.cadence = cadence;
        return this;
    }

    /**
     * Returns the enabled.
     *
     * @return the result
     */

    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled value
     */

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the enabled and returns this instance.
     *
     * @param enabled the enabled value
     * @return the result
     */

    public ContainerExpirationPolicy withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Returns the keep n.
     *
     * @return the result
     */

    public Integer getKeepN() {
        return keepN;
    }

    /**
     * Sets the keep n.
     *
     * @param keepN the keep n value
     */

    public void setKeepN(Integer keepN) {
        this.keepN = keepN;
    }

    /**
     * Sets the keep n and returns this instance.
     *
     * @param keepN the keep n value
     * @return the result
     */

    public ContainerExpirationPolicy withKeepN(Integer keepN) {
        this.keepN = keepN;
        return this;
    }

    /**
     * Returns the older than.
     *
     * @return the result
     */

    public String getOlderThan() {
        return olderThan;
    }

    /**
     * Sets the older than.
     *
     * @param olderThan the older than value
     */

    public void setOlderThan(String olderThan) {
        this.olderThan = olderThan;
    }

    /**
     * Sets the older than and returns this instance.
     *
     * @param olderThan the older than value
     * @return the result
     */

    public ContainerExpirationPolicy withOlderThan(String olderThan) {
        this.olderThan = olderThan;
        return this;
    }

    /**
     * Returns the name regex.
     *
     * @return the result
     */

    public String getNameRegex() {
        return nameRegex;
    }

    /**
     * Sets the name regex.
     *
     * @param nameRegex the name regex value
     */

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    /**
     * Sets the name regex and returns this instance.
     *
     * @param nameRegex the name regex value
     * @return the result
     */

    public ContainerExpirationPolicy withNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        return this;
    }

    /**
     * Returns the name regex keep.
     *
     * @return the result
     */

    public String getNameRegexKeep() {
        return nameRegexKeep;
    }

    /**
     * Sets the name regex keep.
     *
     * @param nameRegexKeep the name regex keep value
     */

    public void setNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
    }

    /**
     * Sets the name regex keep and returns this instance.
     *
     * @param nameRegexKeep the name regex keep value
     * @return the result
     */

    public ContainerExpirationPolicy withNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
        return this;
    }

    /**
     * Returns the next run at.
     *
     * @return the result
     */

    public String getNextRunAt() {
        return nextRunAt;
    }

    /**
     * Sets the next run at.
     *
     * @param nextRunAt the next run at value
     */

    public void setNextRunAt(String nextRunAt) {
        this.nextRunAt = nextRunAt;
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
