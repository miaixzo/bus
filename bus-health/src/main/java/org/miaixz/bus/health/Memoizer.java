/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * 记忆化函数，用于存储与特定输入集对应的输出。后续使用已记忆的输入调用时，将返回记忆的结果，而不是重新计算。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Memoizer {

    /**
     * 默认过期时间（纳秒）的供应商，记忆化值的默认过期时间配置
     */
    private static final Supplier<Long> DEFAULT_EXPIRATION_NANOS = memoize(Memoizer::queryExpirationConfig,
            TimeUnit.MINUTES.toNanos(1));

    /**
     * 查询记忆化过期时间配置。
     *
     * @return 配置的过期时间（纳秒）
     */
    private static long queryExpirationConfig() {
        return TimeUnit.MILLISECONDS.toNanos(Config.get(Config._UTIL_MEMOIZER_EXPIRATION, 300));
    }

    /**
     * 已安装应用的记忆化过期时间。
     *
     * @return 1 分钟的纳秒数
     */
    public static long installedAppsExpiration() {
        return TimeUnit.MINUTES.toNanos(1);
    }

    /**
     * 记忆化值的默认过期时间（纳秒），在该时间过去后将刷新。可通过设置 {@link Config} 属性 <code>bus.health.memoizer.expiration</code> 为毫秒值来更新。
     *
     * @return 保持记忆化值的时间（纳秒）在刷新之前
     */
    public static long defaultExpiration() {
        return DEFAULT_EXPIRATION_NANOS.get();
    }

    /**
     * 将供应商存储在委托函数中，仅计算一次，且仅在生存时间（ttl）过期后再次计算。
     *
     * @param <T>      提供的对象类型
     * @param original 要记忆化的 {@link java.util.function.Supplier}
     * @param ttlNanos 保留计算的时间（纳秒）。如果为负，则无限期保留。
     * @return 供应商的记忆化版本
     */
    public static <T> Supplier<T> memoize(Supplier<T> original, long ttlNanos) {
        // 改编自 Guava 的 ExpiringMemoizingSupplier
        return new Supplier<>() {
            /**
             * 原始供应商
             */
            private final Supplier<T> delegate = original;
            /**
             * 记忆化的值，可能为 null
             */
            private volatile T value;
            /**
             * 过期时间（纳秒）
             */
            private volatile long expirationNanos;

            @Override
            public T get() {
                long nanos = expirationNanos;
                long now = System.nanoTime();
                if (nanos == 0 || (ttlNanos >= 0 && now - nanos >= 0)) {
                    synchronized (this) {
                        if (nanos == expirationNanos) { // 重新检查以避免竞争条件
                            T t = delegate.get();
                            value = t;
                            nanos = now + ttlNanos;
                            expirationNanos = (nanos == 0) ? 1 : nanos;
                            return t;
                        }
                    }
                }
                return value;
            }
        };
    }

    /**
     * 将供应商存储在委托函数中，仅计算一次。
     *
     * @param <T>      提供的对象类型
     * @param original 要记忆化的 {@link java.util.function.Supplier}
     * @return 供应商的记忆化版本
     */
    public static <T> Supplier<T> memoize(Supplier<T> original) {
        return memoize(original, -1L);
    }

}