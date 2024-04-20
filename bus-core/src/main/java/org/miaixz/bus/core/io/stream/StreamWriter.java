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

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.IoKit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * {@link OutputStream}写出器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamWriter {

    private final OutputStream out;
    private final boolean closeAfterWrite;

    /**
     * 构造
     *
     * @param out             {@link OutputStream}
     * @param closeAfterWrite 写出结束后是否关闭流
     */
    public StreamWriter(final OutputStream out, final boolean closeAfterWrite) {
        this.out = out;
        this.closeAfterWrite = closeAfterWrite;
    }

    /**
     * 创建写出器
     *
     * @param out             {@link OutputStream}
     * @param closeAfterWrite 写出结束后是否关闭流
     * @return StreamReader
     */
    public static StreamWriter of(final OutputStream out, final boolean closeAfterWrite) {
        return new StreamWriter(out, closeAfterWrite);
    }

    /**
     * 将byte[]写到流中
     *
     * @param content 写入的内容
     * @throws InternalException IO异常
     */
    public void write(final byte[] content) throws InternalException {
        final OutputStream out = this.out;
        try {
            out.write(content);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.close(out);
            }
        }
    }

    /**
     * 将多部分对象写到流中，使用{@link ObjectOutputStream}，对象必须实现序列化接口
     *
     * @param contents 写入的内容
     * @throws InternalException IO异常
     */
    public void writeObject(final Object... contents) throws InternalException {
        ObjectOutputStream osw = null;
        try {
            osw = out instanceof ObjectOutputStream ? (ObjectOutputStream) out : new ObjectOutputStream(out);
            for (final Object content : contents) {
                if (content != null) {
                    osw.writeObject(content);
                }
            }
            osw.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.close(osw);
            }
        }
    }

    /**
     * 将多部分内容写到流中，自动转换为字符串
     *
     * @param charset  写出的内容的字符集
     * @param contents 写入的内容，调用toString()方法，不包括不会自动换行
     * @throws InternalException IO异常
     */
    public void writeString(final Charset charset, final Object... contents) throws InternalException {
        OutputStreamWriter osw = null;
        try {
            osw = IoKit.getWriter(out, charset);
            for (final Object content : contents) {
                if (content != null) {
                    osw.write(Convert.toString(content, Normal.EMPTY));
                }
            }
            osw.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.close(osw);
            }
        }
    }

}
