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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Adds opt-in logging defaults through standard Spring Boot environment keys only.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Initializes the post-processor that contributes Bus logging defaults without overriding user properties.
     */
    public LoggingEnvironmentPostProcessor() {
        // No initialization required.
    }

    /**
     * Name of the generated logging defaults property source.
     */
    private static final String PROPERTY_SOURCE = "busLoggingDefaults";
    /**
     * Default console logging pattern.
     */
    private static final String CONSOLE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%5p] %-50.50logger{50} %5.5L : %m%n";
    /**
     * Default file logging pattern.
     */
    private static final String FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%5p] %-50.50logger{50} %5.5L : %m%n";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(EnvironmentKeys.LOGGING_ENABLED, Boolean.class, false)
                || environment.getPropertySources().contains(PROPERTY_SOURCE)) {
            return;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        addIfMissing(environment, defaults, EnvironmentKeys.LOGGING_PATTERN_CONSOLE, CONSOLE_PATTERN);
        addIfMissing(environment, defaults, EnvironmentKeys.LOGGING_PATTERN_FILE, FILE_PATTERN);
        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE, defaults));
        }
    }

    /**
     * Adds one default property only when the environment does not already define it.
     *
     * @param environment Spring environment
     * @param defaults    mutable default property map
     * @param key         property key
     * @param value       default property value
     */
    private void addIfMissing(
            ConfigurableEnvironment environment,
            Map<String, Object> defaults,
            String key,
            String value) {
        if (!environment.containsProperty(key)) {
            defaults.put(key, value);
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

}
