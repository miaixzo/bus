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
package org.miaixz.bus.logger;

/**
 * A simple logger factory that provides cached logger instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class Registry {

    /**
     * Default constructor.
     */
    public Registry() {

    }

    /**
     * Gets a logger instance by name.
     *
     * @param name the name of the logger.
     * @return a {@link Provider} instance.
     */
    public static Provider get(final String name) {
        return Holder.getFactory().getProvider(name);
    }

    /**
     * Gets a logger instance by class.
     *
     * @param clazz the class for which to get the logger.
     * @return a {@link Provider} instance.
     */
    public static Provider get(final Class<?> clazz) {
        return Holder.getFactory().getProvider(clazz != null ? clazz : Logger.class);
    }

}
