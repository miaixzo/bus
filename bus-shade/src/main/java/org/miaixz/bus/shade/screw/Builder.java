/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
