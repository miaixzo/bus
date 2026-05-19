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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum for the various Pipeline status values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum PipelineStatus {

    /**
     * The created pipeline status.
     */
    CREATED,
    /**
     * The waiting for resource pipeline status.
     */
    WAITING_FOR_RESOURCE,
    /**
     * The preparing pipeline status.
     */
    PREPARING,
    /**
     * The pending pipeline status.
     */
    PENDING,
    /**
     * The running pipeline status.
     */
    RUNNING,
    /**
     * The success pipeline status.
     */
    SUCCESS,
    /**
     * The failed pipeline status.
     */
    FAILED,
    /**
     * The canceled pipeline status.
     */
    CANCELED,
    /**
     * The canceling pipeline status.
     */
    CANCELING,
    /**
     * The skipped pipeline status.
     */
    SKIPPED,
    /**
     * The manual pipeline status.
     */
    MANUAL,
    /**
     * The scheduled pipeline status.
     */
    SCHEDULED;

    private static Map<String, PipelineStatus> valuesMap = new HashMap<>(11);

    static {
        for (PipelineStatus status : PipelineStatus.values())
            valuesMap.put(status.toValue(), status);
    }

    /**
     * Returns the value.
     *
     * @param value the value value
     * @return the result
     */

    @JsonCreator
    public static PipelineStatus forValue(String value) {
        return valuesMap.get(value);
    }

    /**
     * Returns the value.
     *
     * @return the result
     */

    @JsonValue
    public String toValue() {
        return (name().toLowerCase());
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (name().toLowerCase());
    }

}
