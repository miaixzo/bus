/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

/**
 * 一种接收器,它在内部保存缓冲区,
 * 以便调用者可以进行小的写操作没有性能损失
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public interface BufferSink extends Sink, WritableByteChannel {

    Buffer buffer();

    BufferSink write(ByteString byteString) throws IOException;

    BufferSink write(byte[] source) throws IOException;

    BufferSink write(byte[] source, int offset, int byteCount) throws IOException;

    long writeAll(Source source) throws IOException;

    BufferSink write(Source source, long byteCount) throws IOException;

    BufferSink writeUtf8(String string) throws IOException;

    BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException;

    BufferSink writeUtf8CodePoint(int codePoint) throws IOException;

    BufferSink writeString(String string, Charset charset) throws IOException;

    BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset)
            throws IOException;

    BufferSink writeByte(int b) throws IOException;

    BufferSink writeShort(int s) throws IOException;

    BufferSink writeShortLe(int s) throws IOException;

    BufferSink writeInt(int i) throws IOException;

    BufferSink writeIntLe(int i) throws IOException;

    BufferSink writeLong(long v) throws IOException;

    BufferSink writeLongLe(long v) throws IOException;

    BufferSink writeDecimalLong(long v) throws IOException;

    BufferSink writeHexadecimalUnsignedLong(long v) throws IOException;

    @Override
    void flush() throws IOException;

    BufferSink emit() throws IOException;

    BufferSink emitCompleteSegments() throws IOException;

    OutputStream outputStream();

}
