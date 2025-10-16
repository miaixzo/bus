/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.loader;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.Loader;

/**
 * A lazy loader that defers object loading until the {@code get()} method is called. This loader is typically used for
 * objects that are large and may not always be used, helping to reduce resource consumption during startup. It uses a
 * double-checked locking mechanism to ensure thread-safe initialization and prevent duplicate or lost loading in a
 * multi-threaded environment.
 *
 * @param <T> The type of the object to be loaded lazily.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class LazyLoader<T> implements Loader<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267795270L;

    /**
     * The lazily loaded object. It is volatile to ensure visibility across threads.
     */
    private volatile T object;

    /**
     * Retrieves the lazily loaded object. The first time this method is called, the object is initialized and then
     * returned. Subsequent calls directly return the already initialized object.
     *
     * @return The lazily loaded object.
     */
    @Override
    public T get() {
        T result = object;
        if (result == null) {
            synchronized (this) {
                result = object;
                if (result == null) {
                    object = result = init();
                }
            }
        }
        return result;
    }

    /**
     * Checks if the object has been initialized.
     *
     * @return {@code true} if the object has been initialized, {@code false} otherwise.
     */
    @Override
    public boolean isInitialized() {
        return null != object;
    }

    /**
     * Initializes the object to be loaded. This method is called only once when the object is first accessed.
     *
     * @return The initialized object.
     */
    protected abstract T init();

}
