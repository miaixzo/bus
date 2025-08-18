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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.miaixz.bus.cache.reader.AbstractReader;
import org.miaixz.bus.cache.reader.MultiCacheReader;
import org.miaixz.bus.cache.reader.SingleCacheReader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.logger.Logger;

/**
 * 缓存模块工厂类
 * <p>
 * 负责初始化和管理缓存相关的核心组件，包括Complex、SingleCacheReader和MultiCacheReader。 采用单例模式，通过ModuleHolder确保全局唯一实例，线程安全。
 * 提供静态的instance方法以初始化并返回Complex实例。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Module {
    /**
     * 缓存复合操作实例
     */
    private Complex complex;

    /**
     * 缓存上下文配置
     */
    private Context context;

    /**
     * 缓存映射集合，键为缓存名称，值为缓存实例
     */
    private Map<String, CacheX> caches;

    /**
     * 缓存命中率统计组件
     */
    private Metrics metrics;

    /**
     * 单键缓存读取器
     */
    private AbstractReader singleCacheReader;

    /**
     * 多键缓存读取器
     */
    private AbstractReader multiCacheReader;

    /**
     * 缓存管理器
     */
    private Manage manage;

    /**
     * 初始化状态标志，true表示已初始化，false表示未初始化
     */
    private boolean initialized;

    /**
     * 私有构造方法，防止外部实例化
     */
    private Module() {
        this.initialized = false;
    }

    /**
     * 获取Module工厂的单例实例
     * <p>
     * 使用ModuleHolder实现延迟加载的单例模式，确保线程安全。
     * </p>
     *
     * @return Module工厂实例
     */
    private static Module getInstance() {
        return ModuleHolder.INSTANCE;
    }

    /**
     * 初始化缓存工厂并返回Complex实例
     * <p>
     * 静态方法，确保只初始化一次，线程安全。如果已初始化，直接返回Complex实例。
     * </p>
     *
     * @param config 缓存配置对象
     * @return Complex缓存复合实例
     * @throws IllegalArgumentException 如果配置为空或缓存映射为空
     */
    public static synchronized Complex instance(Context config) {
        Module module = getInstance();
        if (!module.initialized) {
            module.initialize(config);
            module.initialized = true;
            Logger.info("Cache factory initialized successfully");
        }
        return module.complex;
    }

    /**
     * 初始化所有缓存组件
     * <p>
     * 包括缓存映射、Hitting、SingleCacheReader、MultiCacheReader和Complex实例。
     * </p>
     *
     * @param config 缓存配置对象
     * @throws IllegalArgumentException 如果配置为空或缓存映射为空
     */
    private void initialize(Context config) {
        Assert.isTrue(null != config, "context param can not be null.");
        Assert.isTrue(CollKit.isNotEmpty(config.getCaches()), "caches param can not be empty.");

        this.context = config;
        // 初始化缓存映射
        caches = Collections.unmodifiableMap(new HashMap<>(config.getCaches()));
        Logger.debug("Initialized caches with size: {}", caches.size());

        // 初始化 Hitting
        metrics = config.getHitting();
        Logger.debug("Initialized hitting: {}", metrics != null ? metrics.getClass().getSimpleName() : "null");

        // 初始化 Reader 实例
        singleCacheReader = new SingleCacheReader();
        multiCacheReader = new MultiCacheReader();
        Logger.debug("Initialized singleCacheReader and multiCacheReader");

        // 初始化 Manage 实例
        manage = new Manage(caches, metrics);
        Logger.debug("Initialized manage");

        // 创建 Complex 实例
        complex = new Complex();
        // 设置 Complex 实例的依赖
        complex.setContext(context);
        complex.setManage(manage);
        complex.setSingleCacheReader(singleCacheReader);
        complex.setMultiCacheReader(multiCacheReader);
        Logger.debug("Complex instance created and dependencies set");
    }

    /**
     * 获取指定名称的缓存实例
     *
     * @param name 缓存名称
     * @return 缓存实例
     * @throws IllegalStateException 如果缓存工厂未初始化
     */
    public CacheX getCache(String name) {
        if (caches == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        CacheX cache = caches.get(name);
        Logger.debug("Retrieved cache: {} for name: {}", cache != null ? cache.getClass().getSimpleName() : "null",
                name);
        return cache;
    }

    /**
     * 获取Hitting实例
     *
     * @return Hitting实例的Optional包装
     */
    public Optional<Metrics> getHitting() {
        Logger.debug("Retrieved hitting: {}", metrics != null ? metrics.getClass().getSimpleName() : "null");
        return Optional.ofNullable(metrics);
    }

    /**
     * 获取单缓存读取器
     *
     * @return 单缓存读取器实例
     * @throws IllegalStateException 如果缓存工厂未初始化
     */
    public AbstractReader getSingleCacheReader() {
        if (singleCacheReader == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        Logger.debug("Retrieved singleCacheReader");
        return singleCacheReader;
    }

    /**
     * 获取多缓存读取器
     *
     * @return 多缓存读取器实例
     * @throws IllegalStateException 如果缓存工厂未初始化
     */
    public AbstractReader getMultiCacheReader() {
        if (multiCacheReader == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        Logger.debug("Retrieved multiCacheReader");
        return multiCacheReader;
    }

    /**
     * 单例持有者类，用于延迟加载Module实例
     * <p>
     * 使用静态内部类实现线程安全的延迟初始化单例模式
     * </p>
     */
    private static class ModuleHolder {
        /**
         * Module单例实例
         */
        private static final Module INSTANCE = new Module();
    }

}