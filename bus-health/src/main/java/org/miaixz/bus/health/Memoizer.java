/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * A memoizer function that stores the output corresponding to a particular set of inputs. When called subsequently with
 * the same memoized inputs, it returns the memoized result instead of recomputing it.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Memoizer {

    /**
     * Supplier for the default expiration time (in nanoseconds) for memoized values, configured via {@link Config}.
     */
    private static final Supplier<Long> DEFAULT_EXPIRATION_NANOS = memoize(
            Memoizer::queryExpirationConfig,
            TimeUnit.MINUTES.toNanos(1));

    /**
     * Queries the memoizer expiration configuration.
     *
     * @return The configured expiration time in nanoseconds.
     */
    private static long queryExpirationConfig() {
        return TimeUnit.MILLISECONDS.toNanos(Config.get(Config._UTIL_MEMOIZER_EXPIRATION, 300));
    }

    /**
     * Returns the expiration time for installed applications.
     *
     * @return The expiration time in nanoseconds, currently 1 minute.
     */
    public static long installedAppsExpiration() {
        return TimeUnit.MINUTES.toNanos(1);
    }

    /**
     * Returns the default expiration time (in nanoseconds) for memoized values, after which they will be refreshed.
     * This can be updated by setting the {@link Config} property {@code bus.health.memoizer.expiration} to a value in
     * milliseconds.
     *
     * @return The time in nanoseconds that memoized values are held before being refreshed.
     */
    public static long defaultExpiration() {
        return DEFAULT_EXPIRATION_NANOS.get();
    }

    /**
     * Stores a supplier in a delegate function that computes only once, and then again only after the time-to-live
     * (ttl) has expired.
     *
     * @param <T>      The type of object supplied.
     * @param original The {@link java.util.function.Supplier} to memoize.
     * @param ttlNanos The time in nanoseconds to retain the computed value. If negative, the value is retained
     *                 indefinitely.
     * @return A memoized version of the supplier.
     */
    public static <T> Supplier<T> memoize(Supplier<T> original, long ttlNanos) {
        // Adapted from Guava's ExpiringMemoizingSupplier
        return new Supplier<>() {

            /**
             * The original supplier.
             */
            private final Supplier<T> delegate = original;
            /**
             * The memoized value, which may be null.
             */
            private volatile T value;
            /**
             * The expiration time in nanoseconds.
             */
            private volatile long expirationNanos;

            @Override
            public T get() {
                long nanos = expirationNanos;
                long now = System.nanoTime();
                if (nanos == 0 || (ttlNanos >= 0 && now - nanos >= 0)) {
                    synchronized (this) {
                        if (nanos == expirationNanos) { // Recheck to avoid race condition
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
     * Stores a supplier in a delegate function that computes only once.
     *
     * @param <T>      The type of object supplied.
     * @param original The {@link java.util.function.Supplier} to memoize.
     * @return A memoized version of the supplier.
     */
    public static <T> Supplier<T> memoize(Supplier<T> original) {
        return memoize(original, -1L);
    }

}
