/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.map;

import org.aoju.bus.core.lang.References;
import org.aoju.bus.core.lang.function.Func0;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 线程安全的ReferenceMap实现
 * 参考：jdk.management.resource.internal.WeakKeyConcurrentHashMap
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class ReferenceMap<K, V> implements ConcurrentMap<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    final ConcurrentMap<Reference<K>, V> raw;
    private final ReferenceQueue<K> lastQueue;
    private final References.Type keyType;
    /**
     * 回收监听
     */
    private BiConsumer<Reference<? extends K>, V> purgeListener;

    /**
     * 构造
     *
     * @param raw           {@link ConcurrentMap}实现
     * @param referenceType Reference类型
     */
    public ReferenceMap(ConcurrentMap<Reference<K>, V> raw, References.Type referenceType) {
        this.raw = raw;
        this.keyType = referenceType;
        lastQueue = new ReferenceQueue<>();
    }

    /**
     * 设置对象回收清除监听
     *
     * @param purgeListener 监听函数
     */
    public void setPurgeListener(BiConsumer<Reference<? extends K>, V> purgeListener) {
        this.purgeListener = purgeListener;
    }

    @Override
    public int size() {
        this.purgeStaleKeys();
        return this.raw.size();
    }

    @Override
    public boolean isEmpty() {
        return 0 == size();
    }

    @Override
    public V get(Object key) {
        this.purgeStaleKeys();
        return this.raw.get(ofKey((K) key, null));
    }

    @Override
    public boolean containsKey(Object key) {
        this.purgeStaleKeys();
        return this.raw.containsKey(ofKey((K) key, null));
    }

    @Override
    public boolean containsValue(Object value) {
        this.purgeStaleKeys();
        return this.raw.containsValue(value);
    }

    @Override
    public V put(K key, V value) {
        this.purgeStaleKeys();
        return this.raw.put(ofKey(key, this.lastQueue), value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        this.purgeStaleKeys();
        return this.raw.putIfAbsent(ofKey(key, this.lastQueue), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public V replace(K key, V value) {
        this.purgeStaleKeys();
        return this.raw.replace(ofKey(key, this.lastQueue), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        this.purgeStaleKeys();
        return this.raw.replace(ofKey(key, this.lastQueue), oldValue, newValue);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.purgeStaleKeys();
        this.raw.replaceAll((kWeakKey, value) -> function.apply(kWeakKey.get(), value));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        this.purgeStaleKeys();
        return this.raw.computeIfAbsent(ofKey(key, this.lastQueue), kWeakKey -> mappingFunction.apply(key));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.purgeStaleKeys();
        return this.raw.computeIfPresent(ofKey(key, this.lastQueue), (kWeakKey, value) -> remappingFunction.apply(key, value));
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回Func0回调产生的对象
     *
     * @param key      键
     * @param supplier 如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    public V computeIfAbsent(K key, Func0<? extends V> supplier) {
        return computeIfAbsent(key, (keyParam) -> supplier.callWithRuntimeException());
    }

    @Override
    public V remove(Object key) {
        this.purgeStaleKeys();
        return this.raw.remove(ofKey((K) key, null));
    }

    @Override
    public boolean remove(Object key, Object value) {
        this.purgeStaleKeys();
        return this.raw.remove(ofKey((K) key, null), value);
    }

    @Override
    public void clear() {
        this.raw.clear();
        while (lastQueue.poll() != null) ;
    }

    @Override
    public Set<K> keySet() {
        final Collection<K> trans = CollKit.trans(this.raw.keySet(), (reference) -> null == reference ? null : reference.get());
        return new HashSet<>(trans);
    }

    @Override
    public Collection<V> values() {
        this.purgeStaleKeys();
        return this.raw.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        this.purgeStaleKeys();
        return this.raw.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey().get(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.purgeStaleKeys();
        this.raw.forEach((key, value) -> action.accept(key.get(), value));
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entrySet().iterator();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.purgeStaleKeys();
        return this.raw.compute(ofKey(key, this.lastQueue), (kWeakKey, value) -> remappingFunction.apply(key, value));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        this.purgeStaleKeys();
        return this.raw.merge(ofKey(key, this.lastQueue), value, remappingFunction);
    }

    /**
     * 清除被回收的键
     */
    private void purgeStaleKeys() {
        Reference<? extends K> reference;
        V value;
        while ((reference = this.lastQueue.poll()) != null) {
            value = this.raw.remove(reference);
            if (null != purgeListener) {
                purgeListener.accept(reference, value);
            }
        }
    }

    /**
     * 根据Reference类型构建key对应的{@link Reference}
     *
     * @param key   键
     * @param queue {@link ReferenceQueue}
     * @return {@link Reference}
     */
    private Reference<K> ofKey(K key, ReferenceQueue<? super K> queue) {
        switch (keyType) {
            case WEAK:
                return new WeakKey<>(key, queue);
            case SOFT:
                return new SoftKey<>(key, queue);
        }
        throw new IllegalArgumentException("Unsupported key type: " + keyType);
    }

    /**
     * 弱键
     *
     * @param <K> 键类型
     */
    private static class WeakKey<K> extends WeakReference<K> {
        private final int hashCode;

        /**
         * 构造
         *
         * @param key   原始Key，不能为{@code null}
         * @param queue {@link ReferenceQueue}
         */
        WeakKey(K key, ReferenceQueue<? super K> queue) {
            super(key, queue);
            hashCode = key.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof WeakKey) {
                return ObjectKit.equals(((WeakKey<?>) other).get(), get());
            }
            return false;
        }
    }

    /**
     * 弱键
     *
     * @param <K> 键类型
     */
    private static class SoftKey<K> extends SoftReference<K> {
        private final int hashCode;

        /**
         * 构造
         *
         * @param key   原始Key，不能为{@code null}
         * @param queue {@link ReferenceQueue}
         */
        SoftKey(K key, ReferenceQueue<? super K> queue) {
            super(key, queue);
            hashCode = key.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof SoftKey) {
                return ObjectKit.equals(((SoftKey<?>) other).get(), get());
            }
            return false;
        }
    }

}
