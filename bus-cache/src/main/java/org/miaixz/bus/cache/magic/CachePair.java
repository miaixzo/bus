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
package org.miaixz.bus.cache.magic;

/**
 * 缓存键值对类
 * <p>
 * 用于存储一对关联的键值，提供泛型支持以适应不同类型的数据。 采用不可变设计，一旦创建后不能修改其内容。
 * </p>
 *
 * @param <L> 左值类型
 * @param <R> 右值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CachePair<L, R> {

    /**
     * 左值
     */
    private final L left;

    /**
     * 右值
     */
    private final R right;

    /**
     * 私有构造方法
     *
     * @param left  左值
     * @param right 右值
     */
    private CachePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 创建CachePair实例的工厂方法
     *
     * @param <L>   左值类型
     * @param <R>   右值类型
     * @param left  左值
     * @param right 右值
     * @return CachePair实例
     */
    public static <L, R> CachePair<L, R> of(L left, R right) {
        return new CachePair<>(left, right);
    }

    /**
     * 获取左值
     *
     * @return 左值
     */
    public L getLeft() {
        return left;
    }

    /**
     * 获取右值
     *
     * @return 右值
     */
    public R getRight() {
        return right;
    }

}