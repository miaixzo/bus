/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache.magic;

import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Hitting;
import org.miaixz.bus.cache.Manage;
import org.miaixz.bus.cache.support.KeyGenerator;
import org.miaixz.bus.cache.support.PatternGenerator;
import org.miaixz.bus.cache.support.PreventObjects;
import org.miaixz.bus.core.annotation.Inject;
import org.miaixz.bus.core.annotation.Singleton;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@Singleton
public class SingleCacheReader extends AbstractReader {

    @Inject
    private Manage cacheManager;

    @Inject
    private Context config;

    @Inject(optional = true)
    private Hitting baseHitting;

    @Override
    public Object read(AnnoHolder annoHolder, MethodHolder methodHolder, ProxyChain baseInvoker, boolean needWrite) throws Throwable {
        String key = KeyGenerator.generateSingleKey(annoHolder, baseInvoker.getArguments());
        Object readResult = cacheManager.readSingle(annoHolder.getCache(), key);

        doRecord(readResult, key, annoHolder);
        // 命中
        if (null != readResult) {
            // 是放击穿对象
            if (PreventObjects.isPrevent(readResult)) {
                return null;
            }

            return readResult;
        }

        Object invokeResult = doLogInvoke(baseInvoker::proceed);
        if (null != invokeResult && null == methodHolder.getInnerReturnType()) {
            methodHolder.setInnerReturnType(invokeResult.getClass());
        }

        if (!needWrite) {
            return invokeResult;
        }

        if (null != invokeResult) {
            cacheManager.writeSingle(annoHolder.getCache(), key, invokeResult, annoHolder.getExpire());
            return invokeResult;
        }

        if (config.isPreventOn()) {
            cacheManager.writeSingle(annoHolder.getCache(), key, PreventObjects.getPreventObject(), annoHolder.getExpire());
        }

        return null;
    }

    private void doRecord(Object result, String key, AnnoHolder annoHolder) {
        Logger.info("single cache hit rate: {}/1, key: {}", null == result ? 0 : 1, key);
        if (null != this.baseHitting) {
            String pattern = PatternGenerator.generatePattern(annoHolder);
            if (null != result) {
                this.baseHitting.hitIncr(pattern, 1);
            }
            this.baseHitting.reqIncr(pattern, 1);
        }
    }

}
