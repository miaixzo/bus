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
package org.miaixz.bus.limiter.metric;

import org.miaixz.bus.core.cache.provider.TimedCache;
import org.miaixz.bus.core.xyz.CacheKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Holder;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;

import java.lang.reflect.Method;

/**
 * HOT_METHOD 模式处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodProvider implements Provider {

    private final TimedCache<String, Object> cache;

    public MethodProvider() {
        cache = CacheKit.newTimedCache(1000L * Holder.load().getSeconds());
        cache.schedulePrune(1000);
    }

    @Override
    public StrategyMode get() {
        return StrategyMode.HOT_METHOD;
    }

    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // 获取方法对应的key
        String hotKey = StringKit.format("{}-{}", Builder.resolveMethodName(method), org.miaixz.bus.crypto.Builder.md5Hex(JsonKit.toJsonString(args)));

        // 缓存操作
        if (cache.containsKey(hotKey)) {
            return cache.get(hotKey, false);
        } else {
            // 执行后缓存
            Object result = MethodKit.invoke(bean, method, args);
            cache.put(hotKey, result);
            return result;
        }
    }

}
