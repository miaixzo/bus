/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache;

import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context {

    /**
     * cache接口实现
     */
    private Map<String, CacheX> caches;
    /**
     * 缓存分组命中率统计
     */
    private Hitting hitting;
    /**
     * 是否开启Cache(全局开关)
     */
    private Switch cache;
    /**
     * 是否开启缓存防击穿
     */
    private Switch prevent;

    public static Context newConfig(Map<String, CacheX> caches) {
        Context config = new Context();
        config.caches = caches;
        config.cache = Switch.ON;
        config.prevent = Switch.OFF;
        config.hitting = null;
        return config;
    }

    public boolean isPreventOn() {
        return null != prevent && prevent == Switch.ON;
    }

    public Map<String, CacheX> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheX> caches) {
        this.caches = caches;
    }

    public Hitting getHitting() {
        return hitting;
    }

    public void setHitting(Hitting hitting) {
        this.hitting = hitting;
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

    /**
     * 开关
     */
    public enum Switch {
        ON, OFF
    }

}
