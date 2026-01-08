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
package org.miaixz.bus.cache.reader;

import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Metrics;
import org.miaixz.bus.cache.Manage;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * An abstract base class for cache readers.
 * <p>
 * This class provides a foundational framework for cache reading logic, including dependency injection for core
 * components and a utility method for logging method invocation times. Subclasses must implement the specific logic for
 * reading from the cache.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractReader {

    /**
     * The manager for all registered cache instances.
     */
    protected Manage manage;

    /**
     * The configuration context for the cache module.
     */
    protected Context context;

    /**
     * The component for tracking cache performance metrics.
     */
    protected Metrics metrics;

    /**
     * Executes the cache read operation.
     * <p>
     * Subclasses must implement this method to define the specific strategy for reading from the cache (e.g.,
     * single-key or multi-key lookup), handling cache misses, and optionally writing back the result of the original
     * method invocation.
     * </p>
     *
     * @param annoHolder   The holder for the caching annotations on the method.
     * @param methodHolder The holder for metadata about the method itself.
     * @param baseInvoker  The proxy chain invoker to proceed with the original method call on a cache miss.
     * @param needWrite    If {@code true}, the result of the method invocation should be written to the cache.
     * @return The value from the cache or the result of the original method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    public abstract Object read(
            AnnoHolder annoHolder,
            MethodHolder methodHolder,
            ProxyChain baseInvoker,
            boolean needWrite) throws Throwable;

    /**
     * Executes a supplier function and logs the total execution time.
     * <p>
     * This wrapper ensures that the invocation time is logged, even if the supplier throws an exception.
     * </p>
     *
     * @param throwableSupplier The supplier function to execute.
     * @return The result of the supplier function.
     * @throws Throwable if the supplier function throws an exception.
     */
    Object doLogInvoke(ThrowableSupplier<Object> throwableSupplier) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return throwableSupplier.get();
        } finally {
            Logger.debug("method invoke total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

    /**
     * Sets the cache manager.
     *
     * @param manage The cache manager.
     */
    public void setManage(Manage manage) {
        this.manage = manage;
    }

    /**
     * Sets the cache context configuration.
     *
     * @param config The cache context configuration.
     */
    public void setContext(Context config) {
        this.context = config;
    }

    /**
     * Sets the cache metrics component.
     *
     * @param metrics The cache metrics component.
     */
    public void setHitting(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * A functional interface for a supplier that can throw a {@link Throwable}.
     *
     * @param <T> The type of the result.
     */
    @FunctionalInterface
    protected interface ThrowableSupplier<T> {

        /**
         * Gets a result.
         *
         * @return a result
         * @throws Throwable if unable to compute a result
         */
        T get() throws Throwable;
    }

}
