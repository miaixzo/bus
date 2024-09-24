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
package org.miaixz.bus.core.center.stream;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * {@link WrappedStream}接口的公共实现，用于包装并增强一个已有的流实例
 *
 * @param <T> 流中的元素类型
 * @param <S> {@link EnhancedWrappedStream}的实现类类型
 * @author Kimi Liu
 * @see EasyStream
 * @see EntryStream
 * @since Java 17+
 */
public abstract class EnhancedWrappedStream<T, S extends EnhancedWrappedStream<T, S>>
        implements TerminableWrappedStream<T, S>, TransformableWrappedStream<T, S> {

    /**
     * 原始流实例
     */
    protected Stream<T> stream;

    /**
     * 创建一个流包装器
     *
     * @param stream 包装的流对象
     * @throws NullPointerException 当{@code unwrap}为{@code null}时抛出
     */
    protected EnhancedWrappedStream(final Stream<T> stream) {
        this.stream = Objects.requireNonNull(stream, "unwrap must not null");
    }

    /**
     * 获取被包装的元素流实例
     */
    @Override
    public Stream<T> unwrap() {
        return stream;
    }

    /**
     * 获取当前被包装的实例的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return stream.hashCode();
    }

    /**
     * 比较被包装的实例是否相等
     *
     * @param object 对象
     * @return 是否相等
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Stream && stream.equals(object);
    }

    /**
     * 将被包装的实例转为字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return stream.toString();
    }

    /**
     * 触发流的执行，这是一个终端操作
     */
    public void exec() {
        stream.forEach(t -> {
        });
    }

}
