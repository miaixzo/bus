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
