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
package org.miaixz.bus.vortex.registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Monitor;
import org.miaixz.bus.vortex.Registry;
import org.miaixz.bus.vortex.cache.CacheManager;
import org.springframework.beans.factory.InitializingBean;

import reactor.core.publisher.Mono;

/**
 * An abstract, generic, thread-safe base class for creating in-memory registries.
 * <p>
 * This class provides the core functionality for a key-value store, using a {@link ConcurrentHashMap} for thread-safe
 * operations. It is designed to be extended by concrete registry implementations (e.g., {@link AssetsRegistry}), which
 * must provide a key generation strategy.
 * <p>
 * <b>性能优化（二级缓存架构）：</b>
 * </p>
 * <ul>
 * <li>使用 {@link CacheManager} 管理二级缓存</li>
 * <li>一级缓存：ConcurrentHashMap（热数据，极快访问）</li>
 * <li>二级缓存：Caffeine（全量数据，LRU淘汰）</li>
 * <li>查询顺序：一级 → 二级 → null</li>
 * <li>更新策略：写入时同时更新两级缓存</li>
 * </ul>
 * <p>
 * <b>职责划分：</b>
 * </p>
 * <ul>
 * <li>AbstractRegistry：注册表业务逻辑</li>
 * <li>CacheManager：通用缓存管理（可被其他组件复用）</li>
 * </ul>
 *
 * @param <T> The type of objects to be stored in the registry.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    /**
     * The underlying thread-safe map that stores the registered items.
     * <p>
     * 一级缓存：ConcurrentHashMap，提供最快速的访问
     * </p>
     */
    private final Map<String, T> registry = new ConcurrentHashMap<>();

    /**
     * 缓存管理器：封装二级缓存逻辑
     */
    protected final CacheManager<String, T> cacheManager;

    /**
     * The function used to generate a unique key for each item stored in the registry.
     */
    protected Function<T, String> keyGenerator;

    /**
     * 构造函数：初始化缓存管理器
     */
    public AbstractRegistry() {
        this.cacheManager = new CacheManager<>();
        Logger.debug("AbstractRegistry初始化: 使用CacheManager管理二级缓存");
    }

    /**
     * Sets the key generation strategy for this registry. This method must be called by subclasses in their constructor
     * to define how items are indexed.
     *
     * @param keyGenerator A {@link Function} that takes an item of type {@code T} and returns its unique string key.
     */
    protected void setKeyGenerator(Function<T, String> keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    /**
     * 设置性能监控器（可选）
     * <p>
     * 委托给 CacheManager
     * </p>
     *
     * @param monitor 性能监控器
     */
    public void setMonitor(Monitor monitor) {
        this.cacheManager.setPerformanceMonitor(monitor);
        Logger.debug("性能监控器已设置: {}", monitor.getClass().getSimpleName());
    }

    @Override
    public void register(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set. Call setKeyGenerator in the constructor.");
        }
        register(keyGenerator.apply(item), item);
    }

    @Override
    public void register(String key, T item) {
        // 同时更新 registry 和 cacheManager
        this.registry.put(key, item);
        this.cacheManager.put(key, item);
    }

    @Override
    public void destroy(String key) {
        // 同时从 registry 和 cacheManager 移除
        this.registry.remove(key);
        this.cacheManager.remove(key);
    }

    @Override
    public void destroy(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        destroy(keyGenerator.apply(item));
    }

    @Override
    public void update(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        update(keyGenerator.apply(item), item);
    }

    @Override
    public void update(String key, T item) {
        // 更新就是重新注册
        register(key, item);
    }

    /**
     * Asynchronously clears the registry and then calls the asynchronous {@link #init()} method.
     * <p>
     * 同时清空 registry 和 cacheManager
     * </p>
     *
     * @return A {@code Mono<Void>} that completes when the registry is cleared and re-initialized.
     */
    @Override
    public Mono<Void> refresh() {
        return Mono.fromRunnable(() -> {
            // 清空 registry 和 cacheManager
            this.registry.clear();
            this.cacheManager.clear();
        }).then(init()); // Chain to the async init() method
    }

    @Override
    public T get(String key) {
        // 1. 先查询 registry（一级缓存）
        T item = this.registry.get(key);
        if (item != null) {
            return item;
        }

        // 2. registry 未命中，查询 cacheManager（二级缓存）
        item = this.cacheManager.get(key);
        if (item != null) {
            // 二级缓存命中，同步到 registry
            this.registry.put(key, item);
            return item;
        }

        // 3. 完全未命中
        return null;
    }

    @Override
    public Collection<T> getAll() {
        return this.registry.values();
    }

    /**
     * Integrates with the Spring lifecycle. After all bean properties are set, this method is called, which in turn
     * triggers the initial {@link #refresh()} of the registry.
     * <p>
     * This method will **block** the startup thread until the asynchronous {@code refresh()} completes. This is
     * acceptable behavior during application startup to ensure the registry is populated before use.
     */
    @Override
    public void afterPropertiesSet() {
        // Bridge the synchronous Spring lifecycle with our async refresh.
        // This is acceptable on startup but should be avoided at runtime.
        refresh().block();
    }

    /**
     * 获取缓存统计信息
     * <p>
     * 委托给 CacheManager
     * </p>
     *
     * @return 统计信息
     */
    public Object getStats() {
        return this.cacheManager.getStats();
    }

}
