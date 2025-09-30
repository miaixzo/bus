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

import org.miaixz.bus.core.lang.EnumValue;

import java.util.Map;

/**
 * 缓存上下文配置类
 * <p>
 * 用于配置缓存系统的全局参数，包括缓存实现、命中率统计组件、缓存开关和防击穿开关。 提供了创建默认配置的工厂方法，以及各种参数的getter和setter方法。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context {

    /**
     * 缓存接口实现映射，键为缓存名称，值为缓存实现实例
     */
    private Map<String, CacheX> caches;

    /**
     * 缓存分组命中率统计组件
     */
    private Metrics metrics;

    /**
     * 是否开启Cache(全局开关)
     */
    private EnumValue.Switch cache;

    /**
     * 是否开启缓存防击穿
     */
    private EnumValue.Switch prevent;

    /**
     * 创建默认配置的Context实例
     * <p>
     * 使用传入的缓存映射创建Context实例，默认开启缓存，关闭防击穿功能。
     * </p>
     *
     * @param caches 缓存接口实现映射，键为缓存名称，值为缓存实现实例
     * @return 配置好的Context实例
     */
    public static Context newConfig(Map<String, CacheX> caches) {
        Context config = new Context();
        config.caches = caches;
        config.cache = EnumValue.Switch.ON;
        config.prevent = EnumValue.Switch.OFF;
        config.metrics = null;
        return config;
    }

    /**
     * 判断是否开启了缓存防击穿功能
     *
     * @return 如果开启了防击穿功能则返回true，否则返回false
     */
    public boolean isPreventOn() {
        return null != prevent && prevent == EnumValue.Switch.ON;
    }

    /**
     * 获取缓存接口实现映射
     *
     * @return 缓存接口实现映射，键为缓存名称，值为缓存实现实例
     */
    public Map<String, CacheX> getCaches() {
        return caches;
    }

    /**
     * 设置缓存接口实现映射
     *
     * @param caches 缓存接口实现映射，键为缓存名称，值为缓存实现实例
     */
    public void setCaches(Map<String, CacheX> caches) {
        this.caches = caches;
    }

    /**
     * 获取缓存分组命中率统计组件
     *
     * @return 缓存分组命中率统计组件
     */
    public Metrics getHitting() {
        return metrics;
    }

    /**
     * 设置缓存分组命中率统计组件
     *
     * @param metrics 缓存分组命中率统计组件
     */
    public void setHitting(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 获取缓存全局开关状态
     *
     * @return 缓存全局开关状态
     */
    public EnumValue.Switch getCache() {
        return cache;
    }

    /**
     * 设置缓存全局开关状态
     *
     * @param cache 缓存全局开关状态
     */
    public void setCache(EnumValue.Switch cache) {
        this.cache = cache;
    }

    /**
     * 获取缓存防击穿开关状态
     *
     * @return 缓存防击穿开关状态
     */
    public EnumValue.Switch getPrevent() {
        return prevent;
    }

    /**
     * 设置缓存防击穿开关状态
     *
     * @param prevent 缓存防击穿开关状态
     */
    public void setPrevent(EnumValue.Switch prevent) {
        this.prevent = prevent;
    }

}
