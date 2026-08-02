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

/**
 * A metrics model for recording module refresh status, used to track and record various indicators during the Spring
 * module refresh process.
 * <p>
 * This class extends {@link ChildrenMetrics<BeanMetrics>} and can manage multiple {@link BeanMetrics} sub-indicators.
 * It also records the name of the thread that performed the refresh operation, used for monitoring and analyzing module
 * refresh performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ModuleMetrics extends ChildrenMetrics<BeanMetrics> {

    /**
     * Creates metrics for one startup module and its Bean initialization stages.
     *
     * @param name       module name
     * @param startTime  module start time in milliseconds
     * @param endTime    module end time in milliseconds
     * @param threadName initialization thread name
     * @param children   Bean initialization metrics
     */
    public ModuleMetrics(String name, long startTime, long endTime, String threadName,
            java.util.List<BeanMetrics> children) {
        super(name, startTime, endTime, children);
        this.threadName = threadName == null || threadName.isBlank() ? "unknown" : threadName;
    }

    /**
     * The name of the thread that executed the module refresh operation.
     * <p>
     * Records the thread name to analyze concurrency performance and thread usage.
     * </p>
     */
    private final String threadName;

    /**
     * Returns the thread name.
     *
     * @return the thread name
     */
    public String getThreadName() {
        return threadName;
    }

}
