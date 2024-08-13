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
package org.miaixz.bus.office.csv;

import java.io.Serializable;

import org.miaixz.bus.core.lang.Symbol;

/**
 * CSV写出配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvWriteConfig extends CsvConfig<CsvWriteConfig> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 是否始终使用文本分隔符，文本包装符，默认false，按需添加
     */
    protected boolean alwaysDelimitText;
    /**
     * 换行符
     */
    protected char[] lineDelimiter = { Symbol.C_CR, Symbol.C_LF };
    /**
     * 是否使用安全模式，对可能存在DDE攻击的内容进行替换
     */
    protected boolean ddeSafe;

    /**
     * 文件末尾是否添加换行符 按照https://datatracker.ietf.org/doc/html/rfc4180#section-2 规范，末尾换行符可有可无。
     */
    protected boolean endingLineBreak;

    /**
     * 默认配置
     *
     * @return 默认配置
     */
    public static CsvWriteConfig defaultConfig() {
        return new CsvWriteConfig();
    }

    /**
     * 设置是否始终使用文本分隔符，文本包装符，默认false，按需添加
     *
     * @param alwaysDelimitText 是否始终使用文本分隔符，文本包装符，默认false，按需添加
     * @return this
     */
    public CsvWriteConfig setAlwaysDelimitText(final boolean alwaysDelimitText) {
        this.alwaysDelimitText = alwaysDelimitText;
        return this;
    }

    /**
     * 设置换行符
     *
     * @param lineDelimiter 换行符
     * @return this
     */
    public CsvWriteConfig setLineDelimiter(final char[] lineDelimiter) {
        this.lineDelimiter = lineDelimiter;
        return this;
    }

    /**
     * 设置是否动态数据交换安全，使用文本包装符包裹可能存在DDE攻击的内容 见：https://blog.csdn.net/weixin_41924764/article/details/108665746
     *
     * @param ddeSafe dde安全
     * @return this
     */
    public CsvWriteConfig setDdeSafe(final boolean ddeSafe) {
        this.ddeSafe = ddeSafe;
        return this;
    }

    /**
     * 文件末尾是否添加换行符 按照https://datatracker.ietf.org/doc/html/rfc4180#section-2 规范，末尾换行符可有可无。
     *
     * @param endingLineBreak 文件末尾是否添加换行符
     * @return this
     */
    public CsvWriteConfig setEndingLineBreak(final boolean endingLineBreak) {
        this.endingLineBreak = endingLineBreak;
        return this;
    }

}
