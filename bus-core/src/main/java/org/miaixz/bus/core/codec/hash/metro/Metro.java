/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Apache 发布的MetroHash算法接口，是一组用于非加密用例的最先进的哈希函数。
 * 除了卓越的性能外，他们还以算法生成而著称。
 *
 * <p>
 * 官方实现：https://github.com/jandrewrogers/MetroHash
 * 官方文档：http://www.jandrewrogers.com/2015/05/27/metrohash/
 * 来自：https://github.com/postamar/java-metrohash/
 * </p>
 *
 * @param <R> 返回值类型，为this类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Metro<R extends Metro<R>> {

    /**
     * 创建 {@code Metro}对象
     *
     * @param seed  种子
     * @param is128 是否128位
     * @return {@code Metro}对象
     */
    static Metro<?> of(final long seed, final boolean is128) {
        return is128 ? new Metro128(seed) : new Metro64(seed);
    }

    /**
     * 将给定的{@link ByteBuffer}中的数据追加计算hash值<br>
     * 此方法会更新hash值状态
     *
     * @param input 内容
     * @return this
     */
    R apply(final ByteBuffer input);

    /**
     * 将结果hash值写出到{@link ByteBuffer}中，可选端序
     *
     * @param output    输出
     * @param byteOrder 端序
     * @return this
     */
    R write(ByteBuffer output, final ByteOrder byteOrder);

    /**
     * 重置，重置后可复用对象开启新的计算
     *
     * @return this
     */
    R reset();
}
