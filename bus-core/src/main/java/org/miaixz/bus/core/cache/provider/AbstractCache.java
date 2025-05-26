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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.cache.CacheListener;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * 超时和限制大小的缓存的默认实现 继承此抽象缓存需要：
 * <ul>
 * <li>创建一个新的Map</li>
 * <li>实现 {@code prune} 策略</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852292318535L;

    /**
     * 写的时候每个key一把锁，降低锁的粒度
     */
    protected final Map<K, Lock> keyLockMap = new ConcurrentHashMap<>();
    /**
     * Map缓存
     */
    protected Map<Mutable<K>, CacheObject<K, V>> cacheMap;
    /**
     * 返回缓存容量，{@code 0}表示无大小限制
     */
    protected int capacity;
    /**
     * 缓存失效时长， {@code 0} 表示无限制，单位毫秒
     */
    protected long timeout;

    /**
     * 每个对象是否有单独的失效时长，用于决定清理过期对象是否有必要。
     */
    protected boolean existCustomTimeout;

    /**
     * 命中数，即命中缓存计数
     */
    protected LongAdder hitCount = new LongAdder();
    /**
     * 丢失数，即未命中缓存计数
     */
    protected LongAdder missCount = new LongAdder();

    /**
     * 缓存监听
     */
    protected CacheListener<K, V> listener;

    @Override
    public void put(final K key, final V object) {
        put(key, object, timeout);
    }

    /**
     * 加入元素，无锁
     *
     * @param key     键
     * @param object  值
     * @param timeout 超时时长
     */
    protected void putWithoutLock(final K key, final V object, final long timeout) {
        final CacheObject<K, V> co = new CacheObject<>(key, object, timeout);
        if (timeout != 0) {
            existCustomTimeout = true;
        }
        final MutableObject<K> mKey = MutableObject.of(key);

        // 对于替换的键值对，不做满队列检查和清除
        if (cacheMap.containsKey(mKey)) {
            // 存在相同key，覆盖之
            cacheMap.put(mKey, co);
        } else {
            if (isFull()) {
                pruneCache();
            }
            cacheMap.put(mKey, co);
        }
    }

    /**
     * 获取命中数
     *
     * @return 命中数
     */
    public long getHitCount() {
        return hitCount.sum();
    }

    /**
     * 获取丢失数
     *
     * @return 丢失数
     */
    public long getMissCount() {
        return missCount.sum();
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final SupplierX<V> supplier) {
        return get(key, isUpdateLastAccess, this.timeout, supplier);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);
        if (null == v && null != supplier) {
            // 每个key单独获取一把锁，降低锁的粒度提高并发能力
            final Lock keyLock = keyLockMap.computeIfAbsent(key, k -> new ReentrantLock());
            keyLock.lock();
            try {
                // 双重检查锁，防止在竞争锁的过程中已经有其它线程写入
                // 由于这个方法内的加锁是get独立锁，不和put锁互斥，而put和pruneCache会修改cacheMap，导致在pruneCache过程中get会有并发问题
                // 因此此处需要使用带全局锁的get获取值
                v = get(key, isUpdateLastAccess);
                if (null == v) {
                    // supplier的创建是一个耗时过程，此处创建与全局锁无关，而与key锁相关，这样就保证每个key只创建一个value，且互斥
                    v = supplier.get();
                    put(key, v, timeout);
                }
            } finally {
                keyLock.unlock();
                keyLockMap.remove(key);
            }
        }
        return v;
    }

    /**
     * 获取键对应的{@link CacheObject}
     *
     * @param key 键，实际使用时会被包装为{@link MutableObject}
     * @return {@link CacheObject}
     */
    protected CacheObject<K, V> getWithoutLock(final K key) {
        return this.cacheMap.get(MutableObject.of(key));
    }

    @Override
    public Iterator<V> iterator() {
        final CacheObjectIterator<K, V> copiedIterator = (CacheObjectIterator<K, V>) this.cacheObjIterator();
        return new CacheValuesIterator<>(copiedIterator);
    }

    /**
     * 清理实现 子类实现此方法时无需加锁
     *
     * @return 清理数
     */
    protected abstract int pruneCache();

    @Override
    public int capacity() {
        return capacity;
    }

    /**
     * @return 默认缓存失效时长。 每个对象可以单独设置失效时长
     */
    @Override
    public long timeout() {
        return timeout;
    }

    /**
     * 只有设置公共缓存失效时长或每个对象单独的失效时长时清理可用
     *
     * @return 过期对象清理是否可用，内部使用
     */
    protected boolean isPruneExpiredActive() {
        return (timeout != 0) || existCustomTimeout;
    }

    @Override
    public boolean isFull() {
        return (capacity > 0) && (cacheMap.size() >= capacity);
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    @Override
    public String toString() {
        return this.cacheMap.toString();
    }

    /**
     * 设置监听
     *
     * @param listener 监听
     * @return this
     */
    @Override
    public AbstractCache<K, V> setListener(final CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 返回所有键
     *
     * @return 所有键
     */
    public Set<K> keySet() {
        return this.cacheMap.keySet().stream().map(Mutable::get).collect(Collectors.toSet());
    }

    /**
     * 对象移除回调。默认无动作 子类可重写此方法用于监听移除事件，如果重写，listener将无效
     *
     * @param key          键
     * @param cachedObject 被缓存的对象
     */
    protected void onRemove(final K key, final V cachedObject) {
        final CacheListener<K, V> listener = this.listener;
        if (null != listener) {
            listener.onRemove(key, cachedObject);
        }
    }

    /**
     * 移除key对应的对象，不加锁
     *
     * @param key 键
     * @return 移除的对象，无返回null
     */
    protected CacheObject<K, V> removeWithoutLock(final K key) {
        return cacheMap.remove(MutableObject.of(key));
    }

    /**
     * 获取所有{@link CacheObject}值的{@link Iterator}形式
     *
     * @return {@link Iterator}
     */
    protected Iterator<CacheObject<K, V>> cacheObjIter() {
        return this.cacheMap.values().iterator();
    }

}
