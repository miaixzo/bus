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
package org.miaixz.bus.core.lang.thread.threadlocal;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Normal;

/**
 * An internal data structure that stores all {@link ThreadLocal} variables for a {@link SpecificThread}. This class is
 * intended for internal use only. Unless you know exactly what you are doing, please use {@link SpecificThread}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ThreadLocalMap {

    /**
     * A placeholder value indicating an unset or invalid value. This is used instead of {@code null} because
     * {@code null} can be a valid value for a {@link FastThreadLocal} (e.g., if {@code initialValue()} returns
     * {@code null}).
     */
    public static final Object UNSET = new Object();
    /**
     * A {@link ThreadLocal} used to store {@link ThreadLocalMap} instances for regular {@link Thread}s that are not
     * {@link SpecificThread}s.
     */
    private static final ThreadLocal<ThreadLocalMap> SLOW_THREAD_LOCAL_MAP = new ThreadLocal<>();
    /**
     * An atomic integer used to generate unique indices for {@link FastThreadLocal} variables.
     */
    private static final AtomicInteger NEXT_INDEX = new AtomicInteger();
    /**
     * The index reserved for storing a {@link Set} of {@link FastThreadLocal} variables that need to be removed.
     */
    public static final int VARIABLES_TO_REMOVE_INDEX = nextVariableIndex();
    /**
     * An array to store the values of {@link FastThreadLocal} variables. The index in this array corresponds to the
     * unique index assigned to each {@link FastThreadLocal}.
     */
    private Object[] indexedVariables;
    /**
     * A {@link BitSet} used to track which {@link FastThreadLocal} variables have initiated a cleanup thread for this
     * {@code ThreadLocalMap}. Setting a bit to {@code true} at a specific index indicates that a cleanup thread has
     * been started for the {@link FastThreadLocal} corresponding to that index.
     */
    private BitSet cleanerFlags;

    /**
     * Constructs a new {@code ThreadLocalMap}. Initializes the {@code indexedVariables} array with a default capacity
     * and fills it with the {@link #UNSET} placeholder.
     */
    private ThreadLocalMap() {
        indexedVariables = newIndexedVariableTable();
    }

    /**
     * Retrieves the {@code ThreadLocalMap} instance for the current thread. If the current thread is a
     * {@link SpecificThread}, it uses optimized access. Otherwise, it uses a regular {@link ThreadLocal} for storage.
     *
     * @return The {@code ThreadLocalMap} instance for the current thread.
     */
    public static ThreadLocalMap get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SpecificThread) {
            return fastGet((SpecificThread) thread);
        } else {
            return slowGet();
        }
    }

    /**
     * Retrieves the {@code ThreadLocalMap} instance for a {@link SpecificThread}. If the map does not exist, a new one
     * is created and associated with the thread.
     *
     * @param thread The {@link SpecificThread} to get the map for.
     * @return The {@code ThreadLocalMap} instance for the given thread.
     */
    private static ThreadLocalMap fastGet(SpecificThread thread) {
        ThreadLocalMap threadLocalMap = thread.getThreadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new ThreadLocalMap());
        }
        return threadLocalMap;
    }

    /**
     * Retrieves the {@code ThreadLocalMap} instance for a regular {@link Thread}. If the map does not exist, a new one
     * is created and stored in a {@link ThreadLocal}.
     *
     * @return The {@code ThreadLocalMap} instance for the current thread.
     */
    private static ThreadLocalMap slowGet() {
        ThreadLocalMap ret = SLOW_THREAD_LOCAL_MAP.get();
        if (ret == null) {
            ret = new ThreadLocalMap();
            SLOW_THREAD_LOCAL_MAP.set(ret);
        }
        return ret;
    }

    /**
     * Retrieves the {@code ThreadLocalMap} instance for the current thread if it has been set. This method returns
     * {@code null} if no {@code ThreadLocalMap} is associated with the current thread.
     *
     * @return The {@code ThreadLocalMap} instance for the current thread, or {@code null} if not set.
     */
    public static ThreadLocalMap getIfSet() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SpecificThread) {
            return ((SpecificThread) thread).getThreadLocalMap();
        }
        return SLOW_THREAD_LOCAL_MAP.get();
    }

    /**
     * Removes the {@code ThreadLocalMap} associated with the current thread. This effectively clears all
     * {@link FastThreadLocal} variables for the current thread.
     */
    public static void remove() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SpecificThread) {
            ((SpecificThread) thread).setThreadLocalMap(null);
        } else {
            SLOW_THREAD_LOCAL_MAP.remove();
        }
    }

    /**
     * Destroys the {@code ThreadLocalMap} associated with the current thread. This is an alias for {@link #remove()}.
     */
    public static void destroy() {
        SLOW_THREAD_LOCAL_MAP.remove();
    }

    /**
     * Generates and returns the next unique index for a {@link FastThreadLocal} variable.
     *
     * @return The next available unique index.
     * @throws IllegalStateException if too many thread-local indexed variables have been created.
     */
    public static int nextVariableIndex() {
        int index = NEXT_INDEX.getAndIncrement();
        if (index < 0) {
            NEXT_INDEX.decrementAndGet();
            throw new IllegalStateException("too many thread-local indexed variables");
        }
        return index;
    }

    /**
     * Creates a new array for indexed variables, initialized with {@link #UNSET} values.
     *
     * @return A new {@code Object[]} array for storing indexed variables.
     */
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[Normal._32];
        Arrays.fill(array, UNSET);
        return array;
    }

    /**
     * Returns the number of active {@link FastThreadLocal} variables currently set in this map.
     *
     * @return The count of set variables.
     */
    public int size() {
        int count = 0;
        Object v = indexedVariable(VARIABLES_TO_REMOVE_INDEX);
        if (v != null && v != UNSET) {
            Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
            count += variablesToRemove.size();
        }
        return count;
    }

    /**
     * Checks if an indexed variable at the given index is set (i.e., not {@link #UNSET}).
     *
     * @param index The index of the variable to check.
     * @return {@code true} if the variable at the given index is set; {@code false} otherwise.
     */
    public boolean isIndexedVariableSet(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length && lookup[index] != UNSET;
    }

    /**
     * Retrieves the value of the indexed variable at the specified index.
     *
     * @param index The index of the variable to retrieve.
     * @return The value of the variable, or {@link #UNSET} if the index is out of bounds or the variable is not set.
     */
    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length ? lookup[index] : UNSET;
    }

    /**
     * Sets the value of the indexed variable at the specified index. If the index is out of bounds, the internal array
     * is expanded.
     *
     * @param index The index of the variable to set.
     * @param value The new value for the variable.
     * @return {@code true} if a new thread-local variable was created (i.e., the old value was {@link #UNSET});
     *         {@code false} otherwise.
     */
    public boolean setIndexedVariable(int index, Object value) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object oldValue = lookup[index];
            lookup[index] = value;
            return oldValue == UNSET;
        } else {
            expandIndexedVariableTableAndSet(index, value);
            return true;
        }
    }

    /**
     * Removes the indexed variable at the specified index by setting its value to {@link #UNSET}.
     *
     * @param index The index of the variable to remove.
     * @return The old value of the variable, or {@link #UNSET} if the index is out of bounds or the variable was not
     *         set.
     */
    public Object removeIndexedVariable(int index) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object v = lookup[index];
            lookup[index] = UNSET;
            return v;
        } else {
            return UNSET;
        }
    }

    /**
     * Sets the bit at the specified index in the {@code cleanerFlags} {@link BitSet} to {@code true}. This indicates
     * that a cleanup thread has been initiated for the {@link FastThreadLocal} at this index.
     *
     * @param index The index of the {@link FastThreadLocal} for which to set the cleaner flag.
     */
    public void setCleanerFlags(int index) {
        if (cleanerFlags == null) {
            cleanerFlags = new BitSet();
        }
        cleanerFlags.set(index);
    }

    /**
     * Checks if the cleaner flag is set for the {@link FastThreadLocal} at the specified index.
     *
     * @param index The index of the {@link FastThreadLocal} to check.
     * @return {@code true} if the cleaner flag is set; {@code false} otherwise.
     */
    public boolean isCleanerFlags(int index) {
        return cleanerFlags != null && cleanerFlags.get(index);
    }

    /**
     * Expands the {@code indexedVariables} array to accommodate a new variable at the given index and sets the new
     * value. The array capacity is expanded to the next power of two that can hold the index.
     *
     * @param index The index of the new variable.
     * @param value The value to set at the new index.
     */
    private void expandIndexedVariableTableAndSet(int index, Object value) {
        Object[] oldArray = indexedVariables;
        final int oldCapacity = oldArray.length;
        int newCapacity = index;
        newCapacity |= newCapacity >>> 1;
        newCapacity |= newCapacity >>> 2;
        newCapacity |= newCapacity >>> 4;
        newCapacity |= newCapacity >>> 8;
        newCapacity |= newCapacity >>> 16;
        newCapacity++;

        // Create a new array and copy elements from the old array to the new array.
        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        // Initialize the newly expanded part of the array with UNSET.
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        // Set the variable at the specified index.
        newArray[index] = value;
        // Update the indexedVariables reference to the new array.
        indexedVariables = newArray;
    }

}
