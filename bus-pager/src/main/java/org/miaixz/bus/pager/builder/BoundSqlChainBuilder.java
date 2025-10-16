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

import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

/**
 * A builder for a chain of {@link BoundSqlBuilder} instances. This class allows multiple BoundSql processors to be
 * applied sequentially.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundSqlChainBuilder implements BoundSqlBuilder.Chain {

    /**
     * The original chain to delegate to if no more interceptors are present.
     */
    private final BoundSqlBuilder.Chain original;
    /**
     * The list of BoundSqlBuilder interceptors in this chain.
     */
    private final List<BoundSqlBuilder> interceptors;

    /**
     * The current index of the interceptor to be executed in the chain.
     */
    private int index = 0;
    /**
     * Flag indicating if the chain is currently executable.
     */
    private boolean executable;

    /**
     * Constructs a BoundSqlChainBuilder with an original chain and a list of interceptors.
     *
     * @param original     the original chain to delegate to
     * @param interceptors the list of BoundSqlBuilder interceptors
     */
    public BoundSqlChainBuilder(BoundSqlBuilder.Chain original, List<BoundSqlBuilder> interceptors) {
        this(original, interceptors, false);
    }

    /**
     * Private constructor for BoundSqlChainBuilder.
     *
     * @param original     the original chain to delegate to
     * @param interceptors the list of BoundSqlBuilder interceptors
     * @param executable   a flag indicating if the chain is executable
     */
    private BoundSqlChainBuilder(BoundSqlBuilder.Chain original, List<BoundSqlBuilder> interceptors,
            boolean executable) {
        this.original = original;
        this.interceptors = interceptors;
        this.executable = executable;
    }

    /**
     * Executes the BoundSql processing chain. If the chain is not yet executable, it creates a new executable chain.
     *
     * @param type     the type of SQL operation
     * @param boundSql the BoundSql object for the current operation
     * @param cacheKey the CacheKey associated with the current query
     * @return the processed BoundSql object
     */
    @Override
    public BoundSql doBoundSql(BoundSqlBuilder.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (executable) {
            return _doBoundSql(type, boundSql, cacheKey);
        } else {
            return new BoundSqlChainBuilder(original, interceptors, true).doBoundSql(type, boundSql, cacheKey);
        }
    }

    /**
     * Internal method to execute the BoundSql processing chain. It iterates through the interceptors and calls their
     * {@code boundSql} method. If all interceptors are processed, it delegates to the original chain or returns the
     * BoundSql.
     *
     * @param type     the type of SQL operation
     * @param boundSql the BoundSql object for the current operation
     * @param cacheKey the CacheKey associated with the current query
     * @return the processed BoundSql object
     */
    private BoundSql _doBoundSql(BoundSqlBuilder.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (this.interceptors == null || this.interceptors.size() == this.index) {
            return this.original != null ? this.original.doBoundSql(type, boundSql, cacheKey) : boundSql;
        } else {
            return this.interceptors.get(this.index++).boundSql(type, boundSql, cacheKey, this);
        }
    }

}
