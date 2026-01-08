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
