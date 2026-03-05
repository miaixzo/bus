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

import java.util.ArrayList;
import java.util.List;

/**
 * A component for collecting and aggregating startup consumption statistics, used to gather various performance
 * indicators during the application startup process.
 * <p>
 * This class provides functionality to record the application name, total startup elapsed time, and a list of
 * statistics for each startup stage. It can be used for comprehensive monitoring and analysis of application startup
 * performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class StartupMetrics {

    /**
     * The name of the application.
     * <p>
     * Records the name of the current application instance, used for identification.
     * </p>
     */
    private String appName;

    /**
     * The total elapsed time in milliseconds for the application to boot.
     * <p>
     * Records the total time taken from the start of the application to its completion.
     * </p>
     */
    private long applicationBootElapsedTime = 0;

    /**
     * The timestamp in milliseconds when the application started.
     */
    private long applicationBootTime;

    /**
     * A list of {@link BaseMetrics} objects, each representing the statistics for a specific startup stage.
     * <p>
     * This list stores detailed performance metrics for various phases of the application startup.
     * </p>
     */
    private List<BaseMetrics> stageStats = new ArrayList<>();

}
