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
import java.util.Map;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The health check item class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HealthCheckItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256751978L;

    private HealthCheckStatus status;
    private Map<String, String> labels;
    private String message;

    /**
     * Returns the status.
     *
     * @return the result
     */

    public HealthCheckStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(HealthCheckStatus status) {
        this.status = status;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * Returns the message.
     *
     * @return the result
     */

    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message value
     */

    public void setMessage(String message) {
        this.message = message;
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
