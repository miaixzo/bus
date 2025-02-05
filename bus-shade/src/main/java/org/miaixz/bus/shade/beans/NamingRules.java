/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * 获奖java中需要的驼峰命名
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamingRules {

    /**
     * 说明:获取java类名
     *
     * @param table 表名
     * @return String
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
     * 说明:获取字段名,把"_"后面字母变大写
     *
     * @param field 字段名
     * @param named 是否为名称
     * @return String
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
     * 说明:把sql的数据类型转为java需要的类型
     *
     * @param sqlType sql类型
     * @return String java类型
     */
    public static String jdbcTypeToJavaType(String sqlType) {
        MySQLTypeConvert typeConvert = new MySQLTypeConvert();
        return typeConvert.processTypeConvert(DateType.ONLY_DATE, sqlType).getType();
    }

}
