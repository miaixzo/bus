/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.env;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An {@link EnvironmentPostProcessor} implementation for logging configuration detection and initialization.
 * <p>
 * This post-processor ensures that certain logging-related system properties are set, maintaining compatibility and
 * providing default values if not explicitly configured.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Ensures compatibility by setting specific system properties for logging.
     * <p>
     * The following system properties are set:
     * <ol>
     * <li>{@code spring.output.ansi.enabled}</li>
     * <li>{@code logging.path}</li>
     * <li>{@code file.encoding}</li>
     * <li>{@code logging.pattern.console}</li>
     * <li>{@code logging.pattern.file}</li>
     * </ol>
     *
     * @param context A map containing logging configuration properties.
     * @param keep    A boolean flag indicating whether to apply compatibility settings.
     */
    public static void keepCompatible(Map<String, String> context, boolean keep) {
        if (!keep) {
            return;
        }
        // Logging path
        String loggingPath = System.getProperty(GeniusBuilder.LOGGING_PATH, context.get(GeniusBuilder.LOGGING_PATH));
        System.setProperty(GeniusBuilder.LOGGING_PATH, loggingPath);
        // File encoding
        String fileEncoding = System.getProperty(Keys.FILE_ENCODING, context.get(Keys.FILE_ENCODING));
        System.setProperty(Keys.FILE_ENCODING, fileEncoding);

        // Console logging pattern
        String patternConsole = System
                .getProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE, context.get(GeniusBuilder.LOGGING_PATTERN_CONSOLE));
        if (StringKit.isEmpty(patternConsole)) {
            patternConsole = "%green(%d{yyyy-MM-dd HH:mm:ss.SSSXXX}) [%highlight(%5p)] %magenta(${PID:- }) %yellow(-) %highlight(%-50.50logger{50}) %yellow(%5.5L) %cyan(:) %magenta(%m%n)";
        }
        System.setProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE, patternConsole);

        // File logging pattern
        String patternFile = System
                .getProperty(GeniusBuilder.LOGGING_PATTERN_FILE, context.get(GeniusBuilder.LOGGING_PATTERN_FILE));
        if (StringKit.isEmpty(patternFile)) {
            patternFile = "%d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%5p] ${PID:- } - %-50.50logger{50} %5.5L : %m%n";
        }
        System.setProperty(GeniusBuilder.LOGGING_PATTERN_FILE, patternFile);

    }

    /**
     * Post-processes the environment to detect and initialize logging configurations.
     * <p>
     * This method loads logging-related properties from the environment and applies compatibility settings using
     * {@link #keepCompatible(Map, boolean)}.
     * </p>
     *
     * @param environment The configurable environment.
     * @param application The Spring application instance.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Debug check: Suppress BeanPostProcessor warnings only in non-debug mode and non-GraalVM environment.
        boolean isDebugMode = System.getProperty("spring.profiles.active", "").contains("debug")
                || System.getProperty("debug", "false").equals("true")
                || System.getProperty("logging.level.org.miaixz", "INFO").equals("DEBUG");

        if (!isDebugMode && !Keys.IS_GRAALVM_NATIVE) {
            System.setProperty(
                    "logging.level.org.springframework.context.support.PostProcessorRegistrationDelegate",
                    "OFF");
        }

        Map<String, String> context = new HashMap<>();
        loadLogConfiguration(
                GeniusBuilder.LOGGING_PATH,
                environment.getProperty(GeniusBuilder.LOGGING_PATH),
                context,
                Keys.get(Keys.USER_NAME) + GeniusBuilder.BUS_LOGGING_PATH);
        loadLogConfiguration(Keys.FILE_ENCODING, environment.getProperty(Keys.FILE_ENCODING), context, null);

        loadLogConfiguration(
                GeniusBuilder.LOGGING_PATTERN_CONSOLE,
                environment.getProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE),
                context,
                null);

        loadLogConfiguration(
                GeniusBuilder.LOGGING_PATTERN_FILE,
                environment.getProperty(GeniusBuilder.LOGGING_PATTERN_FILE),
                context,
                null);

        keepCompatible(context, true);
    }

    /**
     * Returns the order value for this post-processor.
     * <p>
     * This ensures that logging configurations are processed after {@link ConfigDataEnvironmentPostProcessor}.
     * </p>
     *
     * @return The order value.
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    /**
     * Loads a logging configuration property into the context map.
     *
     * @param key          The property key.
     * @param value        The property value from the environment.
     * @param context      The map to store the property in.
     * @param defaultValue A default value to use if the property is not found in the environment.
     */
    public void loadLogConfiguration(String key, String value, Map<String, String> context, String defaultValue) {
        if (StringKit.hasText(value)) {
            context.put(key, value);
        } else if (StringKit.hasText(defaultValue)) {
            context.put(key, defaultValue);
        }
    }

}
