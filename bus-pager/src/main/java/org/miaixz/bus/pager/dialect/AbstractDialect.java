/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.parser.CountSqlParser;
import org.miaixz.bus.pager.parser.OrderBySqlParser;
import org.miaixz.bus.pager.parser.defaults.DefaultCountSqlParser;
import org.miaixz.bus.pager.parser.defaults.DefaultOrderBySqlParser;

/**
 * 基于 CountSqlParser 的智能 Count 查询
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * 处理SQL
     */
    protected CountSqlParser countSqlParser;
    protected OrderBySqlParser orderBySqlParser;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    @Override
    public void setProperties(Properties properties) {
        this.countSqlParser = Builder.newInstance(properties.getProperty("countSqlParser"), CountSqlParser.class,
                properties, DefaultCountSqlParser::new);
        this.orderBySqlParser = Builder.newInstance(properties.getProperty("orderBySqlParser"), OrderBySqlParser.class,
                properties, DefaultOrderBySqlParser::new);
    }

}
