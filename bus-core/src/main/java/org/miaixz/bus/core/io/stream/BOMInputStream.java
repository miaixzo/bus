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

import org.miaixz.bus.core.io.ByteOrderMark;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * 读取带BOM头的流内容，{@code getCharset()}方法调用后会得到BOM头的编码，且会去除BOM头
 * BOM定义：<a href="http://www.unicode.org/unicode/faq/utf_bom.html">http://www.unicode.org/unicode/faq/utf_bom.html</a>
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
 * <p>
 * 参考： <a href="http://akini.mbnet.fi/java/unicodereader/UnicodeInputStream.java.txt">http://www.unicode.org/unicode/faq/utf_bom.html</a>
 * </p>
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
        if (!isInited) {
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
     * Read-ahead four bytes and check for BOM marks.
     * Extra bytes are unread back to the stream, only BOM bytes are skipped.
     *
     * @throws IOException 读取引起的异常
     */
    protected void init() throws IOException {
        if (isInited) {
            return;
        }

        final byte[] bom = new byte[BOM_SIZE];
        final int n;
        int unread = 0;
        n = in.read(bom, 0, bom.length);

        for (final ByteOrderMark byteOrderMark : ByteOrderMark.ALL) {
            if (byteOrderMark.test(bom)) {
                charset = byteOrderMark.getCharsetName();
                unread = n - byteOrderMark.length();
                break;
            }
        }
        if (0 == unread) {
            // Unicode BOM mark not found, unread all bytes
            charset = defaultCharset;
            unread = n;
        }

        if (unread > 0) {
            in.unread(bom, (n - unread), unread);
        }

        isInited = true;
    }

}
