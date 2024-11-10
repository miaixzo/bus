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
package org.miaixz.bus.core.io.stream;

import java.io.*;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 同步流，可将包装的流同步为ByteArrayInputStream，以便持有内容并关闭原流
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SyncInputStream extends FilterInputStream {

    private final long length;
    private final boolean isIgnoreEOFError;
    /**
     * 是否异步，异步下只持有流，否则将在初始化时直接读取body内容
     */
    private volatile boolean asyncFlag = true;

    /**
     * 构造 如果isAsync为{@code true}，则直接持有原有流，{@code false}，则将流中内容，按照给定length读到{@link ByteArrayInputStream}中备用
     *
     * @param in               数据流
     * @param length           限定长度，-1表示未知长度
     * @param isAsync          是否异步
     * @param isIgnoreEOFError 是否忽略EOF错误，在Http协议中，对于Transfer-Encoding: Chunked在正常情况下末尾会写入一个Length为0的的chunk标识完整结束
     *                         如果服务端未遵循这个规范或响应没有正常结束，会报EOF异常，此选项用于是否忽略这个异常。
     */
    public SyncInputStream(final InputStream in, final long length, final boolean isAsync,
            final boolean isIgnoreEOFError) {
        super(in);
        this.length = length;
        this.isIgnoreEOFError = isIgnoreEOFError;
        if (!isAsync) {
            sync();
        }
    }

    /**
     * 是否为EOF异常，包括
     * <ul>
     * <li>FileNotFoundException：服务端无返回内容</li>
     * <li>EOFException：EOF异常</li>
     * </ul>
     *
     * @param e 异常
     * @return 是否EOF异常
     */
    private static boolean isEOFException(final Throwable e) {
        if (e instanceof FileNotFoundException) {
            // 服务器无返回内容，忽略之
            return true;
        }
        return e instanceof EOFException || StringKit.containsIgnoreCase(e.getMessage(), "Premature EOF");
    }

    /**
     * 同步数据到内存，同步后关闭原流
     *
     * @return this
     */
    public SyncInputStream sync() {
        if (asyncFlag) {
            this.in = new ByteArrayInputStream(readBytes());
            this.asyncFlag = false;
        }

        return this;
    }

    /**
     * 读取流中所有bytes
     *
     * @return bytes
     */
    public byte[] readBytes() {
        final FastByteArrayOutputStream bytesOut = new FastByteArrayOutputStream(length > 0 ? (int) length : 1024);
        final long length = copyTo(bytesOut, null);
        return length > 0 ? bytesOut.toByteArray() : new byte[0];
    }

    /**
     * 将流的内容拷贝到输出流，拷贝结束后关闭输入流
     *
     * @param out            输出流
     * @param streamProgress 进度条
     * @return 拷贝长度
     */
    public long copyTo(final OutputStream out, final StreamProgress streamProgress) {
        long copyLength = -1;
        try {
            copyLength = IoKit.copy(this.in, out, Normal._8192, this.length, streamProgress);
        } catch (final InternalException e) {
            if (!(isIgnoreEOFError && isEOFException(e.getCause()))) {
                throw e;
            }
            // 忽略读取流中的EOF错误
        } finally {
            // 读取结束
            IoKit.closeQuietly(in);
        }
        return copyLength;
    }

}
