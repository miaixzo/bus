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
package org.miaixz.bus.logger.nimble.console;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating console loggers that print messages using {@code System.out.println}.
 *
 * @author Kimi Liu
 */
public class NormalLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code NormalLoggingFactory}. This factory creates loggers that output to the standard console.
     */
    public NormalLoggingFactory() {
        super("System.out.println");
    }

    /**
     * Creates a new logger instance with the specified name.
     *
     * @param name the name of the logger.
     * @return a new {@link Provider} instance.
     */
    @Override
    public Provider of(final String name) {
        return new NormalLoggingProvider(name);
    }

    /**
     * Creates a new logger instance for the specified class.
     *
     * @param clazz the class for which to create the logger.
     * @return a new {@link Provider} instance.
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new NormalLoggingProvider(clazz);
    }

}
