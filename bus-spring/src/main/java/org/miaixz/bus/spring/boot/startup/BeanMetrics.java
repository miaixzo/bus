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

import java.util.Map;

/**
 * A metrics model for recording Spring Bean initialization status, used to track and record various indicators during
 * the Spring Bean initialization process.
 * <p>
 * This class extends {@link ChildrenMetrics} and can record the bean's type information and actual refresh elapsed
 * time, used for monitoring and analyzing bean initialization performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BeanMetrics extends ChildrenMetrics<BeanMetrics> {

    /**
     * Creates metrics for one initialized Bean.
     *
     * @param name                   Bean name
     * @param startTime              initialization start time in milliseconds
     * @param endTime                initialization end time in milliseconds
     * @param attributes             stage attributes
     * @param children               nested Bean metrics
     * @param type                   Bean type name
     * @param realRefreshElapsedTime measured refresh time in milliseconds
     */
    public BeanMetrics(String name, long startTime, long endTime, Map<String, String> attributes,
            java.util.List<BeanMetrics> children, String type, long realRefreshElapsedTime) {
        super(name, startTime, endTime, attributes, children);
        this.type = type;
        this.realRefreshElapsedTime = realRefreshElapsedTime;
    }

    /**
     * The type information of the Bean, recording the bean's class name or type identifier.
     */
    private final String type;

    /**
     * The actual elapsed time in milliseconds for the bean's refresh operation.
     */
    private final long realRefreshElapsedTime;

    /**
     * Exposes the fully qualified type name of the initialized Bean.
     *
     * @return Bean type name
     */
    public String getType() {
        return type;
    }

    /**
     * Exposes the measured Bean initialization duration in milliseconds.
     *
     * @return refresh time in milliseconds
     */
    public long getRealRefreshElapsedTime() {
        return realRefreshElapsedTime;
    }

}
