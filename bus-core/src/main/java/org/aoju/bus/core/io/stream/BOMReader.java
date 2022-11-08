/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io.stream;

import org.aoju.bus.core.lang.Assert;

import java.io.*;

/**
 * 读取带BOM头的流内容的Reader，如果非bom的流或无法识别的编码，则默认UTF-8
 * BOM定义：http://www.unicode.org/unicode/faq/utf_bom.html
 *
 * <ul>
 * <li>00 00 FE FF = UTF-32, big-endian</li>
 * <li>FF FE 00 00 = UTF-32, little-endian</li>
 * <li>EF BB BF = UTF-8</li>
 * <li>FE FF = UTF-16, big-endian</li>
 * <li>FF FE = UTF-16, little-endian</li>
 * </ul>
 * 使用：
 * <code>
 * FileInputStream fis = new FileInputStream(file);
 * BOMReader uin = new BOMReader(fis);
 * </code>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BOMReader extends Reader {

    private InputStreamReader reader;

    /**
     * 构造
     *
     * @param in 流
     */
    public BOMReader(final InputStream in) {
        Assert.notNull(in, "InputStream must be not null!");
        final BOMInputStream bin = (in instanceof BOMInputStream) ? (BOMInputStream) in : new BOMInputStream(in);
        try {
            this.reader = new InputStreamReader(bin, bin.getCharset());
        } catch (final UnsupportedEncodingException ignore) {
        }
    }

    @Override
    public int read(final char[] buffer, final int off, final int len) throws IOException {
        return reader.read(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
