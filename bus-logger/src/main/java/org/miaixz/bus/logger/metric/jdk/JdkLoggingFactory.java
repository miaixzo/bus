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
package org.miaixz.bus.logger.metric.jdk;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * A factory for creating {@link java.util.logging.Logger} instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code JdkLoggingFactory}. This factory is responsible for creating loggers based on the
     * {@code java.util.logging} framework. It also attempts to read a {@code logging.properties} configuration file
     * from the classpath.
     */
    public JdkLoggingFactory() {
        super("java.util.logging.Logger");
        readConfig();
    }

    /**
     * Creates a logger provider for the specified name.
     *
     * @param name the name of the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider of(final String name) {
        return new JdkLoggingProvider(name);
    }

    /**
     * Creates a logger provider for the specified class.
     *
     * @param clazz the class for which to create the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new JdkLoggingProvider(clazz);
    }

    /**
     * Reads the {@code logging.properties} configuration file from the classpath. If the file is not found, a warning
     * is printed to {@code System.err}, and the default configuration from {@code %JRE_HOME%/lib/logging.properties} is
     * used.
     */
    private void readConfig() {
        // To avoid circular references, do not use related tool classes during log initialization.
        final InputStream in = ResourceKit.getStreamSafe("logging.properties");
        if (null == in) {
            System.err.println(
                    "[WARN] Can not find [logging.properties], use [%JRE_HOME%/lib/logging.properties] as default!");
            return;
        }

        try {
            LogManager.getLogManager().readConfiguration(in);
        } catch (final Exception e) {
            Console.error(e, "Read [logging.properties] from classpath error!");
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (final Exception e1) {
                Console.error(e, "Read [logging.properties] from [%JRE_HOME%/lib/logging.properties] error!");
            }
        } finally {
            IoKit.closeQuietly(in);
        }
    }

}
