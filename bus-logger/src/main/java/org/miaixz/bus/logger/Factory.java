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

import org.miaixz.bus.core.instance.Instances;

/**
 * An interface for logger factories.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Factory {

    /**
     * Gets the name of the logging framework. This is used to identify the current logging implementation.
     *
     * @return the name of the logging framework.
     */
    String getName();

    /**
     * Creates a new logger instance with the specified name.
     *
     * @param name the name of the logger.
     * @return a new {@link Provider} instance.
     */
    Provider of(String name);

    /**
     * Creates a new logger instance for the specified class.
     *
     * @param clazz the class for which to create the logger.
     * @return a new {@link Provider} instance.
     */
    Provider of(Class<?> clazz);

    /**
     * Gets a singleton logger instance with the specified name.
     *
     * @param name the name of the logger.
     * @return a singleton {@link Provider} instance.
     */
    default Provider getProvider(final String name) {
        return Instances.get(getName() + name, () -> of(name));
    }

    /**
     * Gets a singleton logger instance for the specified class.
     *
     * @param clazz the class for which to get the logger.
     * @return a singleton {@link Provider} instance.
     */
    default Provider getProvider(final Class<?> clazz) {
        return Instances.get(getName() + clazz.getName(), () -> of(clazz));
    }

}
