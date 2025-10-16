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
package org.miaixz.bus.pager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.parsing.SqlParser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

/**
 * Utility class for common operations related to pagination, such as SQL parsing and instance creation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * The SQL parser instance used for parsing SQL statements. It is initialized via ServiceLoader or defaults to
     * {@link SqlParser#DEFAULT}.
     */
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

    /**
     * Parses a SQL statement string into a {@link Statement} object.
     *
     * @param statementReader the SQL statement string to parse
     * @return the parsed Statement object
     * @throws RuntimeException if a {@link JSQLParserException} or {@link ParseException} occurs during parsing
     */
    public static Statement parse(String statementReader) {
        try {
            return SQL_PARSER.parse(statementReader);
        } catch (JSQLParserException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new instance of a class, supporting configuration and Service Provider Interface (SPI). The priority
     * for instance creation is: configured class > SPI > default value.
     *
     * @param classStr        the fully qualified class name string, can be null or empty
     * @param spi             the SPI interface class
     * @param properties      the properties to set on the instance if it implements {@link Property}
     * @param defaultSupplier a supplier for the default instance if no other instance can be created
     * @param <T>             the type of the instance to create
     * @return a new instance of the specified type
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

    /**
     * Creates a new instance of a class from its fully qualified name.
     *
     * @param classStr   the fully qualified class name string
     * @param properties the properties to set on the instance if it implements {@link Property}
     * @param <T>        the type of the instance to create
     * @return a new instance of the specified type
     * @throws PageException if an error occurs during instance creation
     */
    public static <T> T newInstance(String classStr, Properties properties) {
        try {
            Class<?> cls = Class.forName(classStr);
            return (T) newInstance(cls, properties);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

    /**
     * Creates a new instance of a class from its {@link Class} object.
     *
     * @param cls        the {@link Class} object of the type to instantiate
     * @param properties the properties to set on the instance if it implements {@link Property}
     * @param <T>        the type of the instance to create
     * @return a new instance of the specified type
     * @throws PageException if an error occurs during instance creation
     */
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
     * Retrieves the current method stack trace information. This can be used to debug where a {@link Page} object was
     * created.
     *
     * @return a string containing the current stack trace information
     */
    public static String current() {
        Exception exception = new Exception("Stack information when setting pagination parameters");
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
