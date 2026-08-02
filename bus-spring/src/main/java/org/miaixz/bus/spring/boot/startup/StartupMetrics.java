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

import java.util.List;

/**
 * Immutable aggregate produced for exactly one application startup.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StartupMetrics {

    /**
     * Application name captured for the startup report.
     */
    private final String appName;
    /**
     * Total application startup duration in milliseconds.
     */
    private final long applicationBootElapsedTime;
    /**
     * Application startup timestamp in milliseconds since the epoch.
     */
    private final long applicationBootTime;
    /**
     * Immutable startup-stage metrics in reporting order.
     */
    private final List<BaseMetrics> stageStats;

    /**
     * Creates an immutable application startup metrics snapshot.
     *
     * @param appName                    application name
     * @param applicationBootElapsedTime elapsed boot time in milliseconds
     * @param applicationBootTime        application boot timestamp
     * @param stageStats                 ordered startup stage metrics
     */
    public StartupMetrics(String appName, long applicationBootElapsedTime, long applicationBootTime,
            List<? extends BaseMetrics> stageStats) {
        this.appName = appName;
        this.applicationBootElapsedTime = applicationBootElapsedTime;
        this.applicationBootTime = applicationBootTime;
        this.stageStats = List.copyOf(stageStats == null ? List.of() : stageStats);
    }

    /**
     * Exposes the application name associated with this startup snapshot.
     *
     * @return the app name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Exposes the measured application startup duration in milliseconds.
     *
     * @return the application boot elapsed time
     */
    public long getApplicationBootElapsedTime() {
        return applicationBootElapsedTime;
    }

    /**
     * Exposes the wall-clock timestamp at which application startup began.
     *
     * @return the application boot time
     */
    public long getApplicationBootTime() {
        return applicationBootTime;
    }

    /**
     * Exposes the immutable top-level startup stage snapshots.
     *
     * @return the stage stats
     */
    public List<BaseMetrics> getStageStats() {
        return stageStats;
    }

}
