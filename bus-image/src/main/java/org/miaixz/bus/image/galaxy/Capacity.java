/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.logger.Logger;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Capacity<V> implements Cloneable, java.io.Serializable {

    static final boolean DISABLED = isFalse(Capacity.class.getName());
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

    public Capacity() {
        init(DEFAULT_CAPACITY);
    }

    public Capacity(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException(
                    "expectedMaxSize is negative: " + expectedMaxSize);

        init(capacity(expectedMaxSize));
    }

    private static boolean isFalse(String name) {
        try {
            String s = System.getProperty(name);
            return ((null != s) && s.equalsIgnoreCase("false"));
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return false;
    }

    public static String hostNameOf(InetAddress inetAddress) {
        if (DISABLED)
            return inetAddress.getHostAddress();

        String hostAddress = inetAddress.getHostAddress();
        Logger.debug("rDNS {} -> ...", hostAddress);
        long start = System.nanoTime();
        String hostName = inetAddress.getHostName();
        long end = System.nanoTime();
        Logger.debug("rDNS {} -> {} in {} ms", hostAddress, hostName, (end - start) / 1000);
        return hostName;
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

    private void init(int initCapacity) {
        keys = new int[initCapacity];
        values = new Object[initCapacity];
        states = new byte[initCapacity];
        free = initCapacity >>> 1;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

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

    public void rehash() {
        resize(keys.length);
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

    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(states, FREE);
        size = 0;
        free = keys.length >>> 1;
    }

    public Object clone() {
        try {
            Capacity<V> m = (Capacity<V>) super.clone();
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
            if (states[i] > FREE)
                if (!visitor.visit(keys[i], (V) values[i]))
                    return false;
        return true;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
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

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        int count = s.readInt();
        init(capacity(count));
        size = count;
        free -= count;

        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        int mask = keys.length - 1;

        while (count-- > 0) {
            int key = s.readInt();
            int i = key & mask;
            while (states[i] != FREE)
                i = (i + 1) & mask;
            states[i] = FULL;
            keys[i] = key;
            values[i] = s.readObject();
        }
    }

    public interface Visitor<V> {
        boolean visit(int key, V value);
    }

}
