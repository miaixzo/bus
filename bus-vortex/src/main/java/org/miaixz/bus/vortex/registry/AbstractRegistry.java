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
package org.miaixz.bus.vortex.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.vortex.Registry;
import org.springframework.beans.factory.InitializingBean;

/**
 * 抽象注册类，提供通用的注册表功能，用于管理和存储键值对数据
 *
 * @param <T> 注册表中存储的值的类型
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    /**
     * 线程安全的缓存，用于存储键值对数据
     */
    private final Map<String, T> cache = new ConcurrentHashMap<>();

    /**
     * 键生成策略
     */
    protected RegistryKey<T> registryKey;

    /**
     * 设置键生成策略
     *
     * @param registryKey 键生成策略
     */
    protected void setKeyGenerator(RegistryKey<T> registryKey) {
        this.registryKey = registryKey;
    }

    /**
     * 初始化注册表，子类需实现具体初始化逻辑
     */
    public abstract void init();

    /**
     * 添加键值对到注册表
     *
     * @param key 键
     * @param reg 值
     * @return 如果键不存在且添加成功返回 true，否则返回 false
     */
    @Override
    public boolean add(String key, T reg) {
        if (null != cache.get(key)) {
            return false;
        }
        cache.put(key, reg);
        return true;
    }

    /**
     * 添加对象到注册表，使用键生成策略生成键
     *
     * @param item 要添加的对象
     * @return 如果添加成功返回 true，否则返回 false
     */
    public boolean add(T item) {
        if (registryKey == null) {
            throw new IllegalStateException("Key generator not set");
        }
        return add(registryKey.keys(item), item);
    }

    /**
     * 从注册表中移除指定键的记录
     *
     * @param id 键
     * @return 如果移除成功返回 true，否则返回 false
     */
    public boolean remove(String id) {
        return null != this.cache.remove(id);
    }

    /**
     * 更新注册表中的键值对，先移除后添加
     *
     * @param key 键
     * @param reg 新值
     * @return 如果更新成功返回 true，否则返回 false
     */
    public boolean amend(String key, T reg) {
        cache.remove(key);
        return add(key, reg);
    }

    /**
     * 更新注册表中的对象，使用键生成策略生成键
     *
     * @param item 要更新的对象
     * @return 如果更新成功返回 true，否则返回 false
     */
    public boolean amend(T item) {
        if (registryKey == null) {
            throw new IllegalStateException("Key generator not set");
        }
        return amend(registryKey.keys(item), item);
    }

    /**
     * 刷新注册表，清空缓存并重新初始化
     */
    public void refresh() {
        cache.clear();
        init();
    }

    /**
     * 获取指定键对应的值
     *
     * @param key 键
     * @return 对应的值，若不存在返回 null
     */
    public T get(String key) {
        return cache.get(key);
    }

    /**
     * Spring 初始化回调，在 bean 属性设置后调用，触发注册表刷新
     */
    @Override
    public void afterPropertiesSet() {
        refresh();
    }

    /**
     * 键生成策略接口
     *
     * @param <T> 对象类型
     */
    @FunctionalInterface
    public interface RegistryKey<T> {

        /**
         * 根据对象生成键
         *
         * @param item 对象
         * @return 生成的键
         */
        String keys(T item);
    }

}
