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
import java.util.Objects;

/**
 * Immutable summary produced from one Spring Boot startup lifecycle.
 *
 * @param appName                    application name
 * @param applicationBootElapsedTime total startup duration in milliseconds
 * @param applicationBootTime        startup timestamp in milliseconds since the epoch
 * @param stageStats                 ordered Spring startup stages
 * @author Kimi Liu
 */
public record SpringStartupSummary(String appName, long applicationBootElapsedTime, long applicationBootTime,
        List<Stage> stageStats) {

    /**
     * Copies mutable input and validates duration values.
     */
    public SpringStartupSummary {
        if (applicationBootElapsedTime < 0) {
            throw new IllegalArgumentException("applicationBootElapsedTime must not be negative");
        }
        stageStats = List.copyOf(stageStats == null ? List.of() : stageStats);
    }

    /**
     * Immutable duration for one Spring Boot startup stage.
     *
     * @param name           stable stage name
     * @param durationMillis stage duration in milliseconds
     */
    public record Stage(String name, long durationMillis) {

        /**
         * JVM startup before Bus startup collection is activated.
         */
        public static final String JVM_STARTING = "JvmStartingStage";
        /**
         * Spring environment preparation.
         */
        public static final String ENVIRONMENT_PREPARE = "EnvironmentPrepareStage";
        /**
         * Application-context preparation.
         */
        public static final String APPLICATION_CONTEXT_PREPARE = "ApplicationContextPrepareStage";
        /**
         * Application-context loading.
         */
        public static final String APPLICATION_CONTEXT_LOAD = "ApplicationContextLoadStage";
        /**
         * Application-context refresh.
         */
        public static final String APPLICATION_CONTEXT_REFRESH = "ApplicationContextRefreshStage";

        /**
         * Validates one Spring startup stage.
         */
        public Stage {
            Objects.requireNonNull(name, "name");
            if (durationMillis < 0) {
                throw new IllegalArgumentException("durationMillis must not be negative");
            }
        }

        /**
         * Creates a stage from two wall-clock timestamps.
         *
         * @param name      stable stage name
         * @param startTime stage start time in milliseconds
         * @param endTime   stage end time in milliseconds
         * @return immutable startup stage
         */
        public static Stage between(String name, long startTime, long endTime) {
            return new Stage(name, Math.max(0, endTime - startTime));
        }

    }

}
