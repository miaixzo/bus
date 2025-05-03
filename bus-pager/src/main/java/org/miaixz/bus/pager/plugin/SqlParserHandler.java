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
package org.miaixz.bus.pager.plugin;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.reflection.MetaObject;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;

import java.util.List;

/**
 * 抽象 SQL 解析类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SqlParserHandler extends AbstractSqlHandler {

    /**
     * 解析 SQL 方法
     *
     * @param metaObject 元对象
     * @param sql        SQL 语句
     * @return SQL 信息
     */
    public String parser(MetaObject metaObject, String sql) {
        if (this.allowProcess(metaObject)) {
            try {
                Logger.debug("Original SQL: " + sql);
                StringBuilder sqlStringBuilder = new StringBuilder();
                Statements statements = CCJSqlParserUtil.parseStatements(sql);
                int i = 0;
                for (Statement statement : statements.getStatements()) {
                    if (null != statement) {
                        if (i++ > 0) {
                            sqlStringBuilder.append(Symbol.C_SEMICOLON);
                        }
                        sqlStringBuilder.append(this.processParser(statement));
                    }
                }
                if (sqlStringBuilder.length() > 0) {
                    return sqlStringBuilder.toString();
                }
            } catch (JSQLParserException e) {
                throw new InternalException(
                        "Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql);
            }
        }
        return null;
    }

    /**
     * 执行 SQL 解析
     *
     * @param statement Statement
     * @return SQL 信息
     */
    public String processParser(Statement statement) {
        if (statement instanceof Insert) {
            this.processInsert((Insert) statement);
        } else if (statement instanceof Select) {
            this.processSelectBody(((Select) statement).getSelectBody());
        } else if (statement instanceof Update) {
            this.processUpdate((Update) statement);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete) statement);
        }
        if (Logger.isDebugEnabled()) {
            Logger.debug("Parser SQL: " + statement.toString());
        }
        return statement.toString();
    }

    /**
     * 查询
     *
     * @param select 查询信息
     */
    public void processSelectBody(Object select) {
        if (select != null) {
            if (select instanceof WithItem) {
                WithItem<?> withItem = (WithItem<?>) select;
                Select withSelect = withItem.getSelect();
                if (withSelect != null) {
                    processSelectBody(withSelect);
                }
            } else {
                SetOperationList operationList = (SetOperationList) select;
                if (operationList.getSelects() != null && !operationList.getSelects().isEmpty()) {
                    List<Select> plainSelects = operationList.getSelects();
                    for (Select plainSelect : plainSelects) {
                        processSelectBody(plainSelect);
                    }
                }
            }
        }
    }

    /**
     * 判断是否允许执行 例如：逻辑删除只解析 delete , update 操作
     *
     * @param metaObject 元对象
     * @return true
     */
    public boolean allowProcess(MetaObject metaObject) {
        return true;
    }

    /**
     * 是否执行 SQL 解析 parser 方法
     *
     * @param metaObject 元对象
     * @param sql        SQL 语句
     * @return SQL 信息
     */
    public boolean doFilter(final MetaObject metaObject, final String sql) {
        return true;
    }

    /**
     * 新增
     *
     * @param insert 添加检查
     */
    public void processInsert(Insert insert) {

    }

    /**
     * 删除
     *
     * @param delete 删除检查
     */
    public void processDelete(Delete delete) {
        Assert.notNull(delete.getWhere(), "Prohibition of full table deletion");
    }

    /**
     * 更新
     *
     * @param update 更新检查
     */
    public void processUpdate(Update update) {
        Assert.notNull(update.getWhere(), "Prohibition of table update operation");
    }

}
