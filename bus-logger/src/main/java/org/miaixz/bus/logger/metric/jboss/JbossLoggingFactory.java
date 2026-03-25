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
package org.miaixz.bus.logger.metric.jboss;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating {@link org.jboss.logging.Logger} instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JbossLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code JbossLoggingFactory}. This factory is responsible for creating loggers based on the JBoss
     * Logging framework. It also checks for the existence of the {@link org.jboss.logging.Logger} class.
     */
    public JbossLoggingFactory() {
        super("org.jboss.logging.Logger");
        exists(org.jboss.logging.Logger.class);
    }

    /**
     * Creates a logger provider for the specified name.
     *
     * @param name the name of the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider of(final String name) {
        return new JbossLoggingProvider(name);
    }

    /**
     * Creates a logger provider for the specified class.
     *
     * @param clazz the class for which to create the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new JbossLoggingProvider(clazz);
    }

}
