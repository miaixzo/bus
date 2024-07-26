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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.io.CharsetDetector;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 字符集工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Charset {

    /**
     * 默认字符集信息
     */
    public static final java.nio.charset.Charset DEFAULT = java.nio.charset.Charset.defaultCharset();
    public static final String DEFAULT_CHARSET = DEFAULT.displayName();
    /**
     * ISO拉丁字母第1号，即ISO- Latin -1
     */
    public static final String DEFAULT_ISO_8859_1 = "ISO-8859-1";
    public static final java.nio.charset.Charset ISO_8859_1 = java.nio.charset.Charset.forName(DEFAULT_ISO_8859_1);
    /**
     * 7位ASCII码，即ISO646-US，也就是Unicode字符集的基本拉丁字符块
     */
    public static final String DEFAULT_US_ASCII = "US-ASCII";
    public static final java.nio.charset.Charset US_ASCII = java.nio.charset.Charset.forName(DEFAULT_US_ASCII);
    /**
     * GBK UCS 转换格式
     */
    public static final String DEFAULT_GBK = "GBK";
    public static final java.nio.charset.Charset GBK = java.nio.charset.Charset.forName(DEFAULT_GBK);
    /**
     * GB2312 转换格式
     */
    public static final String DEFAULT_GB_2312 = "GB2312";
    public static final java.nio.charset.Charset GB_2312 = java.nio.charset.Charset.forName(DEFAULT_GB_2312);
    /**
     * GB18030 编码
     */
    public static final String DEFAULT_GB_18030 = "GB18030";
    public static final java.nio.charset.Charset GB_18030 = java.nio.charset.Charset.forName(DEFAULT_GB_18030);
    /**
     * 8位UCS转换格式
     */
    public static final String DEFAULT_UTF_8 = "UTF-8";
    public static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName(DEFAULT_UTF_8);
    /**
     * 16位UCS转换格式，字节顺序由可选的字节顺序标记标识
     */
    public static final String DEFAULT_UTF_16 = "UTF-16";
    public static final java.nio.charset.Charset UTF_16 = java.nio.charset.Charset.forName(DEFAULT_UTF_16);
    /**
     * 16位UCS转换格式，大写字节顺序
     */
    public static final String DEFAULT_UTF_16_BE = "UTF-16BE";
    public static final java.nio.charset.Charset UTF_16_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_BE);
    /**
     * 16位UCS转换格式，小写字节顺序
     */
    public static final String DEFAULT_UTF_16_LE = "UTF-16LE";
    public static final java.nio.charset.Charset UTF_16_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_LE);
    /**
     * 32位UCS转换格式，大写字节顺序
     */
    public static final String DEFAULT_UTF_32_BE = "UTF-32BE";
    public static final java.nio.charset.Charset UTF_32_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_BE);
    /**
     * 32位UCS转换格式，小写字节顺序
     */
    public static final String DEFAULT_UTF_32_LE = "UTF-32LE";
    public static final java.nio.charset.Charset UTF_32_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_LE);

    /**
     * 转换为Charset对象
     *
     * @param charsetName 字符集，为空则返回默认字符集
     * @return Charset
     * @throws UnsupportedCharsetException 编码不支持
     */
    public static java.nio.charset.Charset charset(final String charsetName) throws UnsupportedCharsetException {
        return StringKit.isBlank(charsetName) ? java.nio.charset.Charset.defaultCharset()
                : java.nio.charset.Charset.forName(charsetName);
    }

    /**
     * 解析字符串编码为Charset对象，解析失败返回系统默认编码
     *
     * @param charsetName 字符集，为空则返回默认字符集
     * @return Charset
     */
    public static java.nio.charset.Charset parse(final String charsetName) {
        return parse(charsetName, java.nio.charset.Charset.defaultCharset());
    }

    /**
     * 解析字符串编码为Charset对象，解析失败返回默认编码
     *
     * @param charsetName    字符集，为空则返回默认字符集
     * @param defaultCharset 解析失败使用的默认编码
     * @return Charset
     */
    public static java.nio.charset.Charset parse(final String charsetName,
            final java.nio.charset.Charset defaultCharset) {
        if (StringKit.isBlank(charsetName)) {
            return defaultCharset;
        }

        java.nio.charset.Charset result;
        try {
            result = java.nio.charset.Charset.forName(charsetName);
        } catch (final UnsupportedCharsetException e) {
            result = defaultCharset;
        }

        return result;
    }

    /**
     * 转换字符串的字符集编码
     *
     * @param source      字符串
     * @param srcCharset  源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return 转换后的字符集
     */
    public static String convert(final String source, final String srcCharset, final String destCharset) {
        return convert(source, java.nio.charset.Charset.forName(srcCharset),
                java.nio.charset.Charset.forName(destCharset));
    }

    /**
     * 转换字符串的字符集编码 当以错误的编码读取为字符串时，打印字符串将出现乱码。 此方法用于纠正因读取使用编码错误导致的乱码问题。
     * 例如，在Servlet请求中客户端用GBK编码了请求参数，我们使用UTF-8读取到的是乱码，此时，使用此方法即可还原原编码的内容
     * 
     * <pre>
     * 客户端 - GBK编码 - Servlet容器 - UTF-8解码 - 乱码
     * 乱码 - UTF-8编码 - GBK解码 - 正确内容
     * </pre>
     *
     * @param source      字符串
     * @param srcCharset  源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return 转换后的字符集
     */
    public static String convert(final String source, java.nio.charset.Charset srcCharset,
            java.nio.charset.Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = ISO_8859_1;
        }

        if (null == destCharset) {
            destCharset = UTF_8;
        }

        if (StringKit.isBlank(source) || srcCharset.equals(destCharset)) {
            return source;
        }
        return new String(source.getBytes(srcCharset), destCharset);
    }

    /**
     * 转换文件编码 此方法用于转换文件编码，读取的文件实际编码必须与指定的srcCharset编码一致，否则导致乱码
     *
     * @param file        文件
     * @param srcCharset  原文件的编码，必须与文件内容的编码保持一致
     * @param destCharset 转码后的编码
     * @return 被转换编码的文件
     */
    public static File convert(final File file, final java.nio.charset.Charset srcCharset,
            final java.nio.charset.Charset destCharset) {
        ;
        return FileKit.writeString(FileKit.readString(file, srcCharset), file, destCharset);
    }

    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码，否则取 {@link Charset#defaultCharsetName()}
     *
     * @return 系统字符集编码
     * @see Charset#defaultCharsetName()
     */
    public static String systemCharsetName() {
        return systemCharset().name();
    }

    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码，否则取 {@link Charset#defaultCharsetName()}
     *
     * @return 系统字符集编码
     * @see Charset#defaultCharsetName()
     */
    public static java.nio.charset.Charset systemCharset() {
        return FileKit.isWindows() ? GBK : defaultCharset();
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
    public static java.nio.charset.Charset defaultCharset() {
        return java.nio.charset.Charset.defaultCharset();
    }

    /**
     * 探测编码 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用使用支持reset方法的流
     *
     * @param in       流，使用后关闭此流
     * @param charsets 需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     * @see CharsetDetector#detect(InputStream, java.nio.charset.Charset...)
     */
    public static java.nio.charset.Charset detect(final InputStream in, final java.nio.charset.Charset... charsets) {
        return CharsetDetector.detect(in, charsets);
    }

    /**
     * 探测编码 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用使用支持reset方法的流
     *
     * @param bufferSize 自定义缓存大小，即每次检查的长度
     * @param in         流，使用后关闭此流
     * @param charsets   需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     * @see CharsetDetector#detect(int, InputStream, java.nio.charset.Charset...)
     */
    public static java.nio.charset.Charset detect(final int bufferSize, final InputStream in,
            final java.nio.charset.Charset... charsets) {
        return CharsetDetector.detect(bufferSize, in, charsets);
    }

    /**
     * 创建一个新的CharsetEncoder实例，配置指定的字符集和错误处理策略。
     *
     * @param charset 指定的字符集，不允许为null。
     * @param action  对于不合法的字符或无法映射的字符的处理策略，不允许为null
     * @return 配置好的CharsetEncoder实例
     */
    public static CharsetEncoder newEncoder(final java.nio.charset.Charset charset, final CodingErrorAction action) {
        return Assert.notNull(charset).newEncoder().onMalformedInput(action).onUnmappableCharacter(action);
    }

    /**
     * 创建一个新的CharsetDecoder实例，配置指定的字符集和错误处理行为。
     *
     * @param charset 指定的字符集，不允许为null。
     * @param action  当遇到不合法的字符编码或不可映射字符时采取的行动，例如忽略、替换等。
     * @return 配置好的CharsetDecoder实例，用于解码字符。
     */
    public static CharsetDecoder newDecoder(final java.nio.charset.Charset charset, final CodingErrorAction action) {
        return Assert.notNull(charset).newDecoder().onMalformedInput(action).onUnmappableCharacter(action)
                // 设置遇到无法解码的字符时的替换字符串
                .replaceWith("?");
    }

}
