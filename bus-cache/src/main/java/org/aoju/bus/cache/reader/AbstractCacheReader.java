package org.aoju.bus.cache.reader;

import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.invoker.BaseInvoker;
import org.aoju.bus.logger.Logger;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractCacheReader {

    public abstract Object read(CacheHolder cacheHolder, CacheMethod cacheMethod, BaseInvoker baseInvoker, boolean needWrite) throws Throwable;

    Object doLogInvoke(ThrowableSupplier<Object> throwableSupplier) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return throwableSupplier.get();
        } finally {
            Logger.debug("method invoke total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

    @FunctionalInterface
    protected interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }
}
