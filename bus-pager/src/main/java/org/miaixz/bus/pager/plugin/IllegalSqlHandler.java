/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.logger.Logger;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 由于开发人员水平参差不齐，即使订了开发规范很多人也不遵守
 * <p>
 * SQL是影响系统性能最重要的因素，所以拦截掉垃圾SQL语句
 * </p>
 * <p>
 * 拦截SQL类型的场景
 * </p>
 * <p>
 * 1.必须使用到索引，包含left jion连接字段，符合索引最左原则
 * </p>
 * <p>
 * 必须使用索引好处，
 * </p>
 * <p>
 * 1.1 如果因为动态SQL，bug导致update的where条件没有带上，全表更新上万条数据
 * </p>
 * <p>
 * 1.2 如果检查到使用了索引，SQL性能基本不会太差
 * </p>
 * <p>
 * 2.SQL尽量单表执行，有查询left jion的语句，必须在注释里面允许该SQL运行，否则会被拦截
 * </p>
 * <p>
 * SQL尽量单表执行的好处
 * </p>
 * <p>
 * 2.1 查询条件简单、易于开理解和维护；
 * </p>
 * <p>
 * 2.2 扩展性极强；(可为分库分表做准备)
 * </p>
 * <p>
 * 2.3 缓存利用率高；
 * </p>
 * <p>
 * 2.在字段上使用函数
 * </p>
 * <p>
 * 3.where条件为空
 * </p>
 * <p>
 * 4.where条件使用了 !=
 * </p>
 * <p>
 * 5.where条件使用了 not 关键字
 * </p>
 * <p>
 * 6.where条件使用了 or 关键字
 * </p>
 * <p>
 * 7.where条件使用了 使用子查询
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class IllegalSqlHandler extends SqlParserHandler implements Interceptor {

    /**
     * 缓存验证结果，提高性能
     */
    private static final Set<String> cacheValidResult = new HashSet<>();

    /**
     * 缓存表的索引信息
     */
    private static final Map<String, List<IndexInfo>> indexInfoMap = new ConcurrentHashMap<>();

    /**
     * 验证expression对象是不是 or、not等等
     *
     * @param expression ignore
     */
    private static void validExpression(Expression expression) {
        // where条件使用了 or 关键字
        if (expression instanceof OrExpression) {
            OrExpression orExpression = (OrExpression) expression;
            throw new InternalException("非法SQL，where条件中不能使用【or】关键字，错误or信息：" + orExpression.toString());
        } else if (expression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) expression;
            throw new InternalException("非法SQL，where条件中不能使用【!=】关键字，错误!=信息：" + notEqualsTo.toString());
        } else if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            if (binaryExpression.getLeftExpression() instanceof Function) {
                Function function = (Function) binaryExpression.getLeftExpression();
                throw new InternalException("非法SQL，where条件中不能使用数据库函数，错误函数信息：" + function.toString());
            }
            if (binaryExpression.getRightExpression() instanceof ParenthesedSelect) {
                ParenthesedSelect subSelect = (ParenthesedSelect) binaryExpression.getRightExpression();
                throw new InternalException("非法SQL，where条件中不能使用子查询，错误子查询SQL信息：" + subSelect.toString());
            }
        } else if (expression instanceof InExpression) {
            InExpression inExpression = (InExpression) expression;
            if (inExpression.getRightExpression() instanceof ParenthesedSelect) {
                ParenthesedSelect subSelect = (ParenthesedSelect) inExpression.getRightExpression();
                throw new InternalException("非法SQL，where条件中不能使用子查询，错误子查询SQL信息：" + subSelect.toString());
            }
        }

    }

    /**
     * 如果SQL用了 left Join，验证是否有or、not等等，并且验证是否使用了索引
     *
     * @param joins      ignore
     * @param table      ignore
     * @param connection ignore
     */
    private static void validJoins(List<Join> joins, Table table, Connection connection) {
        // 允许执行join，验证jion是否使用索引等等
        if (null != joins) {
            for (Join join : joins) {
                Table rightTable = (Table) join.getRightItem();
                Expression expression = join.getOnExpression();
                validWhere(expression, table, rightTable, connection);
            }
        }
    }

    /**
     * 检查是否使用索引
     *
     * @param table      ignore
     * @param columnName ignore
     * @param connection ignore
     */
    private static void validUseIndex(Table table, String columnName, Connection connection) {
        // 是否使用索引
        boolean useIndexFlag = false;

        String tableInfo = table.getName();
        // 表存在的索引
        String dbName = null;
        String tableName;
        String[] tableArray = tableInfo.split("\\.");
        if (tableArray.length == 1) {
            tableName = tableArray[0];
        } else {
            dbName = tableArray[0];
            tableName = tableArray[1];
        }
        List<IndexInfo> indexInfos = getIndexInfos(dbName, tableName, connection);
        for (IndexInfo indexInfo : indexInfos) {
            if (Objects.equals(columnName, indexInfo.getColumnName())) {
                useIndexFlag = true;
                break;
            }
        }
        if (!useIndexFlag) {
            throw new InternalException("非法SQL，SQL未使用到索引, table:" + table + ", columnName:" + columnName);
        }
    }

    /**
     * 验证where条件的字段，是否有not、or等等，并且where的第一个字段，必须使用索引
     *
     * @param expression ignore
     * @param table      ignore
     * @param connection ignore
     */
    private static void validWhere(Expression expression, Table table, Connection connection) {
        validWhere(expression, table, null, connection);
    }

    /**
     * 验证where条件的字段，是否有not、or等等，并且where的第一个字段，必须使用索引
     *
     * @param expression ignore
     * @param table      ignore
     * @param joinTable  ignore
     * @param connection ignore
     */
    private static void validWhere(Expression expression, Table table, Table joinTable, Connection connection) {
        validExpression(expression);
        if (expression instanceof BinaryExpression) {
            // 获得左边表达式
            Expression leftExpression = ((BinaryExpression) expression).getLeftExpression();
            validExpression(leftExpression);

            // 如果左边表达式为Column对象，则直接获得列名
            if (leftExpression instanceof Column) {
                Expression rightExpression = ((BinaryExpression) expression).getRightExpression();
                if (null != joinTable && rightExpression instanceof Column) {
                    if (Objects.equals(((Column) rightExpression).getTable().getName(), table.getAlias().getName())) {
                        validUseIndex(table, ((Column) rightExpression).getColumnName(), connection);
                        validUseIndex(joinTable, ((Column) leftExpression).getColumnName(), connection);
                    } else {
                        validUseIndex(joinTable, ((Column) rightExpression).getColumnName(), connection);
                        validUseIndex(table, ((Column) leftExpression).getColumnName(), connection);
                    }
                } else {
                    // 获得列名
                    validUseIndex(table, ((Column) leftExpression).getColumnName(), connection);
                }
            }
            // 如果BinaryExpression，进行迭代
            else if (leftExpression instanceof BinaryExpression) {
                validWhere(leftExpression, table, joinTable, connection);
            }

            // 获得右边表达式，并分解
            Expression rightExpression = ((BinaryExpression) expression).getRightExpression();
            validExpression(rightExpression);
        }
    }

    /**
     * 得到表的索引信息
     *
     * @param dbName    ignore
     * @param tableName ignore
     * @param conn      ignore
     * @return ignore
     */
    private static List<IndexInfo> getIndexInfos(String dbName, String tableName, Connection conn) {
        return getIndexInfos(null, dbName, tableName, conn);
    }

    /**
     * 得到表的索引信息
     *
     * @param key       ignore
     * @param dbName    ignore
     * @param tableName ignore
     * @param conn      ignore
     * @return ignore
     */
    private static List<IndexInfo> getIndexInfos(String key, String dbName, String tableName, Connection conn) {
        List<IndexInfo> indexInfos = null;
        if (StringKit.isNotBlank(key)) {
            indexInfos = indexInfoMap.get(key);
        }
        if (null == indexInfos || indexInfos.isEmpty()) {
            ResultSet rs;
            try {
                DatabaseMetaData metadata = conn.getMetaData();
                String catalog = StringKit.isBlank(dbName) ? conn.getCatalog() : dbName;
                String schema = StringKit.isBlank(dbName) ? conn.getSchema() : dbName;
                rs = metadata.getIndexInfo(catalog, schema, tableName, false, true);
                indexInfos = new ArrayList<>();
                while (rs.next()) {
                    // 索引中的列序列号等于1，才有效
                    if (Objects.equals(rs.getString(8), "1")) {
                        IndexInfo indexInfo = new IndexInfo();
                        indexInfo.setDbName(rs.getString(1));
                        indexInfo.setTableName(rs.getString(3));
                        indexInfo.setColumnName(rs.getString(9));
                        indexInfos.add(indexInfo);
                    }
                }
                if (StringKit.isNotBlank(key)) {
                    indexInfoMap.put(key, indexInfos);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return indexInfos;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        // 如果是insert操作， 或者 @SqlParser(filter = true) 跳过该方法解析 ， 不进行验证
        MappedStatement mappedStatement = getMappedStatement(metaObject);
        if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType()) || getSqlParserInfo(metaObject)) {
            return invocation.proceed();
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL);
        String originalSql = boundSql.getSql();
        Logger.debug("Check for SQL : " + originalSql);

        String md5Base64 = Base64.getEncoder()
                .encodeToString(Builder.md5().digest(originalSql.getBytes(Charset.UTF_8)));

        if (cacheValidResult.contains(md5Base64)) {
            Logger.debug("The SQL has been checked : " + originalSql);
            return invocation.proceed();
        }
        Connection connection = (Connection) invocation.getArgs()[0];
        Statement statement = CCJSqlParserUtil.parse(originalSql);
        Expression where = null;
        Table table = null;
        List<Join> joins = null;
        if (statement instanceof Select) {
            PlainSelect plainSelect = (PlainSelect) ((Select) statement).getSelectBody();
            where = plainSelect.getWhere();
            table = (Table) plainSelect.getFromItem();
            joins = plainSelect.getJoins();
        } else if (statement instanceof Update) {
            Update update = (Update) statement;
            where = update.getWhere();
            table = update.getTable();
            joins = update.getJoins();
        } else if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            where = delete.getWhere();
            table = delete.getTable();
            joins = delete.getJoins();
        }
        // where条件不能为空
        if (null == where) {
            throw new InternalException("非法SQL，必须要有where条件");
        }
        validWhere(where, table, connection);
        validJoins(joins, table, connection);
        // 缓存验证结果
        cacheValidResult.add(md5Base64);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object object) {
        if (object instanceof StatementHandler) {
            return Plugin.wrap(object, this);
        }
        return object;
    }

    /**
     * 索引对象
     */
    private static class IndexInfo {

        private String dbName;

        private String tableName;

        private String columnName;

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }

}
