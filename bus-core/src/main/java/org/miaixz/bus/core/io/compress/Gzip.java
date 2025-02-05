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
package org.miaixz.bus.core.io.compress;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * GZIP是用于Unix系统的文件压缩 gzip的基础是DEFLATE
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Gzip implements Closeable {

    private InputStream source;
    private OutputStream target;

    /**
     * 构造
     *
     * @param source 源流
     * @param target 目标流
     */
    public Gzip(final InputStream source, final OutputStream target) {
        this.source = source;
        this.target = target;
    }

    /**
     * 创建Gzip
     *
     * @param source 源流
     * @param target 目标流
     * @return Gzip
     */
    public static Gzip of(final InputStream source, final OutputStream target) {
        return new Gzip(source, target);
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
     * @return Gzip
     */
    public Gzip gzip() {
        try {
            target = (target instanceof GZIPOutputStream) ? (GZIPOutputStream) target : new GZIPOutputStream(target);
            IoKit.copy(source, target);
            ((GZIPOutputStream) target).finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 将压缩流解压到target中
     *
     * @return Gzip
     */
    public Gzip unGzip() {
        try {
            source = (source instanceof GZIPInputStream) ? (GZIPInputStream) source : new GZIPInputStream(source);
            IoKit.copy(source, target);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.target);
        IoKit.closeQuietly(this.source);
    }

}
