/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.cache;

import org.aoju.bus.cache.provider.BaseProvider;
import org.aoju.bus.cache.support.cache.Cache;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public class CacheConfig {

    // cache接口实现
    private Map<String, Cache> caches;

    // 缓存分组命中率统计
    private BaseProvider provider;

    // 是否开启Cache(全局开关)
    private Switch cache;

    // 是否开启缓存防击穿
    private Switch prevent;

    public static CacheConfig newConfig(Map<String, Cache> caches) {
        CacheConfig config = new CacheConfig();
        config.caches = caches;
        config.cache = Switch.ON;
        config.prevent = Switch.OFF;
        config.provider = null;
        return config;
    }

    public boolean isPreventOn() {
        return prevent != null && prevent == Switch.ON;
    }

    public Map<String, Cache> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, Cache> caches) {
        this.caches = caches;
    }

    public BaseProvider getProvider() {
        return provider;
    }

    public void setProvider(BaseProvider provider) {
        this.provider = provider;
    }

    public Switch getCache() {
        return cache;
    }

    public void setCache(Switch cache) {
        this.cache = cache;
    }

    public Switch getPrevent() {
        return prevent;
    }

    public void setPrevent(Switch prevent) {
        this.prevent = prevent;
    }

    public enum Switch {
        ON,
        OFF
    }

}
