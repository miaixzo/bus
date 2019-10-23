/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.reader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aoju.bus.cache.Context;
import org.aoju.bus.cache.Manage;
import org.aoju.bus.cache.Provider;
import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.proxy.ProxyChain;
import org.aoju.bus.cache.support.KeyGenerator;
import org.aoju.bus.cache.support.PatternGenerator;
import org.aoju.bus.cache.support.PreventObjects;
import org.aoju.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
@Singleton
public class SingleCacheReader extends AbstractReader {

    @Inject
    private Manage cacheManager;

    @Inject
    private Context config;

    @Inject(optional = true)
    private Provider baseProvider;

    @Override
    public Object read(CacheHolder cacheHolder, CacheMethod cacheMethod, ProxyChain baseInvoker, boolean needWrite) throws Throwable {
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
