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
