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
package org.miaixz.bus.shade.screw;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.screw.execute.ProduceExecute;

/**
 * Default constants and utility methods for document generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * The percent sign symbol (%).
     */
    public static final String PERCENT_SIGN = Symbol.PERCENT;
    /**
     * A constant string indicating that a feature is not yet supported.
     */
    public static final String NOT_SUPPORTED = "Not supported yet!";

    /**
     * The default locale string ("zh_CN").
     */
    public static final String DEFAULT_LOCALE = "zh_CN";
    /**
     * A constant string for the Mac operating system.
     */
    public static final String MAC = "Mac";
    /**
     * A constant string for the Windows operating system.
     */
    public static final String WINDOWS = "Windows";
    /**
     * A constant string representing zero decimal digits ("0").
     */
    public static final String ZERO_DECIMAL_DIGITS = "0";
    /**
     * The default description for the database design document.
     */
    public static final String DESCRIPTION = "数据库设计文档";
    /**
     * The connection property key for MySQL to use the Information Schema.
     */
    public static final String USE_INFORMATION_SCHEMA = "useInformationSchema";
    /**
     * The connection property key for Oracle to retrieve remarks.
     */
    public static final String ORACLE_REMARKS = "remarks";
    /**
     * The string representation of zero ("0").
     */
    public static final String ZERO = "0";
    /**
     * The string "N", typically representing "No".
     */
    public static final String N = "N";
    /**
     * The string "Y", typically representing "Yes".
     */
    public static final String Y = "Y";

    /**
     * Creates and generates the database structure documentation file.
     *
     * @param config The configuration object containing all necessary parameters for document generation.
     */
    public static void createFile(Config config) {
        new ProduceExecute(config).execute();
    }

}
