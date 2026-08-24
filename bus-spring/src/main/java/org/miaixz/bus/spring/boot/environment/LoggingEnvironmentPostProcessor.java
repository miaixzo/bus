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
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.IterableConfigurationPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Bridges Bus logging properties to Spring Boot's native logging namespace.
 * <p>
 * Any {@code bus.logging.*} property is exposed as the matching {@code logging.*} property with higher priority than
 * existing native logging properties. Spring Boot remains the owner of the logging configuration model.
 *
 * @author Kimi Liu
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Name of the generated logging bridge property source.
     */
    private static final String PROPERTY_SOURCE = "busLoggingNamespaceBridge";
    /**
     * Name of the property source containing Bus logging pattern defaults.
     */
    private static final String DEFAULTS_PROPERTY_SOURCE = "busLoggingPatternDefaults";

    /**
     * Property that controls whether Bus logging pattern defaults are contributed.
     */
    private static final ConfigurationPropertyName PATTERN_DEFAULTS = ConfigurationPropertyName
            .of(EnvironmentKeys.BUS_LOGGING_PATTERN_DEFAULTS);

    /**
     * Default console logging pattern.
     */
    private static final String CONSOLE_PATTERN = "%green(%d{yyyy-MM-dd HH:mm:ss.SSSXXX}) [%highlight(%5p)] "
            + "%magenta(${PID:- }) %yellow(-) %highlight(%-50.50logger{50}) %yellow(%5.5L) %cyan(:) %magenta(%m%n)";

    /**
     * Default file logging pattern.
     */
    private static final String FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%5p] ${PID:- } - "
            + "%-50.50logger{50} %5.5L : %m%n";

    /**
     * Source namespace for Bus logging properties.
     */
    private static final ConfigurationPropertyName SOURCE_NAMESPACE = ConfigurationPropertyName
            .of(EnvironmentKeys.BUS_LOGGING_PREFIX);
    /**
     * Source namespace element count.
     */
    private static final int SOURCE_NAMESPACE_ELEMENTS = SOURCE_NAMESPACE.getNumberOfElements();

    /**
     * Initializes the post-processor that exposes Bus logging properties as native Spring Boot logging properties.
     */
    public LoggingEnvironmentPostProcessor() {
        // No initialization required.
    }

    /**
     * Maps {@code bus.logging.*} properties to {@code logging.*} properties.
     *
     * @param environment configurable application environment
     * @param application current Spring Boot application
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        addPatternDefaults(environment, propertySources);
        if (propertySources.contains(PROPERTY_SOURCE)) {
            propertySources.remove(PROPERTY_SOURCE);
        }

        Map<String, Object> bridges = new LinkedHashMap<>();
        for (ConfigurationPropertySource source : ConfigurationPropertySources.get(environment)) {
            if (source instanceof IterableConfigurationPropertySource iterableSource) {
                iterableSource.stream().filter(this::isBusLoggingProperty)
                        .forEach(name -> addBridgeProperty(source, name, bridges));
            }
        }

        if (!bridges.isEmpty()) {
            propertySources.addFirst(new MapPropertySource(PROPERTY_SOURCE, bridges));
        }
    }

    /**
     * Returns whether the supplied property belongs to the Bus logging namespace.
     *
     * @param name property name
     * @return {@code true} when the property is a descendant of {@code bus.logging}
     */
    private boolean isBusLoggingProperty(ConfigurationPropertyName name) {
        return name != null && name.getNumberOfElements() > SOURCE_NAMESPACE_ELEMENTS
                && SOURCE_NAMESPACE.isAncestorOf(name) && !PATTERN_DEFAULTS.equals(name);
    }

    /**
     * Contributes the Bus console and file patterns as low-priority defaults.
     *
     * @param environment     configurable application environment
     * @param propertySources mutable environment property sources
     */
    private void addPatternDefaults(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
        if (propertySources.contains(DEFAULTS_PROPERTY_SOURCE)) {
            propertySources.remove(DEFAULTS_PROPERTY_SOURCE);
        }

        boolean enabled = environment.getProperty(PATTERN_DEFAULTS.toString(), Boolean.class, true);
        if (!enabled) {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(EnvironmentKeys.LOGGING_PATTERN_CONSOLE, CONSOLE_PATTERN);
        defaults.put(EnvironmentKeys.LOGGING_PATTERN_FILE, FILE_PATTERN);
        propertySources.addLast(new MapPropertySource(DEFAULTS_PROPERTY_SOURCE, defaults));
    }

    /**
     * Adds one bridged logging property while preserving the highest-priority Bus source value.
     *
     * @param source  property source
     * @param name    source property name
     * @param bridges mutable bridge property map
     */
    private void addBridgeProperty(
            ConfigurationPropertySource source,
            ConfigurationPropertyName name,
            Map<String, Object> bridges) {
        String targetName = getTargetName(name);
        if (bridges.containsKey(targetName)) {
            return;
        }
        ConfigurationProperty property = source.getConfigurationProperty(name);
        if (property != null && property.getValue() != null) {
            bridges.put(targetName, property.getValue());
        }
    }

    /**
     * Builds the matching Spring Boot logging property name.
     *
     * @param name source Bus logging property name
     * @return target Spring Boot logging property name
     */
    private String getTargetName(ConfigurationPropertyName name) {
        StringBuilder targetName = new StringBuilder(EnvironmentKeys.LOGGING_PREFIX);
        for (int i = SOURCE_NAMESPACE_ELEMENTS; i < name.getNumberOfElements(); i++) {
            if (i > SOURCE_NAMESPACE_ELEMENTS) {
                targetName.append(Symbol.C_DOT);
            }
            targetName.append(name.getElement(i, Form.ORIGINAL));
        }
        return targetName.toString();
    }

    /**
     * Applies the namespace bridge immediately after Spring Boot has processed configuration data.
     *
     * @return environment post-processor order
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

}
