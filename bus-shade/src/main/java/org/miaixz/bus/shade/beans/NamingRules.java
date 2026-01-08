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
package org.miaixz.bus.shade.beans;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Provides utility methods for converting database naming conventions to Java-style camel case naming. This includes
 * converting table names to class names and field names to Java field names.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamingRules {

    /**
     * Converts a database table name to a Java class name, applying camel case convention. The first letter of the
     * resulting class name will be capitalized.
     *
     * @param table The database table name (e.g., "user_info").
     * @return The corresponding Java class name (e.g., "UserInfo").
     */
    public static String getClassName(String table) {
        table = changeToJavaFiled(table, true);
        StringBuilder sbuilder = new StringBuilder();
        char[] cs = table.toCharArray();
        cs[0] -= Normal._32;
        sbuilder.append(String.valueOf(cs));
        return sbuilder.toString();
    }

    /**
     * Converts a database field name to a Java field name, applying camel case convention. If {@code named} is true, it
     * converts underscores to camel case (e.g., "user_name" to "userName"). If {@code named} is false, the field name
     * is returned as is.
     *
     * @param field The database field name (e.g., "user_name").
     * @param named A boolean indicating whether to apply naming conversion. If false, the original field name is
     *              returned.
     * @return The corresponding Java field name.
     */
    public static String changeToJavaFiled(String field, boolean named) {
        if (!named) {
            return field;
        }
        String[] fields = field.split(Symbol.UNDERLINE);
        StringBuilder sbuilder = new StringBuilder(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            char[] cs = fields[i].toCharArray();
            cs[0] -= Normal._32;
            sbuilder.append(String.valueOf(cs));
        }
        return sbuilder.toString();
    }

    /**
     * Converts a SQL data type to its corresponding Java type. This method uses {@link MySQLTypeConvert} with
     * {@link DateType#ONLY_DATE} strategy for date types.
     *
     * @param sqlType The SQL data type string (e.g., "varchar", "int", "datetime").
     * @return The corresponding Java type name (e.g., "String", "Integer", "Date").
     */
    public static String jdbcTypeToJavaType(String sqlType) {
        MySQLTypeConvert typeConvert = new MySQLTypeConvert();
        return typeConvert.processTypeConvert(DateType.ONLY_DATE, sqlType).getType();
    }

}
