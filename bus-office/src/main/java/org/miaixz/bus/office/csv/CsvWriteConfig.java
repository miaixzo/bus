/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.Symbol;

/**
 * CSV write configuration options.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvWriteConfig extends CsvConfig<CsvWriteConfig> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283371616L;

    /**
     * Whether to always use text delimiters (quotes). Default is {@code false}, delimiters are added as needed.
     */
    protected boolean alwaysDelimitText;
    /**
     * The newline character(s) to use. Default is CR+LF.
     */
    protected char[] lineDelimiter = { Symbol.C_CR, Symbol.C_LF };
    /**
     * Whether to enable DDE safe mode. If enabled, content that might pose a DDE attack risk will be handled.
     */
    protected boolean ddeSafe;

    /**
     * Whether to add a newline character at the end of the file. According to
     * <a href="https://datatracker.ietf.org/doc/html/rfc4180#section-2">RFC 4180 Section 2</a>, a final line break is
     * optional.
     */
    protected boolean endingLineBreak;

    /**
     * Creates a new default {@code CsvWriteConfig} instance.
     *
     * @return A new {@code CsvWriteConfig} with default settings.
     */
    public static CsvWriteConfig defaultConfig() {
        return new CsvWriteConfig();
    }

    /**
     * Sets whether to always use text delimiters (quotes). Default is {@code false}, delimiters are added as needed.
     *
     * @param alwaysDelimitText {@code true} to always use text delimiters, {@code false} to use them only when
     *                          necessary.
     * @return This configuration object, for chaining.
     */
    public CsvWriteConfig setAlwaysDelimitText(final boolean alwaysDelimitText) {
        this.alwaysDelimitText = alwaysDelimitText;
        return this;
    }

    /**
     * Sets the newline character(s).
     *
     * @param lineDelimiter The character array representing the newline sequence.
     * @return This configuration object, for chaining.
     */
    public CsvWriteConfig setLineDelimiter(final char[] lineDelimiter) {
        this.lineDelimiter = lineDelimiter;
        return this;
    }

    /**
     * Sets whether to enable DDE safe mode. This wraps content that might pose a DDE attack risk with text delimiters.
     * For more information, refer to: <a href="https://blog.csdn.net/weixin_41924764/article/details/108665746">DDE
     * Attack Risk in Excel</a>.
     *
     * @param ddeSafe {@code true} to enable DDE safe mode, {@code false} otherwise.
     * @return This configuration object, for chaining.
     */
    public CsvWriteConfig setDdeSafe(final boolean ddeSafe) {
        this.ddeSafe = ddeSafe;
        return this;
    }

    /**
     * Sets whether to add a newline character at the end of the file. According to
     * <a href="https://datatracker.ietf.org/doc/html/rfc4180#section-2">RFC 4180 Section 2</a>, a final line break is
     * optional.
     *
     * @param endingLineBreak {@code true} to add a newline at the end of the file, {@code false} otherwise.
     * @return This configuration object, for chaining.
     */
    public CsvWriteConfig setEndingLineBreak(final boolean endingLineBreak) {
        this.endingLineBreak = endingLineBreak;
        return this;
    }

}
