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
package org.miaixz.bus.core.lang.loader;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.Loader;

/**
 * An atomic reference-based lazy loader. It implements lazy loading using {@link AtomicReference}, following this
 * process:
 * 
 * <pre>
 * 1. Checks if a loaded object already exists in the reference; if so, returns it.
 * 2. If not, it initializes an object and then re-checks if another thread has already loaded an object into the reference.
 *    If not, it sets its newly created object; otherwise, it returns the existing object.
 * </pre>
 * 
 * Although the object initialization operation might be called multiple times in a multi-threaded environment when the
 * object has not yet been created, it always ensures that the same object is eventually returned.
 *
 * @param <T> The type of the object to be loaded.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AtomicLoader<T> implements Loader<T>, Serializable {

    /**
     * Constructs a new AtomicLoader. Utility class constructor for static access.
     */
    private AtomicLoader() {
    }

    @Serial
    private static final long serialVersionUID = 2852267211560L;

    /**
     * The atomic reference holding the lazily loaded object.
     */
    private final AtomicReference<T> reference = new AtomicReference<>();

    /**
     * Retrieves the lazily loaded object. The first time this method is called, the object is initialized and then
     * returned. Subsequent calls directly return the already initialized object.
     *
     * @return The lazily loaded object.
     */
    @Override
    public T get() {
        T result = reference.get();

        if (result == null) {
            result = init();
            if (!reference.compareAndSet(null, result)) {
                // Another thread has already created this object
                result = reference.get();
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
        return null != reference.get();
    }

    /**
     * Initializes the object to be loaded. This method is called only once when the object is first accessed.
     *
     * @return The initialized object.
     */
    protected abstract T init();

}
