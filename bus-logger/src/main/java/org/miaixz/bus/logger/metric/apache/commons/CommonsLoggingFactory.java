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
package org.miaixz.bus.logger.metric.apache.commons;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating {@link org.apache.commons.logging.Log} instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommonsLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code CommonsLoggingFactory}. This factory is responsible for creating loggers based on the
     * Apache Commons Logging framework. It also checks for the existence of the
     * {@link org.apache.commons.logging.LogFactory} class.
     */
    public CommonsLoggingFactory() {
        super("org.apache.commons.logging.Log");
        exists(org.apache.commons.logging.LogFactory.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final String name) {
        return new CommonsLoggingProvider(name);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new CommonsLoggingProvider(clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    protected void exists(final Class<?> logClassName) {
        super.exists(logClassName);
        // This is to ensure that the logging framework is initialized.
        of(CommonsLoggingFactory.class);
    }

}
