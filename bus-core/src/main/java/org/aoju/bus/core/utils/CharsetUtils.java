/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 字符集工具类
 *
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public class CharsetUtils {

    /**
     * 转换为Charset对象
     *
     * @param charsetName 字符集，为空则返回默认字符集
     * @return Charset
     * @throws UnsupportedCharsetException 编码不支持
     */
    public static Charset charset(String charsetName) throws UnsupportedCharsetException {
        return StringUtils.isBlank(charsetName) ? Charset.defaultCharset() : Charset.forName(charsetName);
    }

    /**
     * 转换字符串的字符集编码
     *
     * @param source      字符串
     * @param srcCharset  源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return 转换后的字符集
     */
    public static String convert(String source, String srcCharset, String destCharset) {
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    /**
     * 转换字符串的字符集编码
     * 当以错误的编码读取为字符串时，打印字符串将出现乱码。
     * 此方法用于纠正因读取使用编码错误导致的乱码问题。
     * 例如，在Servlet请求中客户端用GBK编码了请求参数，我们使用UTF-8读取到的是乱码，此时，使用此方法即可还原原编码的内容
     * <pre>
     * 客户端 -》 GBK编码 -》 Servlet容器 -》 UTF-8解码 -》 乱码
     * 乱码 -》 UTF-8编码 -》 GBK解码 -》 正确内容
     * </pre>
     *
     * @param source      字符串
     * @param srcCharset  源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return 转换后的字符集
     */
    public static String convert(String source, Charset srcCharset, Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = StandardCharsets.ISO_8859_1;
        }

        if (null == destCharset) {
            destCharset = StandardCharsets.UTF_8;
        }

        if (StringUtils.isBlank(source) || srcCharset.equals(destCharset)) {
            return source;
        }
        return new String(source.getBytes(srcCharset), destCharset);
    }

    /**
     * 转换文件编码
     * 此方法用于转换文件编码，读取的文件实际编码必须与指定的srcCharset编码一致，否则导致乱码
     *
     * @param file        文件
     * @param srcCharset  原文件的编码，必须与文件内容的编码保持一致
     * @param destCharset 转码后的编码
     * @return 被转换编码的文件
     * @since 3.1.9
     */
    public static File convert(File file, Charset srcCharset, Charset destCharset) {
        final String str = FileUtils.readString(file, srcCharset);
        return FileUtils.writeString(str, file, destCharset);
    }

    /**
     * 系统默认字符集编码
     *
     * @return 系统字符集编码
     */
    public static String defaultCharsetName() {
        return defaultCharset().name();
    }

    /**
     * 系统默认字符集编码
     *
     * @return 系统字符集编码
     */
    public static Charset defaultCharset() {
        return Charset.defaultCharset();
    }


    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码，否则取 {@link CharsetUtils#defaultCharsetName()}
     *
     * @return 系统字符集编码
     * @see CharsetUtils#defaultCharsetName()
     * @since 3.1.9
     */
    public static String systemCharsetName() {
        return systemCharset().name();
    }

    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码，否则取 {@link CharsetUtils#defaultCharsetName()}
     *
     * @return 系统字符集编码
     * @see CharsetUtils#defaultCharsetName()
     * @since 3.1.9
     */
    public static Charset systemCharset() {
        return FileUtils.isWindows() ? org.aoju.bus.core.consts.Charset.CHARSET_GBK : defaultCharset();
    }

}
