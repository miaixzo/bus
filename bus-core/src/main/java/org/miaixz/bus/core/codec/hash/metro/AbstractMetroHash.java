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
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;

/**
 * Apache 发布的MetroHash算法抽象实现，是一组用于非加密用例的最先进的哈希函数。 除了卓越的性能外，他们还以算法生成而著称。
 *
 * <p>
 * 官方实现：https://github.com/jandrewrogers/MetroHash 官方文档：http://www.jandrewrogers.com/2015/05/27/metrohash/
 * 来自：https://github.com/postamar/java-metrohash/
 * </p>
 *
 * @param <R> 返回值类型，为this类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMetroHash<R extends AbstractMetroHash<R>> implements MetroHash<R> {

    final long seed;
    long v0, v1, v2, v3;
    long nChunks;

    /**
     * 使用指定种子构造
     *
     * @param seed 种子
     */
    public AbstractMetroHash(final long seed) {
        this.seed = seed;
        reset();
    }

    static long grab(final ByteBuffer bb, final int length) {
        long result = bb.get() & 0xFFL;
        for (int i = 1; i < length; i++) {
            result |= (bb.get() & 0xFFL) << (i << 3);
        }
        return result;
    }

    static void writeLittleEndian(final long hash, final ByteBuffer output) {
        for (int i = 0; i < 8; i++) {
            output.put((byte) (hash >>> (i * 8)));
        }
    }

    @Override
    public R apply(final ByteBuffer input) {
        reset();
        while (input.remaining() >= 32) {
            partialApply32ByteChunk(input);
        }
        return partialApplyRemaining(input);
    }

    /**
     * 从byteBuffer中计算32-byte块并更新hash状态
     *
     * @param partialInput byte buffer，至少有32byte的数据
     * @return this
     */
    abstract R partialApply32ByteChunk(ByteBuffer partialInput);

    /**
     * 从byteBuffer中计算剩余bytes并更新hash状态
     *
     * @param partialInput byte buffer，少于32byte的数据
     * @return this
     */
    abstract R partialApplyRemaining(ByteBuffer partialInput);

}
