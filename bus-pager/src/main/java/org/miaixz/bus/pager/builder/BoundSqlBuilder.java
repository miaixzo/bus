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
package org.miaixz.bus.pager.builder;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

/**
 * Interface for processing {@link BoundSql} objects within the pagination plugin. Implementations of this interface can
 * modify the BoundSql at different stages of query processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BoundSqlBuilder {

    /**
     * Processes the {@link BoundSql} for a specific type of SQL operation.
     *
     * @param type     the type of SQL operation (e.g., ORIGINAL, COUNT_SQL, PAGE_SQL)
     * @param boundSql the BoundSql object for the current operation
     * @param cacheKey the CacheKey associated with the current query
     * @param chain    the processor chain, allowing continuation to subsequent processors or termination by returning
     *                 the modified BoundSql
     * @return the modified BoundSql object
     */
    BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain);

    /**
     * Enumerates the types of SQL operations that can be processed by the {@link BoundSqlBuilder}.
     */
    enum Type {
        /**
         * Original SQL, executed before the pagination plugin processes it.
         */
        ORIGINAL,
        /**
         * Count SQL, executed second in the processing chain.
         */
        COUNT_SQL,
        /**
         * Page SQL, executed last in the processing chain.
         */
        PAGE_SQL
    }

    /**
     * Represents a chain of {@link BoundSqlBuilder} processors. Implementations can control whether to continue
     * execution to the next processor in the chain.
     */
    interface Chain {

        /**
         * A default chain implementation that performs no operation and simply returns the original BoundSql.
         */
        Chain DO_NOTHING = (type, boundSql, cacheKey) -> boundSql;

        /**
         * Executes the next processor in the chain or performs the final BoundSql processing.
         *
         * @param type     the type of SQL operation
         * @param boundSql the BoundSql object for the current operation
         * @param cacheKey the CacheKey associated with the current query
         * @return the processed BoundSql object
         */
        BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey);

    }

}
