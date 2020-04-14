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
package org.aoju.bus.core.io.streams;

import org.aoju.bus.core.io.FastByteBuffer;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CharsetUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 基于快速缓冲FastByteBuffer的OutputStream,自动扩充缓冲区
 * 可以通过{@link #toByteArray()}和 {@link #toString()}来获取数据
 * 避免重新分配内存块而是分配新增的缓冲区,缓冲区不会被GC,数据也不会被拷贝到其他缓冲区
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class ByteArrayOutputStream extends OutputStream {

    private final FastByteBuffer buffer;

    public ByteArrayOutputStream() {
        this(1024);
    }

    /**
     * 构造
     *
     * @param size 预估大小
     */
    public ByteArrayOutputStream(int size) {
        buffer = new FastByteBuffer(size);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.append(b, off, len);
    }

    @Override
    public void write(int b) {
        buffer.append((byte) b);
    }

    public int size() {
        return buffer.size();
    }

    /**
     * 此方法无任何效果,当流被关闭后不会抛出IOException
     */
    @Override
    public void close() throws IOException {
        // nop
    }

    public void reset() {
        buffer.reset();
    }

    /**
     * 写出
     *
     * @param out 输出流
     * @throws InstrumentException IO异常
     */
    public void writeTo(OutputStream out) throws InstrumentException {
        final int index = buffer.index();
        byte[] buf;
        try {
            for (int i = 0; i < index; i++) {
                buf = buffer.array(i);
                out.write(buf);
            }
            out.write(buffer.array(index), 0, buffer.offset());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }


    /**
     * 转为Byte数组
     *
     * @return Byte数组
     */
    public byte[] toByteArray() {
        return buffer.toArray();
    }

    @Override
    public String toString() {
        return new String(toByteArray());
    }

    /**
     * 转为字符串
     *
     * @param charsetName 编码
     * @return 字符串
     */
    public String toString(String charsetName) {
        return toString(CharsetUtils.charset(charsetName));
    }

    /**
     * 转为字符串
     *
     * @param charset 编码
     * @return 字符串
     */
    public String toString(Charset charset) {
        return new String(toByteArray(), charset);
    }

}
