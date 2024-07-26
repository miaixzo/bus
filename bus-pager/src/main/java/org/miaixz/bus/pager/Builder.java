/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.parser.SqlParser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 公共方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * SQL语法检查正则：符合两个关键字（有先后顺序）才算匹配
     */
    private static final Pattern SQL_SYNTAX_PATTERN = Pattern.compile(
            "(insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set)"
                    + "\\s+.*(into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or)|(select\\s*\\*\\s*from\\s+)",
            Pattern.CASE_INSENSITIVE);
    /**
     * 使用'、;或注释截断SQL检查正则
     */
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("'.*(or|union|--|#|/*|;)",
            Pattern.CASE_INSENSITIVE);

    private static final SqlParser SQL_PARSER;

    static {
        SqlParser temp = null;
        ServiceLoader<SqlParser> loader = ServiceLoader.load(SqlParser.class);
        for (SqlParser sqlParser : loader) {
            temp = sqlParser;
            break;
        }
        if (temp == null) {
            temp = SqlParser.DEFAULT;
        }
        SQL_PARSER = temp;
    }

    public static Statement parse(String statementReader) {
        try {
            return SQL_PARSER.parse(statementReader);
        } catch (JSQLParserException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查参数是否存在 SQL 注入
     *
     * @param value 检查参数
     * @return true 非法 false 合法
     */
    public static boolean check(String value) {
        if (value == null) {
            return false;
        }
        // 不允许使用任何函数（不能出现括号），否则无法检测后面这个注入 order by id,if(1=2,1,(sleep(100)));
        return value.contains(Symbol.PARENTHESE_LEFT) || SQL_COMMENT_PATTERN.matcher(value).find()
                || SQL_SYNTAX_PATTERN.matcher(value).find();
    }

    /**
     * 支持配置和SPI，优先级：配置类 > SPI > 默认值
     *
     * @param classStr        配置串，可空
     * @param spi             SPI 接口
     * @param properties      配置属性
     * @param defaultSupplier 默认值
     */
    public static <T> T newInstance(String classStr, Class<T> spi, Properties properties, Supplier<T> defaultSupplier) {
        if (StringKit.isNotEmpty(classStr)) {
            try {
                Class<?> cls = Class.forName(classStr);
                return (T) newInstance(cls, properties);
            } catch (Exception ignored) {
            }
        }
        T result = null;
        if (spi != null) {
            ServiceLoader<T> loader = ServiceLoader.load(spi);
            for (T t : loader) {
                result = t;
                break;
            }
        }
        if (result == null) {
            result = defaultSupplier.get();
        }
        if (result instanceof Property) {
            ((Property) result).setProperties(properties);
        }
        return result;
    }

    public static <T> T newInstance(String classStr, Properties properties) {
        try {
            Class<?> cls = Class.forName(classStr);
            return (T) newInstance(cls, properties);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

    public static <T> T newInstance(Class<T> cls, Properties properties) {
        try {
            T instance = cls.newInstance();
            if (instance instanceof Property) {
                ((Property) instance).setProperties(properties);
            }
            return instance;
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

    /**
     * 当前方法堆栈信息
     */
    public static String current() {
        Exception exception = new Exception("Stack information when setting pagination parameters");
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
