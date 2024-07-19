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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * 通过一个 Writer和一个CharsetDecoder实现将字节数据输出为字符数据。可以通过不同的构造函数配置缓冲区大小和是否立即写入。
 * 来自：https://github.com/subchen/jetbrick-commons/blob/master/src/main/java/jetbrick/io/stream/WriterOutputStream.java
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WriterOutputStream extends OutputStream {

    private final Writer writer;
    private final CharsetDecoder decoder;
    private final boolean writeImmediately;
    private final ByteBuffer decoderIn;
    private final CharBuffer decoderOut;

    /**
     * 构造函数，使用指定字符集和默认配置。
     *
     * @param writer  目标 Writer，用于写入字符数据
     * @param charset 字符集，用于编码字节数据
     */
    public WriterOutputStream(final Writer writer, final java.nio.charset.Charset charset) {
        this(writer, charset, Normal._8192, false);
    }

    /**
     * 构造函数，使用指定字符集、默认缓冲区大小和不立即写入配置。
     *
     * @param writer           目标 Writer，用于写入字符数据
     * @param charset          字符集，用于编码字节数据
     * @param bufferSize       缓冲区大小，用于控制字符数据的临时存储量
     * @param writeImmediately 是否立即写入，如果为 true，则不使用内部缓冲区，每个字节立即被解码并写入
     */
    public WriterOutputStream(final Writer writer, final java.nio.charset.Charset charset, final int bufferSize, final boolean writeImmediately) {
        this(writer, Charset.newDecoder(charset, CodingErrorAction.REPLACE), bufferSize, writeImmediately);
    }

    /**
     * 构造，使用默认缓冲区大小和不立即写入配置。
     *
     * @param writer  目标 Writer，用于写入字符数据
     * @param decoder 字符集解码器，用于将字节数据解码为字符数据
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder) {
        this(writer, decoder, Normal._8192, false);
    }

    /**
     * 构造，允许自定义缓冲区大小和是否立即写入的配置。
     *
     * @param writer           目标 Writer，用于写入字符数据
     * @param decoder          字符集解码器，用于将字节数据解码为字符数据
     * @param bufferSize       缓冲区大小，用于控制字符数据的临时存储量
     * @param writeImmediately 是否立即写入，如果为 true，则不使用内部缓冲区，每个字节立即被解码并写入
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder, final int bufferSize, final boolean writeImmediately) {
        this.writer = writer;
        this.decoder = decoder;
        this.writeImmediately = writeImmediately;
        this.decoderOut = CharBuffer.allocate(bufferSize);
        this.decoderIn = ByteBuffer.allocate(128);
    }

    @Override
    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int c = Math.min(len, decoderIn.remaining());
            decoderIn.put(b, off, c);
            processInput(false);
            len -= c;
            off += c;
        }
        if (writeImmediately) flushOutput();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void flush() throws IOException {
        flushOutput();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        processInput(true);
        flushOutput();
        writer.close();
    }

    private void processInput(final boolean endOfInput) throws IOException {
        decoderIn.flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (!coderResult.isOverflow()) break;
            flushOutput();
        }
        if (!coderResult.isUnderflow()) {
            throw new IOException("Unexpected coder result");
        }

        decoderIn.compact();
    }

    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

}
