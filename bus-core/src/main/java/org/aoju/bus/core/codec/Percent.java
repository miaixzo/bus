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
package org.aoju.bus.core.codec;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.HexKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.BitSet;

/**
 * 百分号编码(Percent-encoding), 也称作URL编码(URL encoding)
 * 百分号编码可用于URI的编码，也可以用于"application/x-www-form-urlencoded"的MIME准备数据
 * <ul>
 *     <li>URI：遵循RFC 3986保留字规范</li>
 *     <li>application/x-www-form-urlencoded，遵循W3C HTML Form content types规范，如空格须转+</li>
 * </ul>
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class Percent implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 存放安全编码
     */
    private final BitSet safeCharacters;
    /**
     * 是否编码空格为+
     * 如果为{@code true}，则将空格编码为"+"，此项只在"application/x-www-form-urlencoded"中使用
     * 如果为{@code false}，则空格编码为"%20",此项一般用于URL的Query部分（RFC3986规范）
     */
    private boolean encodeSpaceAsPlus = false;

    /**
     * 构造
     * [a-zA-Z0-9]默认不被编码
     */
    public Percent() {
        this(new BitSet(Normal._256));
    }

    /**
     * 构造
     *
     * @param safeCharacters 安全字符，安全字符不被编码
     */
    public Percent(BitSet safeCharacters) {
        this.safeCharacters = safeCharacters;
    }

    /**
     * 从已知Percent创建Percent，会复制给定Percent的安全字符
     *
     * @param codec Percent
     * @return this
     */
    public static Percent of(Percent codec) {
        return new Percent((BitSet) codec.safeCharacters.clone());
    }

    /**
     * 创建Percent，使用指定字符串中的字符作为安全字符
     *
     * @param chars 安全字符合集
     * @return this
     */
    public static Percent of(CharSequence chars) {
        final Percent codec = new Percent();
        final int length = chars.length();
        for (int i = 0; i < length; i++) {
            codec.addSafe(chars.charAt(i));
        }
        return codec;
    }

    /**
     * 增加安全字符
     * 安全字符不被编码
     *
     * @param c 字符
     * @return this
     */
    public Percent addSafe(char c) {
        safeCharacters.set(c);
        return this;
    }

    /**
     * 移除安全字符
     * 安全字符不被编码
     *
     * @param c 字符
     * @return this
     */
    public Percent removeSafe(char c) {
        safeCharacters.clear(c);
        return this;
    }

    /**
     * 增加安全字符到挡墙的Percent
     *
     * @param codec Percent
     * @return this
     */
    public Percent or(Percent codec) {
        this.safeCharacters.or(codec.safeCharacters);
        return this;
    }

    /**
     * 组合当前Percent和指定Percent为一个新的Percent，安全字符为并集
     *
     * @param codec Percent
     * @return this
     */
    public Percent orNew(Percent codec) {
        return of(this).or(codec);
    }

    /**
     * 是否将空格编码为+
     *
     * @param encodeSpaceAsPlus 是否将空格编码为+
     * @return this
     */
    public Percent setEncodeSpaceAsPlus(boolean encodeSpaceAsPlus) {
        this.encodeSpaceAsPlus = encodeSpaceAsPlus;
        return this;
    }

    /**
     * 将URL中的字符串编码为%形式
     *
     * @param path    需要编码的字符串
     * @param charset 编码, {@code null}返回原字符串，表示不编码
     * @return 编码后的字符串
     */
    public String encode(CharSequence path, Charset charset) {
        if (null == charset || StringKit.isEmpty(path)) {
            return StringKit.toString(path);
        }

        final StringBuilder rewrittenPath = new StringBuilder(path.length());
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final OutputStreamWriter writer = new OutputStreamWriter(buf, charset);

        int c;
        for (int i = 0; i < path.length(); i++) {
            c = path.charAt(i);
            if (safeCharacters.get(c)) {
                rewrittenPath.append((char) c);
            } else if (encodeSpaceAsPlus && c == Symbol.C_SPACE) {
                // 对于空格单独处理
                rewrittenPath.append('+');
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write((char) c);
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }

                byte[] ba = buf.toByteArray();
                for (byte toEncode : ba) {
                    // Converting each byte in the buffer
                    rewrittenPath.append('%');
                    HexKit.appendHex(rewrittenPath, toEncode, false);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }

}
