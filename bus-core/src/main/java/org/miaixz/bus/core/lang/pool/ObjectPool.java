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
package org.miaixz.bus.core.lang.pool;

import java.io.Closeable;
import java.io.Serializable;

/**
 * 对象池接口，提供：
 * <ul>
 * <li>{@link #borrowObject()} 对象借出。</li>
 * <li>{@link #returnObject(Poolable)}对象归还。</li>
 * </ul>
 * 对于对象池中对象维护，通过{@link PoolConfig#getMaxIdle()}控制，规则如下：
 * <ul>
 * <li>如果借出量很多，则不断扩容，直到达到{@link PoolConfig#getMaxSize()}</li>
 * <li>如果池对象闲置超出{@link PoolConfig#getMaxIdle()}，则销毁。</li>
 * <li>实际使用中，池中对象可能少于{@link PoolConfig#getMinSize()}</li>
 * </ul>
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ObjectPool<T> extends Closeable, Serializable {

    /**
     * 借出对象，流程如下：
     * <ol>
     * <li>从池中取出对象</li>
     * <li>检查对象可用性</li>
     * <li>如果无可用对象，扩容池并创建新对象</li>
     * <li>继续取对象</li>
     * </ol>
     *
     * @return 对象
     */
    Poolable<T> borrowObject();

    /**
     * 归还对象，流程如下：
     * <ol>
     * <li>检查对象可用性</li>
     * <li>不可用则销毁之</li>
     * <li>可用则入池</li>
     * </ol>
     *
     * @param obj 对象
     * @return this
     */
    ObjectPool<T> returnObject(final Poolable<T> obj);

    /**
     * 获取持有对象总数（包括空闲对象 + 正在使用对象数）
     *
     * @return 总数
     */
    int getTotal();

    /**
     * 获取空闲对象数，即在池中的对象数
     *
     * @return 空闲对象数，-1表示此信息不可用
     */
    int getIdleCount();

    /**
     * 获取已经借出的对象（正在使用的）对象数
     *
     * @return 正在使用的对象数，-1表示此对象不可用
     */
    int getActiveCount();

}
