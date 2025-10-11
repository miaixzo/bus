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
package org.miaixz.bus.core.lang.thread.threadlocal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A specialized variant of {@link ThreadLocal} that offers higher access performance when accessed from a
 * {@link SpecificThread}. {@link SpecificThread} uses a constant index in an array (instead of hash codes and hash
 * tables) to look up variables. While seemingly subtle, this provides a slight performance advantage over using hash
 * tables and is particularly useful when frequently accessed.
 *
 * @param <V> The type of the thread-local variable's value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastThreadLocal<V> {

    /**
     * The unique index assigned to this {@link FastThreadLocal} instance within the {@link ThreadLocalMap}.
     */
    private final int index;

    /**
     * Constructs a new {@code FastThreadLocal} instance. A unique index is assigned to this instance for efficient
     * lookup in {@link ThreadLocalMap}.
     */
    public FastThreadLocal() {
        this.index = ThreadLocalMap.nextVariableIndex();
    }

    /**
     * Returns the number of {@link FastThreadLocal} variables currently bound to the current thread.
     *
     * @return The count of bound {@link FastThreadLocal} variables.
     */
    public static int size() {
        ThreadLocalMap threadLocalMap = ThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return 0;
        } else {
            return threadLocalMap.size();
        }
    }

    /**
     * Removes all {@link FastThreadLocal} variables bound to the current thread. This operation is particularly useful
     * in container environments where thread-local variables should not be left behind in unmanaged threads (e.g., web
     * servers).
     */
    public static void removeAll() {
        // 1. Retrieve the ThreadLocalMap for the current thread. If null, there's nothing to remove.
        ThreadLocalMap threadLocalMap = ThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return;
        }

        try {
            // 2. Get the set of FastThreadLocal variables that need to be removed from the ThreadLocalMap.
            // This set is stored at a special index (VARIABLES_TO_REMOVE_INDEX) within the ThreadLocalMap.
            Object v = threadLocalMap.indexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX);
            if (v != null && v != ThreadLocalMap.UNSET) {
                Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
                // Convert the set to an array to avoid ConcurrentModificationException during iteration and removal.
                FastThreadLocal<?>[] variablesToRemoveArray = variablesToRemove
                        .toArray(new FastThreadLocal[variablesToRemove.size()]);
                for (FastThreadLocal<?> tlv : variablesToRemoveArray) {
                    tlv.remove(threadLocalMap);
                }
            }
        } finally {
            // 3. Finally, remove the entire ThreadLocalMap associated with the current thread.
            ThreadLocalMap.remove();
        }
    }

    /**
     * Destroys the data structure that holds all {@link FastThreadLocal} variables accessed from
     * non-{@link SpecificThread}s. This operation is useful in container environments where thread-local variables
     * should not be left behind in unmanaged threads. Call this method when your application is undeployed from a
     * container.
     */
    public static void destroy() {
        ThreadLocalMap.destroy();
    }

    /**
     * Adds the current {@link FastThreadLocal} instance to the set of variables to be removed within the
     * {@link ThreadLocalMap} at {@link ThreadLocalMap#VARIABLES_TO_REMOVE_INDEX}.
     *
     * @param threadLocalMap The {@link ThreadLocalMap} for the current thread.
     * @param variable       The {@link FastThreadLocal} instance to add.
     */
    private static void addToVariablesToRemove(ThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {
        // 1. Retrieve the Set of FastThreadLocal variables from the ThreadLocalMap. If it doesn't exist, create one.
        Object v = threadLocalMap.indexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX);
        Set<FastThreadLocal<?>> variablesToRemove;
        if (v == ThreadLocalMap.UNSET || v == null) {
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
            threadLocalMap.setIndexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX, variablesToRemove);
        } else {
            variablesToRemove = (Set<FastThreadLocal<?>>) v;
        }

        variablesToRemove.add(variable);
    }

    /**
     * Removes the specified {@link FastThreadLocal} instance from the set of variables to be removed within the
     * {@link ThreadLocalMap}.
     *
     * @param threadLocalMap The {@link ThreadLocalMap} for the current thread.
     * @param variable       The {@link FastThreadLocal} instance to remove.
     */
    private static void removeFromVariablesToRemove(ThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {
        Object v = threadLocalMap.indexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX);

        if (v == ThreadLocalMap.UNSET || v == null) {
            return;
        }

        Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
        variablesToRemove.remove(variable);
    }

    /**
     * Returns the value in the current thread's copy of this thread-local variable. If the variable has no value for
     * the current thread, it is first initialized to the value returned by an invocation of the {@link #initialValue()}
     * method.
     *
     * @return The current thread's value for this thread-local variable.
     */
    public final V get() {
        // 1. Get the ThreadLocalMap for the current thread.
        ThreadLocalMap threadLocalMap = ThreadLocalMap.get();
        // 2. Retrieve the value from the ThreadLocalMap at this FastThreadLocal's unique index.
        // If the value is not UNSET, it means it's a valid value, so return it directly.
        Object v = threadLocalMap.indexedVariable(index);
        if (v != ThreadLocalMap.UNSET) {
            return (V) v;
        }
        // 3. If indexedVariable[index] has no valid value, initialize it.
        return initialize(threadLocalMap);
    }

    /**
     * Sets the current thread's copy of this thread-local variable to the specified value. If the value is {@code null}
     * or {@link ThreadLocalMap#UNSET}, the variable is removed.
     *
     * @param value The value to be stored in the current thread's copy of this thread-local.
     */
    public final void set(V value) {
        // If the value is null or UNSET, it signifies a removal operation.
        if (value == null || value == ThreadLocalMap.UNSET) {
            remove();
        } else {
            // Otherwise, get the ThreadLocalMap and set the indexed variable.
            // If a new variable was created (i.e., it was previously UNSET), add it to the set of variables to remove.
            ThreadLocalMap threadLocalMap = ThreadLocalMap.get();
            if (threadLocalMap.setIndexedVariable(index, value)) {
                addToVariablesToRemove(threadLocalMap, this);
            }
        }
    }

    /**
     * Removes the current thread's value for this thread-local variable. The variable will be reinitialized by the next
     * {@link #get()} operation.
     */
    public final void remove() {
        remove(ThreadLocalMap.getIfSet());
    }

    /**
     * Removes the value for this thread-local variable from the specified {@link ThreadLocalMap}. Subsequent calls to
     * {@link #get()} will trigger an invocation of {@link #initialValue()}. The specified thread-local map must be for
     * the current thread.
     *
     * @param threadLocalMap The {@link ThreadLocalMap} from which to remove the variable.
     */
    public final void remove(ThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return;
        }
        // 1. Remove the value associated with this FastThreadLocal from the ThreadLocalMap.
        Object v = threadLocalMap.removeIndexedVariable(index);
        // 2. Remove this FastThreadLocal instance from the set of variables to remove within the ThreadLocalMap.
        removeFromVariablesToRemove(threadLocalMap, this);
        // 3. If a valid value was removed, invoke the onRemoval callback.
        if (v != ThreadLocalMap.UNSET) {
            try {
                onRemoval((V) v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the initial value for this thread-local variable. This method will be invoked the first time a thread
     * accesses the variable with {@link #get()}. Subclasses should override this method to provide their own initial
     * value.
     *
     * @return The initial value for this thread-local.
     */
    protected V initialValue() {
        return null;
    }

    /**
     * Callback method invoked when this thread-local variable is removed via {@link #remove()}. Subclasses can override
     * this method to perform cleanup or other actions when the variable is removed.
     *
     * @param value The value that was removed from the thread-local.
     */
    protected void onRemoval(V value) {

    }

    /**
     * Initializes the thread-local variable for the current thread by calling {@link #initialValue()} and storing the
     * result in the {@link ThreadLocalMap}.
     *
     * @param threadLocalMap The {@link ThreadLocalMap} for the current thread.
     * @return The initial value of the thread-local variable.
     */
    private V initialize(ThreadLocalMap threadLocalMap) {
        V v;
        try {
            // 1. Get the initial value from the subclass implementation.
            v = initialValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 2. Set the initial value in the ThreadLocalMap at this FastThreadLocal's index.
        threadLocalMap.setIndexedVariable(index, v);
        // 3. Add this FastThreadLocal instance to the set of variables to remove in the ThreadLocalMap.
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }

}
