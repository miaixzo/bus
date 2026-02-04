/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.logger;

import java.net.URL;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.logger.metric.apache.commons.CommonsLoggingFactory;
import org.miaixz.bus.logger.metric.apache.log4j.Log4jLoggingFactory;
import org.miaixz.bus.logger.metric.console.NormalLoggingFactory;
import org.miaixz.bus.logger.metric.jdk.JdkLoggingFactory;
import org.miaixz.bus.logger.metric.slf4j.Slf4jLoggingFactory;

/**
 * A simple factory for retrieving {@link Factory} instances. This class provides a static factory method to obtain a
 * logger factory instance. It automatically detects the appropriate logging framework from the classpath.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder {

    private static volatile Factory DEFAULT_FACTORY;

    /**
     * Default constructor.
     */
    public Holder() {

    }

    /**
     * Gets the singleton {@link Factory} instance. It automatically creates the appropriate template engine object
     * based on the template engine JAR introduced by the user.
     *
     * @return The singleton {@link Factory} instance.
     */
    public static Factory getFactory() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Sets the default logger factory by providing a factory instance.
     *
     * @param factory The logger factory instance to set as the default.
     * @see Slf4jLoggingFactory
     * @see Log4jLoggingFactory
     * @see CommonsLoggingFactory
     * @see JdkLoggingFactory
     * @see NormalLoggingFactory
     */
    public static void setDefaultFactory(final Factory factory) {
        DEFAULT_FACTORY = factory;
        Instances.put(Holder.class.getName(), factory);
        factory.of(Holder.class).debug("Custom Use [{}] Logger.", factory.getName());
    }

    /**
     * Sets the default logger factory by providing the factory class.
     *
     * @param clazz The class of the logger factory to set as the default.
     * @see Slf4jLoggingFactory
     * @see Log4jLoggingFactory
     * @see CommonsLoggingFactory
     * @see JdkLoggingFactory
     * @see NormalLoggingFactory
     */
    public static void setDefaultFactory(final Class<? extends Factory> clazz) {
        try {
            setDefaultFactory(ReflectKit.newInstance(clazz));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Can not instance LogFactory class!", e);
        }
    }

    /**
     * Creates a new logger factory instance of the specified class.
     *
     * @param clazz The class of the logger factory to create.
     * @return A new instance of the specified logger factory.
     */
    public static Factory of(final Class<? extends Factory> clazz) {
        return ReflectKit.newInstance(clazz);
    }

    /**
     * Determines the logging implementation to use. It checks for the presence of logging library JARs in a specific
     * order. If no logging library is found, it checks for a `logging.properties` file in the classpath. If the file
     * exists, {@link JdkLoggingFactory} is used; otherwise, {@link NormalLoggingFactory} is used.
     *
     * @return The determined logger factory instance.
     */
    public static Factory of() {
        final Factory factory = doFactory();
        factory.of(Registry.class).debug("Use [{}] Logger As Default.", factory.getName());
        return factory;
    }

    /**
     * Determines the logging implementation to use. It checks for the presence of logging library JARs in a specific
     * order. If no logging library is found, it checks for a `logging.properties` file in the classpath. If the file
     * exists, {@link JdkLoggingFactory} is used; otherwise, {@link NormalLoggingFactory} is used.
     *
     * @return The determined logger factory instance.
     */
    private static Factory doFactory() {
        if (null != DEFAULT_FACTORY) {
            return DEFAULT_FACTORY;
        }

        final Factory factory = NormalSpiLoader.loadFirstAvailable(Factory.class);
        if (null != factory) {
            return factory;
        }

        // When no supportable log library is found, the basis for judgment is:
        // when the configuration file of JDK Logging is in the classpath, use JDK Logging, otherwise use Console.
        final URL url = ResourceKit.getResourceUrl("logging.properties");
        return (null != url) ? new JdkLoggingFactory() : new NormalLoggingFactory();
    }

    /**
     * This inner class holds the singleton instance of the {@link Factory}. This approach ensures that the instance is
     * created only when it is first needed.
     */
    private static class InstanceHolder {

        /**
         * The singleton instance of the {@link Factory}.
         */
        public static final Factory INSTANCE = of();

    }

}
