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

/**
 * Property and resource keys used by early environment processing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class EnvironmentKeys {

    /**
     * Spring metadata resource directory.
     */
    public static final String SPRING_CONTEXT_PATH = "META-INF/spring";
    /**
     * Default Spring profile name.
     */
    public static final String DEFAULT_PROFILE = "default";
    /**
     * Bus configuration property-source name.
     */
    public static final String BUS_PROPERTY_SOURCE = "configurationProperties";
    /**
     * High-priority Bus property-source name.
     */
    public static final String BUS_HIGH_PRIORITY = "priorityConfig";
    /**
     * Active Bus scenes property.
     */
    public static final String BUS_SCENES = "bus.scenes";
    /**
     * Classpath directory containing Bus scene resources.
     */
    public static final String BUS_SCENES_PATH = "bus/scenes";
    /**
     * Default Bus logging directory.
     */
    public static final String BUS_LOGGING_PATH = "/logs";
    /**
     * Spring logging file-path property.
     */
    public static final String LOGGING_PATH = "logging.file.path";
    /**
     * Spring logging file property prefix.
     */
    public static final String LOGGING_PATH_PREFIX = "logging.file.";
    /**
     * Root Spring logging-level property.
     */
    public static final String LOGGING_LEVEL = "logging.level";
    /**
     * Spring logging-level property prefix.
     */
    public static final String LOGGING_LEVEL_PREFIX = "logging.level.";
    /**
     * Spring console logging-pattern property.
     */
    public static final String LOGGING_PATTERN_CONSOLE = "logging.pattern.console";
    /**
     * Spring file logging-pattern property.
     */
    public static final String LOGGING_PATTERN_FILE = "logging.pattern.file";
    /**
     * Spring application-name property.
     */
    public static final String APPLICATION_NAME = "spring.application.name";
    /**
     * Conventional Spring Cloud bootstrap profile.
     */
    public static final String CLOUD_BOOTSTRAP = "bootstrap";
    /**
     * Spring Cloud bootstrap configuration type name.
     */
    public static final String CLOUD_BOOTSTRAP_CONFIGURATION = "org.springframework.cloud.bootstrap.BootstrapConfiguration";
    /**
     * Spring Cloud bootstrap enabled property.
     */
    public static final String CLOUD_BOOTSTRAP_ENABLED = "spring.cloud.bootstrap.enabled";
    /**
     * Spring Cloud bootstrap marker type name.
     */
    public static final String CLOUD_BOOTSTRAP_MARKER = "org.springframework.cloud.bootstrap.marker.Marker";
    /**
     * Master Bus environment-processing flag.
     */
    public static final String ENVIRONMENT_ENABLED = "bus.spring.environment.enabled";
    /**
     * Bus logging environment-processing flag.
     */
    public static final String LOGGING_ENABLED = "bus.spring.logging.enabled";
    /**
     * Bus scene environment-processing flag.
     */
    public static final String SCENES_ENABLED = "bus.spring.scenes.enabled";
    /**
     * Bus configuration listener flag.
     */
    public static final String CONFIG_ENABLED = "bus.spring.config.enabled";
    /**
     * Bus Spring Cloud configuration listener flag.
     */
    public static final String CLOUD_CONFIG_ENABLED = "bus.spring.cloud-config.enabled";
    /**
     * Bus startup metrics flag.
     */
    public static final String STARTUP_ENABLED = "bus.spring.startup.enabled";

    /**
     * Prevents instantiation of this constants holder.
     */
    private EnvironmentKeys() {
        // No initialization required.
    }

}
