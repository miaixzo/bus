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
package org.miaixz.bus.core.lang.loader;

import java.io.Serial;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;

/**
 * A functional lazy loader. It takes a {@link Supplier} to generate an object. The object is created only when it is
 * first needed, after which the supplier is discarded. This loader is useful for large objects that may not always be
 * used, thus reducing resource consumption at startup. It extends {@link LazyLoader}, and thread safety is handled by
 * the parent class.
 *
 * @param <T> The type of the object being loaded.
 * @author Kimi Liu
 * @see LazyLoader
 * @since Java 17+
 */
public class LazyFunLoader<T> extends LazyLoader<T> {

    @Serial
    private static final long serialVersionUID = 2852267697038L;

    /**
     * The supplier function for generating the object.
     */
    private Supplier<T> supplier;

    /**
     * Constructor.
     *
     * @param supplier The supplier function for generating the object.
     */
    public LazyFunLoader(final Supplier<T> supplier) {
        Assert.notNull(supplier);
        this.supplier = supplier;
    }

    /**
     * Static factory method for creating a {@code LazyFunLoader}.
     *
     * @param supplier The supplier function for generating the object.
     * @param <T>      The type of the object.
     * @return a new {@code LazyFunLoader} instance.
     */
    public static <T> LazyFunLoader<T> of(final Supplier<T> supplier) {
        Assert.notNull(supplier, "supplier must be not null!");
        return new LazyFunLoader<>(supplier);
    }

    /**
     * Initializes the object by calling the supplier. The supplier is then set to `null` to be garbage collected.
     *
     * @return The initialized object.
     */
    @Override
    protected T init() {
        final T t = this.supplier.get();
        this.supplier = null; // Discard the supplier to free up memory
        return t;
    }

}
