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
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A high-performance hash map implementation for primitive {@code int} keys, based on open addressing with linear
 * probing. This class is designed to be a more memory and performance-efficient alternative to
 * {@code java.util.HashMap<Integer, V>} by avoiding the overhead of key boxing/unboxing.
 * <p>
 * This implementation allows {@code null} values. It does not guarantee the order of iteration.
 *
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class IntHashMap<V> implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852273966013L;

    /**
     * The default initial capacity of the hash map.
     */
    private static final int DEFAULT_CAPACITY = 32;
    /**
     * The minimum capacity for the hash map.
     */
    private static final int MINIMUM_CAPACITY = 4;
    /**
     * The maximum capacity for the hash map.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Represents a free (empty) slot in the hash table.
     */
    private static final byte FREE = 0;
    /**
     * Represents a full (occupied) slot in the hash table.
     */
    private static final byte FULL = 1;
    /**
     * Represents a removed slot in the hash table.
     */
    private static final byte REMOVED = -1;

    /**
     * The array storing the keys.
     */
    private transient int[] keys;
    /**
     * The array storing the values.
     */
    private transient Object[] values;
    /**
     * The array storing the state of each slot (FREE, FULL, REMOVED).
     */
    private transient byte[] states;
    /**
     * The number of free slots available before resizing is needed.
     */
    private transient int free;
    /**
     * The number of key-value mappings in this map.
     */
    private transient int size;

    /**
     * Constructs an empty {@code IntHashMap} with the default initial capacity.
     */
    public IntHashMap() {
        init(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty {@code IntHashMap} with an initial capacity sized for the expected number of elements.
     *
     * @param expectedMaxSize The expected maximum number of elements this map will hold.
     */
    public IntHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);

        init(capacity(expectedMaxSize));
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return The number of key-value mappings.
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value for the specified key, or {@code null} if the key is not found. Note: A {@code null} return
     * value does not necessarily mean the key is absent; it could also mean the key is mapped to {@code null}. Use
     * {@link #containsKey(int)} to distinguish these cases.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value for the key, or {@code null} if the key is not found.
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
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key The key whose presence is to be tested.
     * @return {@code true} if this map contains a mapping for the key.
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
     * Associates the specified value with the specified key. If the map previously contained a mapping for the key, the
     * old value is replaced.
     *
     * @param key   The key with which the value is to be associated.
     * @param value The value to be associated with the key.
     * @return The previous value associated with the key, or {@code null} if there was no mapping for the key.
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

    /**
     * Trims the capacity of this map to be its current size. This can be used to minimize the map's storage.
     */
    public void trimToSize() {
        resize(capacity(size));
    }

    /**
     * Rehashes the contents of this map into a new table with the same capacity. This is useful for reclaiming space
     * left by a large number of removed entries.
     */
    public void rehash() {
        resize(keys.length);
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key The key whose mapping is to be removed.
     * @return The previous value associated with the key, or {@code null} if there was no mapping.
     */
    public V remove(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != FREE) {
            if (keys[i] == key) {
                if (states[i] < FREE)
                    return null; // Already removed

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
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(states, FREE);
        size = 0;
        free = keys.length >>> 1;
    }

    /**
     * Creates a shallow copy of this {@code IntHashMap}. The internal arrays are cloned, but the values themselves are
     * not.
     *
     * @return A shallow copy of this map.
     */
    @Override
    public Object clone() {
        try {
            IntHashMap<V> m = (IntHashMap<V>) super.clone();
            m.states = states.clone();
            m.keys = keys.clone();
            m.values = values.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            // This should not happen since we are Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * Iterates over the map and applies a visitor function to each key-value pair. This is a performance-oriented
     * alternative to creating an iterator.
     *
     * @param visitor The visitor function to apply.
     * @return {@code true} if the entire map was visited, or {@code false} if the visitor stopped the iteration early.
     */
    public boolean accept(Visitor<V> visitor) {
        for (int i = 0; i < states.length; i++)
            if (states[i] == FULL)
                if (!visitor.visit(keys[i], (V) values[i]))
                    return false;
        return true;
    }

    /**
     * Initializes the internal arrays with the specified capacity.
     *
     * @param initCapacity The initial capacity for the arrays.
     */
    private void init(int initCapacity) {
        keys = new int[initCapacity];
        values = new Object[initCapacity];
        states = new byte[initCapacity];
        free = initCapacity >>> 1;
    }

    /**
     * Calculates the appropriate power-of-two capacity for the hash table.
     *
     * @param expectedMaxSize The expected maximum number of elements.
     * @return The calculated capacity.
     */
    private int capacity(int expectedMaxSize) {
        int minCapacity = expectedMaxSize << 1;
        if (minCapacity > MAXIMUM_CAPACITY)
            return MAXIMUM_CAPACITY;

        int capacity = MINIMUM_CAPACITY;
        while (capacity < minCapacity)
            capacity <<= 1;

        return capacity;
    }

    /**
     * Resizes the hash table to a new length and rehashes all existing entries.
     *
     * @param newLength The new length for the internal arrays. Must be a power of 2.
     * @throws IllegalStateException if the new length exceeds {@link #MAXIMUM_CAPACITY}.
     */
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
            if (oldStates[j] > 0) { // Is FULL
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

    /**
     * Serializes this {@code IntHashMap} instance.
     *
     * @param s The {@link java.io.ObjectOutputStream} to write to.
     * @throws java.io.IOException if an I/O error occurs.
     */
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

    /**
     * Deserializes this {@code IntHashMap} instance.
     *
     * @param in The {@link java.io.ObjectInputStream} to read from.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the class of a serialized object could not be found.
     */
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

    /**
     * A functional interface for visiting key-value pairs in the {@code IntHashMap} efficiently.
     *
     * @param <V> The type of values in the map.
     */
    public interface Visitor<V> {

        /**
         * Visits a key-value pair.
         *
         * @param key   The integer key.
         * @param value The associated value.
         * @return {@code true} to continue iteration, {@code false} to stop.
         */
        boolean visit(int key, V value);
    }

}
