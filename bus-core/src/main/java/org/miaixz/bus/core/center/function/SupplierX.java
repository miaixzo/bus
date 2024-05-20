/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.function;

import org.miaixz.bus.core.toolkit.ExceptionKit;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 可序列化的Supplier
 *
 * @param <R> 返回值类型
 * @author Kimi Liu
 * @see Supplier
 * @since Java 17+
 */
@FunctionalInterface
public interface SupplierX<R> extends Supplier<R>, Serializable {

    /**
     * last
     *
     * @param serSups lambda
     * @param <T>     type
     * @return lambda
     */
    @SafeVarargs
    static <T> SupplierX<T> last(final SupplierX<T>... serSups) {
        return Stream.of(serSups).reduce((l, r) -> r).orElseGet(() -> () -> null);
    }

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception wrapped checked exception
     */
    R getting() throws Throwable;

    /**
     * Gets a result.
     *
     * @return a result
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
