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
 * The external status check status class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ExternalStatusCheckStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253997632L;

    private Long id;
    private String name;
    private String externalUrl;
    private Status status;

    /**
     * The status enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Status {

        /**
         * The passed status.
         */
        PASSED,
        /**
         * The failed status.
         */
        FAILED,
        /**
         * The pending status.
         */
        PENDING;

        private static JacksonJsonEnumHelper<Status> enumHelper = new JacksonJsonEnumHelper<>(Status.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Status forValue(String value) {
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
     * Returns the external url.
     *
     * @return the result
     */

    public String getExternalUrl() {
        return externalUrl;
    }

    /**
     * Sets the external url.
     *
     * @param externalUrl the external url value
     */

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    /**
     * Returns the status.
     *
     * @return the result
     */

    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(Status status) {
        this.status = status;
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
