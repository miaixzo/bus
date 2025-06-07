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
package org.miaixz.bus.core.lang.pool;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对象池配置，提供基本的配置项，包括：
 * <ul>
 * <li>最小池大小（初始大小）</li>
 * <li>最大池大小</li>
 * <li>最长等待时间</li>
 * <li>最长空闲时间</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PoolConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852272106223L;
    /**
     * 最小（初始）池大小
     */
    private int minSize = 5;
    /**
     * 最大池大小
     */
    private int maxSize = 20;
    /**
     * 最长等待时间，用于在借出对象时，等待最长时间（默认5秒）。
     */
    private long maxWait = 5000;
    /**
     * 最长空闲时间（在池中时间）
     */
    private long maxIdle;

    /**
     * 创建{@code PoolConfig}
     *
     * @return {@code PoolConfig}
     */
    public static PoolConfig of() {
        return new PoolConfig();
    }

    /**
     * 获取最小（初始）池大小
     *
     * @return 最小（初始）池大小
     */
    public int getMinSize() {
        return minSize;
    }

    /**
     * 设置最小（初始）池大小
     *
     * @param minSize 最小（初始）池大小
     * @return this
     */
    public PoolConfig setMinSize(final int minSize) {
        this.minSize = minSize;
        return this;
    }

    /**
     * 获取最大池大小
     *
     * @return 最大池大小
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 设置最大池大小
     *
     * @param maxSize 最大池大小
     * @return this
     */
    public PoolConfig setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * 获取最长等待时间，用于在借出对象时，等待最长时间。
     *
     * @return 最长等待时间，用于在借出对象时，等待最长时间。
     */
    public long getMaxWait() {
        return maxWait;
    }

    /**
     * 设置最长等待时间，用于在借出对象时，等待最长时间。
     *
     * @param maxWait 最长等待时间，用于在借出对象时，等待最长时间。
     * @return this
     */
    public PoolConfig setMaxWait(final long maxWait) {
        this.maxWait = maxWait;
        return this;
    }

    /**
     * 获取最长空闲时间（在池中时间）
     *
     * @return 最长空闲时间（在池中时间）,小于等于0表示不限制
     */
    public long getMaxIdle() {
        return maxIdle;
    }

    /**
     * 设置最长空闲时间（在池中时间）
     *
     * @param maxIdle 最长空闲时间（在池中时间）,小于等于0表示不限制
     * @return this
     */
    public PoolConfig setMaxIdle(final long maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

}
