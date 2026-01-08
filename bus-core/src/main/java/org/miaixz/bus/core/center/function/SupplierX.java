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
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link Supplier} interface that supports throwing exceptions and combining multiple suppliers.
 *
 * @param <R> The type of results supplied by this supplier.
 * @author Kimi Liu
 * @see Supplier
 * @since Java 17+
 */
@FunctionalInterface
public interface SupplierX<R> extends Supplier<R>, Serializable {

    /**
     * Returns the last {@code SupplierX} from a given array of suppliers. If the array is empty, it returns a supplier
     * that supplies {@code null}.
     *
     * @param serSups An array of {@code SupplierX} instances to choose from.
     * @param <T>     The type of results supplied by the suppliers.
     * @return The last {@code SupplierX} instance in the array, or a supplier that returns {@code null} if the array is
     *         empty.
     */
    @SafeVarargs
    static <T> SupplierX<T> last(final SupplierX<T>... serSups) {
        return Stream.of(serSups).reduce((l, r) -> r).orElseGet(() -> () -> null);
    }

    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return A result.
     * @throws Throwable Any throwable exception that might occur during the operation.
     */
    R getting() throws Throwable;

    /**
     * Gets a result, automatically handling checked exceptions by wrapping them in a {@link RuntimeException}.
     *
     * @return A result.
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     */
    @Override
    default R get() {
        try {
            return getting();
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}
