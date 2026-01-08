/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
