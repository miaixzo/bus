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
package org.miaixz.bus.spring.boot.environment;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Property keys used by early Spring Boot environment processing.
 *
 * @author Kimi Liu
 */
public final class EnvironmentKeys {

    /**
     * Spring application-name property.
     */
    public static final String APPLICATION_NAME = "spring.application.name";
    /**
     * Bus configuration property-source name.
     */
    public static final String BUS_PROPERTY_SOURCE = "configurationProperties";
    /**
     * Spring Boot logging property prefix.
     */
    public static final String LOGGING_PREFIX = "logging" + Symbol.DOT;
    /**
     * Root Spring logging-level property.
     */
    public static final String LOGGING_LEVEL = LOGGING_PREFIX + "level";
    /**
     * Spring Boot console logging-pattern property.
     */
    public static final String LOGGING_PATTERN_CONSOLE = LOGGING_PREFIX + "pattern" + Symbol.DOT + "console";
    /**
     * Spring Boot file logging-pattern property.
     */
    public static final String LOGGING_PATTERN_FILE = LOGGING_PREFIX + "pattern" + Symbol.DOT + "file";
    /**
     * Bus logging configuration namespace.
     */
    public static final String BUS_LOGGING_PREFIX = "bus" + Symbol.DOT + "logging";
    /**
     * Bus logging-pattern defaults flag.
     */
    public static final String BUS_LOGGING_PATTERN_DEFAULTS = BUS_LOGGING_PREFIX + Symbol.DOT + "pattern" + Symbol.DOT
            + "defaults";
    /**
     * Bus configuration-listener flag.
     */
    public static final String CONFIG_ENABLED = "bus.config.enabled";
    /**
     * Bus metrics integration flag.
     */
    public static final String METRICS_ENABLED = "bus.metrics.enabled";
    /**
     * Bus startup-metrics flag.
     */
    public static final String STARTUP_METRICS_ENABLED = "bus.metrics.startup.enabled";

    /**
     * Prevents instantiation of this constants holder.
     */
    private EnvironmentKeys() {
        // No initialization required.
    }

}
