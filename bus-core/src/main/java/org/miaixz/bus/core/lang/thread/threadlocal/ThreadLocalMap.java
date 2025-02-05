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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Normal;

/**
 * 存储所有 {@link SpecificThread} 的 {@link ThreadLocal} 变量的内部数据结构。 请注意，此类仅供内部使用。除非知道自己在做什么，否则请使用 {@link SpecificThread}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ThreadLocalMap {

    /**
     * 无效的value值（占位符），不使用null做无效值的原因是因为netty认为null也是一个有效值，
     * 例如：假设没有重写FastThreadLocal的initialValue()方法，则该方法返回为null，会将null作为有效值直接存储起来
     */
    public static final Object UNSET = new Object();
    /**
     * 线程缓存
     */
    private static final ThreadLocal<ThreadLocalMap> SLOW_THREAD_LOCAL_MAP = new ThreadLocal<>();
    /**
     * 下一索引
     */
    private static final AtomicInteger NEXT_INDEX = new AtomicInteger();
    /**
     * 要删除的变量索引
     */
    public static final int VARIABLES_TO_REMOVE_INDEX = nextVariableIndex();
    /**
     * 索引变量 {@link FastThreadLocal} index就是FastThreadLocal的唯一索引index value是相对应的FastThreadLocal所要存储的值
     */
    private Object[] indexedVariables;
    /**
     * BitSet简要原理： BitSet默认底层数据结构是一个long[]数组，开始时长度为1，即只有long[0],而一个long有64bit。
     * 当BitSet.set(1)的时候，表示将long[0]的第二位设置为true，即0000 0000 ... 0010（64bit）,则long[0]==2
     * 当BitSet.get(1)的时候，第二位为1，则表示true；如果是0，则表示false 当BitSet.set(64)的时候，表示设置第65位，此时long[0]已经不够用了，扩容处long[1]来，进行存储
     * <p>
     * 存储类似 {index:boolean} 键值对，用于防止一个FastThreadLocal多次启动清理线程
     * 将index位置的bit设为true，表示该InternalThreadLocalMap中对该FastThreadLocal已经启动了清理线程
     */
    private BitSet cleanerFlags;

    /**
     * 构造 创建indexedVariables数组，并将每一个元素初始化为UNSET
     */
    private ThreadLocalMap() {
        indexedVariables = newIndexedVariableTable();
    }

    /**
     * 获取ThreadLocalMap实例
     *
     * @return {@link ThreadLocalMap}
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
     * 获取hreadLocalMap实例，如果为null会创建新的；如果不为null，也直接返回
     *
     * @param thread 快速访问变量
     * @return {@link ThreadLocalMap}
     */
    private static ThreadLocalMap fastGet(SpecificThread thread) {
        ThreadLocalMap threadLocalMap = thread.getThreadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new ThreadLocalMap());
        }
        return threadLocalMap;
    }

    /**
     * 获取当前线程信息
     *
     * @return {@link ThreadLocalMap}
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
     * 获取当前线程信息
     *
     * @return {@link ThreadLocalMap}
     */
    public static ThreadLocalMap getIfSet() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SpecificThread) {
            return ((SpecificThread) thread).getThreadLocalMap();
        }
        return SLOW_THREAD_LOCAL_MAP.get();
    }

    /**
     * 删除当前线程的ThreadLocalMap
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
     * 销毁线程信息
     */
    public static void destroy() {
        SLOW_THREAD_LOCAL_MAP.remove();
    }

    /**
     * 获取FastThreadLocal的唯一索引
     *
     * @return the int
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
     * 新的索引变量表
     *
     * @return the object
     */
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[Normal._32];
        Arrays.fill(array, UNSET);
        return array;
    }

    /**
     * 当前线程大小
     *
     * @return the int
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
     * 是否索引变量集
     *
     * @param index 索引
     * @return the true/false
     */
    public boolean isIndexedVariableSet(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length && lookup[index] != UNSET;
    }

    /**
     * 获取指定位置的元素
     *
     * @param index 索引
     * @return the object
     */
    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length ? lookup[index] : UNSET;
    }

    /**
     * 设置索引变量
     *
     * @param index 索引
     * @param value 变量值
     * @return @code true} 当且仅当创建了新的线程局部变量时
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
     * 删除指定位置的对象
     *
     * @param index 索引
     * @return the object
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
     * 设置当前索引位置index（FastThreadLocal）的bit为1
     */
    public void setCleanerFlags(int index) {
        if (cleanerFlags == null) {
            cleanerFlags = new BitSet();
        }
        cleanerFlags.set(index);
    }

    /**
     * 获取 当前index的bit值，1表示true，0表示false（默认值）
     */
    public boolean isCleanerFlags(int index) {
        return cleanerFlags != null && cleanerFlags.get(index);
    }

    /**
     * 展开索引变量表和集合
     *
     * @param index 索引
     * @param value 索引值
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

        // 创建新数组并拷贝旧数组的元素到新数组
        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        // 初始化扩容出来的部分的元素
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        // 设置变量
        newArray[index] = value;
        // 将新数组设置给成员变量
        indexedVariables = newArray;
    }

}