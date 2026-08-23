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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.spring.boot.startup.SpringStartupSummary.Stage;

/**
 * Collects Spring lifecycle stages and creates the completed startup summary.
 *
 * @author Kimi Liu
 */
public class SpringStartupCollector {

    /**
     * Application name associated with the current startup.
     */
    private final String appName;
    /**
     * Application startup timestamp in milliseconds since the epoch.
     */
    private final long applicationBootTime;
    /**
     * Stages retained in lifecycle order and keyed to prevent duplicate callbacks.
     */
    private final Map<String, Stage> stages = new LinkedHashMap<>();

    /**
     * Creates an isolated collector for one application startup.
     *
     * @param appName             application name
     * @param applicationBootTime application startup timestamp
     */
    public SpringStartupCollector(String appName, long applicationBootTime) {
        this.appName = appName;
        this.applicationBootTime = applicationBootTime;
    }

    /**
     * Adds a stage unless the same lifecycle stage has already been recorded.
     *
     * @param stage completed startup stage
     */
    public synchronized void addStage(Stage stage) {
        Stage value = Objects.requireNonNull(stage, "stage");
        stages.putIfAbsent(value.name(), value);
    }

    /**
     * Returns whether a lifecycle stage has already completed.
     *
     * @param name stable stage name
     * @return whether the stage is present
     */
    public synchronized boolean containsStage(String name) {
        return stages.containsKey(name);
    }

    /**
     * Completes collection and creates an immutable startup summary.
     *
     * @param applicationBootEndTime application startup completion timestamp
     * @return completed startup summary
     */
    public synchronized SpringStartupSummary complete(long applicationBootEndTime) {
        long elapsedTime = Math.max(0, applicationBootEndTime - applicationBootTime);
        return new SpringStartupSummary(appName, elapsedTime, applicationBootTime, List.copyOf(stages.values()));
    }

}
