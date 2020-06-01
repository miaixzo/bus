/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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

import org.aoju.bus.core.lang.Charset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
public class StringOutputStream extends OutputStream {

    private StringBuilder sb;
    private ByteArrayOutputStream baos;
    private String charset;

    public StringOutputStream(StringBuilder sb) {
        this(sb, Charset.DEFAULT_UTF_8);
    }

    public StringOutputStream(StringBuilder sb, String charset) {
        this.sb = sb;
        baos = new ByteArrayOutputStream();
        this.charset = charset;
    }

    /**
     * 完成本方法后,确认字符串已经完成写入后,务必调用flash方法!
     */
    @Override
    public void write(int b) throws IOException {
        if (null == baos)
            throw new IOException("Stream is closed");
        baos.write(b);
    }

    /**
     * 使用StringBuilder前,务必调用
     */
    @Override
    public void flush() throws IOException {
        if (null != baos) {
            baos.flush();
            if (baos.size() > 0) {
                if (charset == null)
                    sb.append(new String(baos.toByteArray()));
                else
                    sb.append(new String(baos.toByteArray(), charset));
                baos.reset();
            }
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        baos = null;
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }

}