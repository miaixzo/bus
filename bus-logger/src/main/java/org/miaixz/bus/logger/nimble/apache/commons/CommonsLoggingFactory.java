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
package org.miaixz.bus.logger.nimble.apache.commons;

import org.apache.commons.logging.LogFactory;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating {@link org.apache.commons.logging.Log} instances.
 *
 * @author Kimi Liu
 */
public class CommonsLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code CommonsLoggingFactory}. This factory is responsible for creating loggers based on the
     * Apache Commons Logging framework. It also checks for the existence of the
     * {@link org.apache.commons.logging.LogFactory} class.
     */
    public CommonsLoggingFactory() {
        super("org.apache.commons.logging.Log");
        exists(LogFactory.class);
    }

    /**
     * Creates a new logger instance with the specified name.
     *
     * @param name the name of the logger.
     * @return a new {@link Provider} instance.
     */
    @Override
    public Provider of(final String name) {
        return new CommonsLoggingProvider(name);
    }

    /**
     * Creates a new logger instance for the specified class.
     *
     * @param clazz the class for which to create the logger.
     * @return a new {@link Provider} instance.
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new CommonsLoggingProvider(clazz);
    }

    /**
     * Checks if the logging implementation exists.
     * <p>
     * This method is used to verify the presence of the required classes for a specific logging framework. It is called
     * during the automatic detection process (e.g., in {@code detectLogFactory}). If the implementation class does not
     * exist, this method should throw an exception (like {@code ClassNotFoundException}), which signals the detection
     * mechanism to try the next available logging framework.
     * </p>
     *
     * @param logClassName the logging implementation class to check.
     */
    @Override
    protected void exists(final Class<?> logClassName) {
        super.exists(logClassName);
        // This is to ensure that the logging framework is initialized.
        of(CommonsLoggingFactory.class);
    }

}
