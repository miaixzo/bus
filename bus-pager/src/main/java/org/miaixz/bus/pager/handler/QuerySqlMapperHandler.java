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
package org.miaixz.bus.pager.handler;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Handler;

/**
 * 查询 SQL 拦截处理器，继承 AbstractSqlHandler，用于解析和处理 MyBatis 查询 SQL
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QuerySqlMapperHandler extends SqlParserHandler implements Handler {

    /**
     * 是否启用 SQL 解析
     */
    private boolean enableSqlParsing = true;

    /**
     * 判断是否执行查询操作 如果返回 false，则终止查询，返回空列表
     *
     * @param executor      MyBatis 执行器
     * @param ms            映射语句
     * @param parameter     查询参数
     * @param rowBounds     分页参数
     * @param resultHandler 结果处理器
     * @param boundSql      绑定的 SQL 对象
     * @return 返回 true 表示继续执行查询，返回 false 表示终止查询
     */
    @Override
    public boolean isQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        if (!enableSqlParsing) {
            return true;
        }
        try {
            String sql = boundSql.getSql();
            if (StringKit.isBlank(sql)) {
                Logger.warn("Empty SQL detected for MappedStatement: {}", ms.getId());
                return false;
            }
            // 解析 SQL
            String parsedSql = parserSingle(sql, null);
            return true; // 可扩展逻辑，如检查全表查询
        } catch (Exception e) {
            Logger.error("Failed to parse SQL for query interception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 查询预处理，解析和修改 SQL
     *
     * @param executor      MyBatis 执行器
     * @param ms            映射语句
     * @param parameter     查询参数
     * @param rowBounds     分页参数
     * @param resultHandler 结果处理器
     * @param boundSql      绑定的 SQL 对象
     */
    @Override
    public void query(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        if (!enableSqlParsing) {
            return;
        }
        try {
            String sql = boundSql.getSql();
            if (StringKit.isBlank(sql)) {
                Logger.warn("Empty SQL detected for pre-processing in MappedStatement: {}", ms.getId());
                return;
            }
            // 解析并处理 SQL
            String parsedSql = parserSingle(sql, parameter);
            if (!sql.equals(parsedSql)) {
                // 修改 BoundSql 的 SQL
                MetaObject boundSqlMeta = getMetaObject(boundSql);
                boundSqlMeta.setValue("sql", parsedSql);
                Logger.debug("Modified SQL for MappedStatement {}: {}", ms.getId(), parsedSql);
            }
        } catch (Exception e) {
            Logger.error("Failed to process query SQL: {}", e.getMessage());
            throw new InternalException("Query SQL processing failed: " + e.getMessage(), e);
        }
    }

}