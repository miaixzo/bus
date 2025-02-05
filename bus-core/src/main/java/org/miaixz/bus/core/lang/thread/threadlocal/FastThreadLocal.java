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
 * {@link ThreadLocal} 的一个特殊变体 当从 {@link SpecificThread} 访问时，可产生更高的访问性能。 {@link SpecificThread}
 * 使用数组中的常量索引（而不是使用哈希码和哈希表）来查找变量。 虽然看似非常微妙，但它比使用哈希表具有轻微的性能优势，并且在频繁访问时很有用。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastThreadLocal<V> {

    /**
     * 索引
     */
    private final int index;

    /**
     * 构造
     */
    public FastThreadLocal() {
        this.index = ThreadLocalMap.nextVariableIndex();
    }

    /**
     * 返回绑定到当前线程的线程局部变量的数量
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
     * 删除与当前线程绑定的所有 {@link FastThreadLocal} 变量。 当处于容器环境中，并且不想将线程局部变量留在未管理的线程中时，此操作非常有用
     */
    public static void removeAll() {
        // 1. 获取当前线程的ThreadLocalMap，如果当前的ThreadLocalMap为null，则直接返回
        ThreadLocalMap threadLocalMap = ThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return;
        }

        try {
            // 2.
            // 从indexedVariable[VARIABLES_TO_REMOVE_INDEX]获取目前ThreadLocalMap存储的有效的FastThreadLocal的值，之后遍历Set，进行remove操作
            // 注意：这也是为什么会将有效的FastThreadLocal存储在一个Set中的原因（另外，如果没有Set<FastThreadLocal<?>>这个集合的话，我们需要直接去遍历整个indexedVariables数组，可能其中有效的并不多，影响效率）
            Object v = threadLocalMap.indexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX);
            if (v != null && v != ThreadLocalMap.UNSET) {
                Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
                // 这里为什么需要将set先转换为数组？
                // 因为set的for-remove模式会报并发修改异常，array不会
                FastThreadLocal<?>[] variablesToRemoveArray = variablesToRemove
                        .toArray(new FastThreadLocal[variablesToRemove.size()]);
                for (FastThreadLocal<?> tlv : variablesToRemoveArray) {
                    tlv.remove(threadLocalMap);
                }
            }
        } finally {
            // 3. 删除当前线程的 ThreadLocalMap
            ThreadLocalMap.remove();
        }
    }

    /**
     * 销毁保存所有从非 {@link SpecificThread} 访问的 {@link FastThreadLocal} 变量的数据结构 当处于容器环境中，并且不想将线程局部变量留在未管理的线程中时，此操作非常有用。
     * 当应用程序从容器中卸载时，请调用此方法。
     */
    public static void destroy() {
        ThreadLocalMap.destroy();
    }

    /**
     * 将当前的FastThreadLocal添加到indexedVariable[variableToRemoveIndex]位置上的Set<FastThreadLocal<?>>中
     *
     * @param threadLocalMap 内部数据结构
     * @param variable       环境变量
     */
    private static void addToVariablesToRemove(ThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {
        // 1、首先从InternalThreadLocalMap获取Set，如果存在，直接往Set里添加值FastThreadLocal；
        // 如果不存在，则先创建一个Set，然后将创建的Set添加到InternalThreadLocalMap中，最后将FastThreadLocal添加到这个Set中
        Object v = threadLocalMap.indexedVariable(ThreadLocalMap.VARIABLES_TO_REMOVE_INDEX);
        // Set中的FastThreadLocal可能有多个类型，所以此处的泛型使用?，而不是用指定的V
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
     * 从要移除的变量中删除数据
     *
     * @param threadLocalMap 内部数据结构
     * @param variable       环境变量
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
     * 获取当前线程的ThreadLocalMap中的当前线程的value
     */
    public final V get() {
        // 1. 获取ThreadLocalMap
        ThreadLocalMap threadLocalMap = ThreadLocalMap.get();
        // 2. 从ThreadLocalMap获取索引为index的value，如果该索引处的value是有效值，不是占位值，则直接返回
        Object v = threadLocalMap.indexedVariable(index);
        if (v != ThreadLocalMap.UNSET) {
            return (V) v;
        }
        // 3. indexedVariable[index]没有设置有效值，执行初始化操作，获取初始值
        return initialize(threadLocalMap);
    }

    /**
     * 设置当前线程的值
     *
     * @param value 线程值
     */
    public final void set(V value) {
        // 如果value是UNSET，表示删除当前的ThreadLocal对应的value；
        // 如果不是UNSET，则可能是修改，也可能是新增；
        // 如果是修改，修改value结束后返回，
        // 如果是新增，则先新增value，然后新增ThreadLocal到Set中，最后注册Cleaner清除线程
        if (value == null || value == ThreadLocalMap.UNSET) {
            // 如果设置的值是UNSET，表示清除该FastThreadLocal的value
            remove();
        } else {
            ThreadLocalMap threadLocalMap = ThreadLocalMap.get();
            if (threadLocalMap.setIndexedVariable(index, value)) {
                addToVariablesToRemove(threadLocalMap, this);
            }
        }
    }

    /**
     * 清除当前的ThreadLocal
     */
    public final void remove() {
        remove(ThreadLocalMap.getIfSet());
    }

    /**
     * 将指定线程本地映射的值设置为未初始化 对 get() 的继续调用将触发对 initialValue() 的调用 指定的线程本地映射必须适用于当前线程。
     */
    public final void remove(ThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return;
        }
        // 1. 从 ThreadLocalMap 中删除当前的FastThreadLocal对应的value
        Object v = threadLocalMap.removeIndexedVariable(index);
        // 2. 从 ThreadLocalMap 中的Set<FastThreadLocal<?>>中删除当前的FastThreadLocal
        removeFromVariablesToRemove(threadLocalMap, this);
        // 3. 如果删除的是有效值，则进行onRemove方法的回调
        if (v != ThreadLocalMap.UNSET) {
            try {
                onRemoval((V) v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 初始化参数 由子类重写
     */
    protected V initialValue() {
        return null;
    }

    /**
     * 当此线程局部变量被{@link #remove()}删除时的回调 由子类重写
     */
    protected void onRemoval(V value) {

    }

    /**
     * 初始化
     *
     * @param threadLocalMap 内部数据结构
     * @return 当前线程的值
     */
    private V initialize(ThreadLocalMap threadLocalMap) {
        V v;
        try {
            // 1. 获取初始值
            v = initialValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 2. 设置value到InternalThreadLocalMap中
        threadLocalMap.setIndexedVariable(index, v);
        // 3. 添加当前的FastThreadLocal到ThreadLocalMap的Set<FastThreadLocal<?>>中
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }

}
