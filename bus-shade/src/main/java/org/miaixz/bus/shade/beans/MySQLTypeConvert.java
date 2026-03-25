/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.beans;

/**
 * MySQL database field type converter. This class provides logic to convert MySQL column types to appropriate Java
 * {@link ColumnType}s.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MySQLTypeConvert implements TypeConvert {

    /**
     * Processes the type conversion from a MySQL field type string to a {@link ColumnType}. It considers the specified
     * {@link DateType} for date and time fields.
     *
     * @param dateType  The strategy for converting date types (e.g., {@link DateType#ONLY_DATE},
     *                  {@link DateType#SQL_PACK}, {@link DateType#TIME_PACK}).
     * @param fieldType The MySQL database field type string (e.g., "varchar", "bigint", "datetime").
     * @return The corresponding {@link ColumnType} for the given MySQL field type.
     */
    @Override
    public ColumnType processTypeConvert(DateType dateType, String fieldType) {
        String t = fieldType.toLowerCase();
        if (t.contains("char")) {
            return MySQLColumnType.STRING;
        } else if (t.contains("bigint")) {
            return MySQLColumnType.LONG;
        } else if (t.contains("tinyint(1)")) {
            return MySQLColumnType.BOOLEAN;
        } else if (t.contains("int")) {
            return MySQLColumnType.INTEGER;
        } else if (t.contains("text")) {
            return MySQLColumnType.STRING;
        } else if (t.contains("bit")) {
            return MySQLColumnType.BOOLEAN;
        } else if (t.contains("decimal")) {
            return MySQLColumnType.BIG_DECIMAL;
        } else if (t.contains("clob")) {
            return MySQLColumnType.CLOB;
        } else if (t.contains("blob")) {
            return MySQLColumnType.BLOB;
        } else if (t.contains("binary")) {
            return MySQLColumnType.BYTE_ARRAY;
        } else if (t.contains("float")) {
            return MySQLColumnType.FLOAT;
        } else if (t.contains("double")) {
            return MySQLColumnType.DOUBLE;
        } else if (t.contains("json") || t.contains("enum")) {
            return MySQLColumnType.STRING;
        } else if (t.contains("date") || t.contains("time") || t.contains("year")) {
            switch (dateType) {
                case ONLY_DATE:
                    return MySQLColumnType.DATE;

                case SQL_PACK:
                    switch (t) {
                        case "date":
                            return MySQLColumnType.DATE_SQL;

                        case "time":
                            return MySQLColumnType.TIME;

                        case "year":
                            return MySQLColumnType.DATE_SQL;

                        default:
                            return MySQLColumnType.TIMESTAMP;
                    }
                case TIME_PACK:
                    switch (t) {
                        case "date":
                            return MySQLColumnType.LOCAL_DATE;

                        case "time":
                            return MySQLColumnType.LOCAL_TIME;

                        case "year":
                            return MySQLColumnType.YEAR;

                        default:
                            return MySQLColumnType.LOCAL_DATE_TIME;
                    }
            }
        }
        return MySQLColumnType.STRING;
    }

}
