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
package org.miaixz.bus.office.csv;

import org.miaixz.bus.core.lang.Symbol;

import java.io.Serializable;

/**
 * CSV写出配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvWriteConfig extends CsvConfig<CsvWriteConfig> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否始终使用文本分隔符,文本包装符,默认false,按需添加
     */
    protected boolean alwaysDelimitText;
    /**
     * 换行符
     */
    protected char[] lineDelimiter = {Symbol.C_CR, Symbol.C_LF};

    /**
     * 默认配置
     *
     * @return 默认配置
     */
    public static CsvWriteConfig defaultConfig() {
        return new CsvWriteConfig();
    }

    /**
     * 设置是否始终使用文本分隔符,文本包装符,默认false,按需添加
     *
     * @param alwaysDelimitText 是否始终使用文本分隔符,文本包装符,默认false,按需添加
     * @return this
     */
    public CsvWriteConfig setAlwaysDelimitText(boolean alwaysDelimitText) {
        this.alwaysDelimitText = alwaysDelimitText;
        return this;
    }

    /**
     * 设置换行符
     *
     * @param lineDelimiter 换行符
     * @return this
     */
    public CsvWriteConfig setLineDelimiter(char[] lineDelimiter) {
        this.lineDelimiter = lineDelimiter;
        return this;
    }

}

