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
package org.miaixz.bus.logger.metric.apache.log4j;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating {@link org.apache.logging.log4j.Logger} instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Log4jLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code Log4jLoggingFactory}. This factory is responsible for creating loggers based on the Log4j
     * 2 framework. It also checks for the existence of the {@link org.apache.logging.log4j.LogManager} class.
     */
    public Log4jLoggingFactory() {
        super("org.apache.logging.log4j.Logger");
        exists(org.apache.logging.log4j.LogManager.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final String name) {
        return new Log4jLoggingProvider(name);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new Log4jLoggingProvider(clazz);
    }

}
