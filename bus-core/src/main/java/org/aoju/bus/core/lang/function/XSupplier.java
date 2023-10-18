/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang.function;

import org.aoju.bus.core.exception.InternalException;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 结果提供者
 *
 * @param <T> 结果类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface XSupplier<T> extends Supplier<T>, Serializable {

    /**
     * last
     *
     * @param args 参数信息
     * @param <T>  结果类型
     * @return the object
     */
    @SafeVarargs
    static <T> XSupplier<T> last(final XSupplier<T>... args) {
        return Stream.of(args).reduce((l, r) -> r).orElseGet(() -> () -> null);
    }

    /**
     * 获取结果
     *
     * @return 结果信息
     * @throws Exception 包装的检查异常
     */
    T getting() throws Exception;

    /**
     * 获取结果
     *
     * @return 结果信息
     */
    @Override
    default T get() {
        try {
            return getting();
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

}
