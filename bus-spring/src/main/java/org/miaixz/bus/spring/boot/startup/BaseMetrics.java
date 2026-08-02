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
package org.miaixz.bus.spring.boot.startup;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable timing fields for one startup stage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BaseMetrics {

    /**
     * Mutable only while the startup report is assembled.
     */
    private final Map<String, String> attributes;
    /**
     * Startup stage name.
     */
    private final String name;
    /**
     * Stage start time in milliseconds.
     */
    private final long startTime;
    /**
     * Stage end time in milliseconds.
     */
    private final long endTime;
    /**
     * Non-negative elapsed time in milliseconds.
     */
    private final long cost;

    /**
     * Creates a startup stage without attributes.
     *
     * @param name      stage name
     * @param startTime stage start time in milliseconds
     * @param endTime   stage end time in milliseconds
     */
    public BaseMetrics(String name, long startTime, long endTime) {
        this(name, startTime, endTime, Map.of());
    }

    /**
     * Creates a startup stage from a defensive copy of its attributes.
     *
     * @param name       stage name
     * @param startTime  stage start time in milliseconds
     * @param endTime    stage end time in milliseconds
     * @param attributes stage attributes
     */
    public BaseMetrics(String name, long startTime, long endTime, Map<String, String> attributes) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = Math.max(0, endTime - startTime);
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
    }

    /**
     * Returns an immutable view of stage attributes.
     *
     * @return stage attributes
     */
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Returns one stage attribute.
     *
     * @param key attribute key
     * @return attribute value, or {@code null} when absent
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Exposes the stable name of the measured startup stage.
     *
     * @return stage name
     */
    public String getName() {
        return name;
    }

    /**
     * Exposes the stage start timestamp in milliseconds since the epoch.
     *
     * @return start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Exposes the stage completion timestamp in milliseconds since the epoch.
     *
     * @return end time in milliseconds
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the non-negative stage duration.
     *
     * @return elapsed time in milliseconds
     */
    public long getCost() {
        return cost;
    }

    /**
     * Returns the package-local mutable view used while diagnostic processors finalize the report.
     *
     * @return mutable stage attributes
     */
    Map<String, String> mutableAttributes() {
        return attributes;
    }

}
