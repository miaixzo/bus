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

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;

/**
 * Interface for automatically obtaining a database dialect. Implementations of this interface are responsible for
 * determining the appropriate dialect based on the provided MappedStatement, DataSource, and properties.
 *
 * @param <K> the type of the cache key used for storing dialect instances
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AutoDialect<K> {

    /**
     * Retrieves the key used for caching the return value of the {@link #extractDialect} method. If this method returns
     * null, the dialect will not be cached. If a key is returned, the system first checks if it already exists. If not,
     * {@link #extractDialect} is called, and the result is then cached.
     *
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties
     * @return the cache key for the dialect, or null if caching is not desired
     */
    K extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties);

    /**
     * Extracts and returns the appropriate {@link AbstractPaging} dialect. This method is responsible for creating or
     * retrieving the dialect instance based on the provided information.
     *
     * @param dialectKey the cache key for the dialect, as returned by {@link #extractDialectKey}
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties
     * @return an instance of {@link AbstractPaging} representing the determined dialect
     */
    AbstractPaging extractDialect(K dialectKey, MappedStatement ms, DataSource dataSource, Properties properties);

}
