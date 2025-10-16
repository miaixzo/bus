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
package org.miaixz.bus.pager.dialect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.pager.binding.PageAutoDialect;

/**
 * Abstract base class for auto-detecting database dialects from a {@link DataSource}. This class provides a default
 * implementation that can extract the JDBC URL from specific DataSource types, such as HikariCP, to determine the
 * dialect.
 *
 * @param <Ds> The specific {@link DataSource} implementation type this dialect handler supports.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractAutoDialect<Ds extends DataSource> implements AutoDialect<String> {

    /**
     * The class of the specific DataSource implementation.
     */
    protected Class dataSourceClass;

    /**
     * Constructs an AbstractAutoDialect instance and determines the specific DataSource class from the generic type
     * parameter.
     */
    public AbstractAutoDialect() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        dataSourceClass = (Class) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    }

    /**
     * Retrieves the JDBC URL from the specific DataSource instance.
     *
     * @param ds the DataSource instance.
     * @return the JDBC URL string.
     */
    public abstract String getJdbcUrl(Ds ds);

    /**
     * Extracts the dialect key (JDBC URL) if the provided DataSource matches the supported type.
     *
     * @param ms         the MappedStatement being executed.
     * @param dataSource the DataSource associated with the MappedStatement.
     * @param properties the configuration properties.
     * @return the JDBC URL as the dialect key if the DataSource is of the supported type, otherwise null.
     */
    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        if (dataSourceClass.isInstance(dataSource)) {
            return getJdbcUrl((Ds) dataSource);
        }
        return null;
    }

    /**
     * Extracts and returns the appropriate {@link AbstractPaging} dialect based on the dialect key (JDBC URL).
     *
     * @param dialectKey the dialect key (JDBC URL).
     * @param ms         the MappedStatement being executed.
     * @param dataSource the DataSource associated with the MappedStatement.
     * @param properties the configuration properties.
     * @return an instance of {@link AbstractPaging} representing the determined dialect.
     */
    @Override
    public AbstractPaging extractDialect(
            String dialectKey,
            MappedStatement ms,
            DataSource dataSource,
            Properties properties) {
        String dialect = PageAutoDialect.fromJdbcUrl(dialectKey);
        return PageAutoDialect.instanceDialect(dialect, properties);
    }

}
