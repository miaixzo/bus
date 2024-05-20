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
package org.miaixz.bus.core.io.stream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * {@link Reader}作为{@link InputStream}使用的实现。
 * 参考：Apache Commons IO
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderInputStream extends InputStream {

    private final Reader reader;
    /**
     * 用于将字符转换为字节的CharsetEncoder
     */
    private final CharsetEncoder encoder;
    private final CharBuffer encoderIn;
    private final ByteBuffer encoderOut;
    private CoderResult lastCoderResult;
    private boolean endOfInput;

    /**
     * 构造，使用指定的字符集和默认缓冲区大小
     *
     * @param reader  提供字符数据的Reader
     * @param charset 字符集，用于创建CharsetEncoder
     */
    public ReaderInputStream(final Reader reader, final java.nio.charset.Charset charset) {
        this(reader, charset, Normal.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 构造，使用指定的字符集和缓冲区大小
     *
     * @param reader     提供字符数据的Reader
     * @param charset    字符集，用于创建CharsetEncoder
     * @param bufferSize 缓冲区大小
     */
    public ReaderInputStream(final Reader reader, final java.nio.charset.Charset charset, final int bufferSize) {
        this(reader, Charset.newEncoder(charset, CodingErrorAction.REPLACE), bufferSize);
    }

    /**
     * 构造，使用默认的缓冲区大小
     *
     * @param reader  提供字符数据的Reader
     * @param encoder 用于编码的CharsetEncoder
     */
    public ReaderInputStream(final Reader reader, final CharsetEncoder encoder) {
        this(reader, encoder, Normal.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 构造，允许指定缓冲区大小。
     *
     * @param reader     提供字符数据的Reader
     * @param encoder    用于编码的CharsetEncoder
     * @param bufferSize 缓冲区大小
     */
    public ReaderInputStream(final Reader reader, final CharsetEncoder encoder, final int bufferSize) {
        this.reader = reader;
        this.encoder = encoder;

        encoderIn = CharBuffer.allocate(bufferSize);
        encoderIn.flip();
        encoderOut = ByteBuffer.allocate(bufferSize);
        encoderOut.flip();
    }

    @Override
    public int read(final byte[] b, int off, int len) throws IOException {
        Assert.notNull(b, "Byte array must not be null");
        if ((len < 0) || (off < 0) || (off + len > b.length)) {
            throw new IndexOutOfBoundsException("Array Size=" + b.length + ", offset=" + off + ", length=" + len);
        }

        int read = 0;
        if (len == 0) {
            return 0;
        }
        while (len > 0) {
            if (encoderOut.hasRemaining()) {
                final int c = Math.min(encoderOut.remaining(), len);
                encoderOut.get(b, off, c);
                off += c;
                len -= c;
                read += c;
            } else {
                fillBuffer();
                if ((endOfInput) && (!encoderOut.hasRemaining())) {
                    break;
                }
            }
        }
        return (read == 0) && (endOfInput) ? -1 : read;
    }

    @Override
    public int read() throws IOException {
        do {
            if (encoderOut.hasRemaining()) {
                return encoderOut.get() & 0xFF;
            }
            fillBuffer();
        } while ((!endOfInput) || (encoderOut.hasRemaining()));
        return -1;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * 填充缓冲区。
     * 此方法用于从输入源读取数据，并将其编码后存储到输出缓冲区中。
     * 它处理输入数据，直到达到输入的末尾或者编码过程中遇到需要停止的条件。
     * 在这个过程中，它会更新编码器的状态以及输入输出缓冲区的状态。
     *
     * @throws IOException 如果在读取输入数据时发生IO异常。
     */
    private void fillBuffer() throws IOException {
        // 如果输入未结束，并且上一次的编码结果是正常的（没有溢出或错误），则尝试读取更多数据
        if ((!endOfInput) && ((lastCoderResult == null) || (lastCoderResult.isUnderflow()))) {
            encoderIn.compact(); // 准备好输入缓冲区，以便接收新的数据
            final int position = encoderIn.position(); // 记录当前读取位置

            // 从reader中读取数据到encoderIn缓冲区
            final int c = reader.read(encoderIn.array(), position, encoderIn.remaining());
            if (c == -1) // 如果读取到输入末尾
                endOfInput = true;
            else {
                // 更新读取位置，准备处理下一批数据
                encoderIn.position(position + c);
            }
            encoderIn.flip(); // 反转输入缓冲区，使其准备好进行编码
        }

        // 准备输出缓冲区，以便接收编码后的数据
        encoderOut.compact();
        // 执行编码操作，将输入缓冲区的数据编码到输出缓冲区
        lastCoderResult = encoder.encode(encoderIn, encoderOut, endOfInput);
        // 反转输出缓冲区，使其准备好被写入到最终目的地
        encoderOut.flip();
    }

}

