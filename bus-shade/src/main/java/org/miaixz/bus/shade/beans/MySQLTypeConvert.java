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
package org.miaixz.bus.shade.beans;

/**
 * Copyright: Copyright (c) 2019
 * <p>
 * MYSQL 数据库字段类型转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MySQLTypeConvert implements TypeConvert {

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
