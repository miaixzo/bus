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
package org.miaixz.bus.spring.metrics;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Base model for startup metrics, used to track and record various indicators during the startup process.
 * <p>
 * This class provides functionality to record start time, end time, elapsed time, and custom attributes, which can be
 * used for monitoring and analyzing system startup performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class BaseMetrics {

    /**
     * A map to store custom attributes as key-value pairs.
     */
    private final Map<String, String> attributes = new HashMap<>();

    /**
     * The name of the metric.
     */
    private String name;

    /**
     * The start time of the metric in milliseconds.
     */
    private long startTime;

    /**
     * The end time of the metric in milliseconds.
     */
    private long endTime;

    /**
     * The elapsed time (cost) of the metric in milliseconds, calculated as {@code endTime - startTime}.
     */
    private long cost;

    /**
     * Sets the end time and automatically calculates the elapsed time (cost).
     * <p>
     * When the end time is set, the {@code cost} field is automatically updated with {@code endTime - startTime}.
     * </p>
     *
     * @param endTime The end time in milliseconds.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.cost = this.endTime - this.startTime;
    }

    /**
     * Adds a custom attribute to the {@link #attributes} map.
     *
     * @param key   The key of the attribute.
     * @param value The value of the attribute.
     */
    public void putAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    /**
     * Retrieves the value of a custom attribute by its key.
     *
     * @param key The key of the attribute to retrieve.
     * @return The value of the attribute, or {@code null} if the key is not found.
     */
    public String getAttribute(String key) {
        return this.attributes.get(key);
    }
}
