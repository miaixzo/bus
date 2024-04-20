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
package org.miaixz.bus.core.compress;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.IoKit;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Deflate算法
 * Deflate是同时使用了LZ77算法与哈夫曼编码（Huffman Coding）的一个无损数据压缩算法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Deflate implements Closeable {

    private final InputStream source;
    private final boolean nowrap;
    private OutputStream target;

    /**
     * 构造
     *
     * @param source 源流
     * @param target 目标流
     * @param nowrap {@code true}表示兼容Gzip压缩
     */
    public Deflate(InputStream source, OutputStream target, boolean nowrap) {
        this.source = source;
        this.target = target;
        this.nowrap = nowrap;
    }

    /**
     * 创建Deflate
     *
     * @param source 源流
     * @param target 目标流
     * @param nowrap {@code true}表示兼容Gzip压缩
     * @return this
     */
    public static Deflate of(InputStream source, OutputStream target, boolean nowrap) {
        return new Deflate(source, target, nowrap);
    }

    @Override
    public void close() {
        IoKit.close(this.target);
        IoKit.close(this.source);
    }

    /**
     * 获取目标流
     *
     * @return 目标流
     */
    public OutputStream getTarget() {
        return this.target;
    }

    /**
     * 将普通数据流压缩
     *
     * @param level 压缩级别，0~9
     * @return this
     */
    public Deflate deflater(int level) {
        target = (target instanceof DeflaterOutputStream) ?
                (DeflaterOutputStream) target : new DeflaterOutputStream(target, new Deflater(level, nowrap));
        IoKit.copy(source, target);
        try {
            ((DeflaterOutputStream) target).finish();
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 将压缩流解压到target中
     *
     * @return this
     */
    public Deflate inflater() {
        target = (target instanceof InflaterOutputStream) ?
                (InflaterOutputStream) target : new InflaterOutputStream(target, new Inflater(nowrap));
        IoKit.copy(source, target);
        try {
            ((InflaterOutputStream) target).finish();
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

}
