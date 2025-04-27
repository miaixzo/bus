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
package org.miaixz.bus.core.center.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * 基于哈希表的{@code IntMap}接口实现。这个实现提供了所有可选的map操作，并允许{@code null}值。 {@link IntHashMap}
 * 类大致相当于{@link java.util.HashMap}，除了它使用{@code int}作为它的键。 这个类不保证映射的顺序;特别是，它不能保证顺序在一段时间内保持不变。
 *
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class IntHashMap<V> implements Cloneable, Serializable {

    private static final long serialVersionUID = -1L;

    private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final byte FREE = 0;
    private static final byte FULL = 1;
    private static final byte REMOVED = -1;

    private transient int[] keys;
    private transient Object[] values;
    private transient byte[] states;
    private transient int free;
    private transient int size;

    /**
     * 构造
     */
    public IntHashMap() {
        init(DEFAULT_CAPACITY);
    }

    /**
     * 构造
     *
     * @param expectedMaxSize HashMap 的初始容量
     */
    public IntHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);

        init(capacity(expectedMaxSize));
    }

    /**
     * @return 返回此映射中的键值映射的数量
     */
    public int size() {
        return size;
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回此映射映射到指定键的值。 如果映射不包含此键的映射，则返回{@code null}。 返回值{@code null}并不一定表示映射中不包含该键的映射;也有可能映射显式地将键映射到{@code null}。
     * {@code containsKey}操作可以用来区分这两种情况。
     *
     * @param key 要返回其关联值的键
     * @return 此映射映射到指定键的值
     */
    public V get(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != FREE) {
            if (keys[i] == key)
                return (V) values[i];
            i = (i + 1) & mask;
        }
        return null;
    }

    /**
     * 如果此映射包含指定键的映射，则返回{@code true}。
     *
     * @param key 判断此 Map 中是否存在的键
     */
    public boolean containsKey(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != FREE) {
            if (keys[i] == key)
                return states[i] > FREE;
            i = (i + 1) & mask;
        }
        return false;
    }

    /**
     * 将指定值与此映射中的指定键关联。如果此映射之前包含此键的映射，则旧值将被替换。
     *
     * @param key   与指定值关联的键
     * @param value 与指定键关联的值
     * @return 与指定键关联的先前值，或如果键没有映射则返回 {@code null}。 返回 {@code null} 还可以指示 HashMap 先前将 {@code null} 与指定键关联。
     */
    public V put(int key, V value) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;

        while (states[i] > FREE) {
            if (keys[i] == key) {
                V oldValue = (V) values[i];
                values[i] = value;
                return oldValue;
            }
            i = (i + 1) & mask;
        }
        byte oldState = states[i];
        states[i] = FULL;
        keys[i] = key;
        values[i] = value;
        ++size;
        if (oldState == FREE && --free < 0)
            resize(Math.max(capacity(size), keys.length));
        return null;
    }

    public void trimToSize() {
        resize(capacity(size));
    }

    /**
     * 将此映射的内容重新哈希到具有更大容量的新 {@code HashMap} 实例中。 当此映射中的键数超出其容量和加载因子时，会自动调用此方法
     */
    public void rehash() {
        resize(keys.length);
    }

    /**
     * 如果存在，则从此映射中删除此键的映射
     *
     * @param key 要从映射中删除其映射的键。
     * @return 与指定键关联的上一个值，如果键没有映射，则返回 {@code null}。 返回 {@code null} 还可以指示映射先前将 {@code null} 与指定键关联。
     */
    public V remove(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != FREE) {
            if (keys[i] == key) {
                if (states[i] < FREE)
                    return null;

                states[i] = REMOVED;
                V oldValue = (V) values[i];
                values[i] = null;
                size--;
                return oldValue;
            }
            i = (i + 1) & mask;
        }
        return null;
    }

    /**
     * 从此映射中删除所有映射
     */
    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(states, FREE);
        size = 0;
        free = keys.length >>> 1;
    }

    public Object clone() {
        try {
            IntHashMap<V> m = (IntHashMap<V>) super.clone();
            m.states = states.clone();
            m.keys = keys.clone();
            m.values = values.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public boolean accept(Visitor<V> visitor) {
        for (int i = 0; i < states.length; i++)
            if (states[i] > FREE) // states[i] == FULL
                if (!visitor.visit(keys[i], (V) values[i]))
                    return false;
        return true;
    }

    private void init(int initCapacity) {
        keys = new int[initCapacity];
        values = new Object[initCapacity];
        states = new byte[initCapacity];
        free = initCapacity >>> 1;
    }

    private int capacity(int expectedMaxSize) {
        int minCapacity = expectedMaxSize << 1;
        if (minCapacity > MAXIMUM_CAPACITY)
            return MAXIMUM_CAPACITY;

        int capacity = MINIMUM_CAPACITY;
        while (capacity < minCapacity)
            capacity <<= 1;

        return capacity;
    }

    private void resize(int newLength) {
        if (newLength > MAXIMUM_CAPACITY)
            throw new IllegalStateException("Capacity exhausted.");

        int[] oldKeys = keys;
        Object[] oldValues = values;
        byte[] oldStates = states;
        int[] newKeys = new int[newLength];
        Object[] newValues = new Object[newLength];
        byte[] newStates = new byte[newLength];
        int mask = newLength - 1;

        for (int j = 0; j < oldKeys.length; j++) {
            if (oldStates[j] > 0) {
                int key = oldKeys[j];
                int i = key & mask;
                while (newStates[i] != FREE)
                    i = (i + 1) & mask;
                newStates[i] = FULL;
                newKeys[i] = key;
                newValues[i] = oldValues[j];
                oldValues[j] = null;
            }
        }
        keys = newKeys;
        values = newValues;
        states = newStates;
        free = (newLength >>> 1) - size;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();

        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        s.writeInt(size);
        for (int i = 0; i < states.length; i++) {
            if (states[i] > FREE) {
                s.writeInt(keys[i]);
                s.writeObject(values[i]);
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        int count = in.readInt();
        init(capacity(count));
        size = count;
        free -= count;

        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        int mask = keys.length - 1;

        while (count-- > 0) {
            int key = in.readInt();
            int i = key & mask;
            while (states[i] != FREE)
                i = (i + 1) & mask;
            states[i] = FULL;
            keys[i] = key;
            values[i] = in.readObject();
        }
    }

    public interface Visitor<V> {
        boolean visit(int key, V value);
    }

}
