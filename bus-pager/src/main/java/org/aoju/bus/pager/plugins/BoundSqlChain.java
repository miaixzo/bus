/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.pager.plugins;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundSqlChain implements BoundSqlHandler.Chain {

    private final BoundSqlHandler.Chain original;
    private final List<BoundSqlHandler> interceptors;

    private int index = 0;
    private boolean executable;

    public BoundSqlChain(BoundSqlHandler.Chain original, List<BoundSqlHandler> interceptors) {
        this(original, interceptors, false);
    }

    private BoundSqlChain(BoundSqlHandler.Chain original, List<BoundSqlHandler> interceptors, boolean executable) {
        this.original = original;
        this.interceptors = interceptors;
        this.executable = executable;
    }

    @Override
    public BoundSql doBoundSql(BoundSqlHandler.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (executable) {
            return _doBoundSql(type, boundSql, cacheKey);
        } else {
            return new BoundSqlChain(original, interceptors, true).doBoundSql(type, boundSql, cacheKey);
        }
    }

    private BoundSql _doBoundSql(BoundSqlHandler.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (this.interceptors == null || this.interceptors.size() == this.index) {
            return this.original != null ? this.original.doBoundSql(type, boundSql, cacheKey) : boundSql;
        } else {
            return this.interceptors.get(this.index++).boundSql(type, boundSql, cacheKey, this);
        }
    }

}
