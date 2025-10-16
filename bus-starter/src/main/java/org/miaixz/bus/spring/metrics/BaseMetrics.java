/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
