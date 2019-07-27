package org.aoju.bus.cache.reader;

import org.aoju.bus.cache.CacheConfig;
import org.aoju.bus.cache.CacheManager;
import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.invoker.BaseInvoker;
import org.aoju.bus.cache.provider.BaseProvider;
import org.aoju.bus.cache.support.KeyGenerator;
import org.aoju.bus.cache.support.PatternGenerator;
import org.aoju.bus.cache.support.PreventObjects;
import org.aoju.bus.logger.Logger;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Singleton
public class SingleCacheReader extends AbstractCacheReader {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private CacheConfig config;

    @Inject(optional = true)
    private BaseProvider baseProvider;

    @Override
    public Object read(CacheHolder cacheHolder, CacheMethod cacheMethod, BaseInvoker baseInvoker, boolean needWrite) throws Throwable {
        String key = KeyGenerator.generateSingleKey(cacheHolder, baseInvoker.getArgs());
        Object readResult = cacheManager.readSingle(cacheHolder.getCache(), key);

        doRecord(readResult, key, cacheHolder);
        // 命中
        if (readResult != null) {
            // 是放击穿对象
            if (PreventObjects.isPrevent(readResult)) {
                return null;
            }

            return readResult;
        }


        // not hit
        // invoke method
        Object invokeResult = doLogInvoke(baseInvoker::proceed);
        if (invokeResult != null && cacheMethod.getInnerReturnType() == null) {
            cacheMethod.setInnerReturnType(invokeResult.getClass());
        }

        if (!needWrite) {
            return invokeResult;
        }

        if (invokeResult != null) {
            cacheManager.writeSingle(cacheHolder.getCache(), key, invokeResult, cacheHolder.getExpire());
            return invokeResult;
        }

        // invokeResult is null
        if (config.isPreventOn()) {
            cacheManager.writeSingle(cacheHolder.getCache(), key, PreventObjects.getPreventObject(), cacheHolder.getExpire());
        }

        return null;
    }

    private void doRecord(Object result, String key, CacheHolder cacheHolder) {
        Logger.info("single cache hit rate: {}/1, key: {}", result == null ? 0 : 1, key);
        if (this.baseProvider != null) {
            String pattern = PatternGenerator.generatePattern(cacheHolder);

            if (result != null) {
                this.baseProvider.hitIncr(pattern, 1);
            }
            this.baseProvider.reqIncr(pattern, 1);
        }
    }

}
