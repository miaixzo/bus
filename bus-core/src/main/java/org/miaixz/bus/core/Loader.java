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
package org.miaixz.bus.core;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * An abstract interface for object loading. By implementing this interface, you can define custom loading strategies,
 * such as lazy loading or multi-threaded loading.
 *
 * @param <T> The type of the object to be loaded.
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Loader<T> extends Serializable {

    /**
     * Gets the fully loaded object. This method should block until the object is prepared and ready to be returned.
     *
     * @return The loaded object.
     */
    T get();

    /**
     * Checks whether the object has been initialized.
     *
     * @return {@code true} if the object has been initialized, {@code false} otherwise.
     */
    default boolean isInitialized() {
        return true;
    }

    /**
     * Executes the given consumer if the object has been initialized.
     *
     * @param consumer The consumer to execute. If {@code null}, no action is performed.
     */
    default void ifInitialized(final Consumer<T> consumer) {
        if (null != consumer && this.isInitialized()) {
            consumer.accept(get());
        }
    }

}
