/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.CommonException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * 读取带BOM头的流内容，<code>getCharset()</code>方法调用后会得到BOM头的编码，且会去除BOM头
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
 * @version 3.0.5
 * @since JDK 1.8
 */
public class BOMInputStream extends InputStream {

    private static final int BOM_SIZE = 4;
    PushbackInputStream in;
    boolean isInited = false;
    String defaultCharset;
    String charset;

    public BOMInputStream(InputStream in) {
        this(in, Charset.DEFAULT_UTF_8);
    }

    public BOMInputStream(InputStream in, String defaultCharset) {
        this.in = new PushbackInputStream(in, BOM_SIZE);
        this.defaultCharset = defaultCharset;
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public String getCharset() {
        if (!isInited) {
            try {
                init();
            } catch (IOException ex) {
                throw new CommonException(ex);
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
     * Read-ahead four bytes and check for BOM marks.
     * Extra bytes are unread back to the stream, only BOM bytes are skipped.
     *
     * @throws IOException 读取引起的异常
     */
    protected void init() throws IOException {
        if (isInited) {
            return;
        }

        byte[] bom = new byte[BOM_SIZE];
        int n, unread;
        n = in.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            charset = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            charset = "UTF-32LE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            charset = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            charset = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            charset = "UTF-16LE";
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
