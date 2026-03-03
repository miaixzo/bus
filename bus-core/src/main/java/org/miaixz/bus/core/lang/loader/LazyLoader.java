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
