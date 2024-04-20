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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Charset;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * 读取带BOM头的流内容,<code>getCharset()</code>方法调用后会得到BOM头的编码,且会去除BOM头
 * <ul>
 * <li>00 00 FE FF = UTF-32, big-endian</li>
 * <li>FF FE 00 00 = UTF-32, little-endian</li>
 * <li>EF BB BF = UTF-8</li>
 * <li>FE FF = UTF-16, big-endian</li>
 * <li>FF FE = UTF-16, little-endian</li>
 * </ul>
 * 使用：
 * <code>
 * String enc = "UTF-8"; // or NULL to use systemdefault
 * FileInputStream fis = new FileInputStream(file);
 * BOMInputStream uin = new BOMInputStream(fis, enc);
 * enc = uin.getCharset(); // check and skip possible BOM bytes
 * </code>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BOMInputStream extends InputStream {

    private static final int BOM_SIZE = 4;
    private final PushbackInputStream in;
    private final String defaultCharset;
    private boolean isInited = false;
    private String charset;

    /**
     * 构造
     *
     * @param in 流
     */
    public BOMInputStream(final InputStream in) {
        this(in, Charset.DEFAULT_UTF_8);
    }

    /**
     * 构造
     *
     * @param in             流
     * @param defaultCharset 默认编码
     */
    public BOMInputStream(final InputStream in, final String defaultCharset) {
        this.in = new PushbackInputStream(in, BOM_SIZE);
        this.defaultCharset = defaultCharset;
    }

    /**
     * 获取默认编码
     *
     * @return 默认编码
     */
    public String getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * 获取BOM头中的编码
     *
     * @return 编码
     */
    public String getCharset() {
        if (false == isInited) {
            try {
                init();
            } catch (final IOException ex) {
                throw new InternalException(ex);
            }
        }
        return charset;
    }

    @Override
    public void close() throws IOException {
        isInited = true;
        in.close();
    }

    @Override
    public int read() throws IOException {
        isInited = true;
        return in.read();
    }

    /**
     * 预读四个字节并检查BOM标记
     * 额外的字节未读回流，只有BOM字节被跳过
     *
     * @throws IOException 读取引起的异常
     */
    protected void init() throws IOException {
        if (isInited) {
            return;
        }

        final byte[] bom = new byte[BOM_SIZE];
        final int n;
        final int unread;
        n = in.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            charset = Charset.DEFAULT_UTF_32_BE;
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            charset = Charset.DEFAULT_UTF_32_LE;
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            charset = Charset.DEFAULT_UTF_8;
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            charset = Charset.DEFAULT_UTF_16_BE;
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            charset = Charset.DEFAULT_UTF_16_LE;
            unread = n - 2;
        } else {
            charset = defaultCharset;
            unread = n;
        }

        if (unread > 0) {
            in.unread(bom, (n - unread), unread);
        }

        isInited = true;
    }

}
