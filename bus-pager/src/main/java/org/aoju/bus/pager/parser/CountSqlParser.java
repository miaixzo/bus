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
package org.aoju.bus.pager.parser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.*;

/**
 * sql解析类,提供更智能的count查询sql
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CountSqlParser {

    public static final String KEEP_ORDERBY = "/*keep orderby*/";
    private static final Alias TABLE_ALIAS;
    /**
     * 聚合函数,以下列函数开头的都认为是聚合函数
     */
    private static final Set<String> AGGREGATE_FUNCTIONS = new HashSet<>(Arrays.asList(
            ("APPROX_COUNT_DISTINCT," +
                    "ARRAY_AGG," +
                    "AVG," +
                    "BIT_," +
                    //"BIT_AND," +
                    //"BIT_OR," +
                    //"BIT_XOR," +
                    "BOOL_," +
                    //"BOOL_AND," +
                    //"BOOL_OR," +
                    "CHECKSUM_AGG," +
                    "COLLECT," +
                    "CORR," +
                    //"CORR_," +
                    //"CORRELATION," +
                    "COUNT," +
                    //"COUNT_BIG," +
                    "COVAR," +
                    //"COVAR_POP," +
                    //"COVAR_SAMP," +
                    //"COVARIANCE," +
                    //"COVARIANCE_SAMP," +
                    "CUME_DIST," +
                    "DENSE_RANK," +
                    "EVERY," +
                    "FIRST," +
                    "GROUP," +
                    //"GROUP_CONCAT," +
                    //"GROUP_ID," +
                    //"GROUPING," +
                    //"GROUPING," +
                    //"GROUPING_ID," +
                    "JSON_," +
                    //"JSON_AGG," +
                    //"JSON_ARRAYAGG," +
                    //"JSON_OBJECT_AGG," +
                    //"JSON_OBJECTAGG," +
                    //"JSONB_AGG," +
                    //"JSONB_OBJECT_AGG," +
                    "LAST," +
                    "LISTAGG," +
                    "MAX," +
                    "MEDIAN," +
                    "MIN," +
                    "PERCENT_," +
                    //"PERCENT_RANK," +
                    //"PERCENTILE_CONT," +
                    //"PERCENTILE_DISC," +
                    "RANK," +
                    "REGR_," +
                    "SELECTIVITY," +
                    "STATS_," +
                    //"STATS_BINOMIAL_TEST," +
                    //"STATS_CROSSTAB," +
                    //"STATS_F_TEST," +
                    //"STATS_KS_TEST," +
                    //"STATS_MODE," +
                    //"STATS_MW_TEST," +
                    //"STATS_ONE_WAY_ANOVA," +
                    //"STATS_T_TEST_*," +
                    //"STATS_WSR_TEST," +
                    "STD," +
                    //"STDDEV," +
                    //"STDDEV_POP," +
                    //"STDDEV_SAMP," +
                    //"STDDEV_SAMP," +
                    //"STDEV," +
                    //"STDEVP," +
                    "STRING_AGG," +
                    "SUM," +
                    "SYS_OP_ZONE_ID," +
                    "SYS_XMLAGG," +
                    "VAR," +
                    //"VAR_POP," +
                    //"VAR_SAMP," +
                    //"VARIANCE," +
                    //"VARIANCE_SAMP," +
                    //"VARP," +
                    "XMLAGG").split(Symbol.COMMA)));

    static {
        TABLE_ALIAS = new Alias("table_count");
        TABLE_ALIAS.setUseAs(false);
    }

    // <editor-fold desc="聚合函数">
    private final Set<String> skipFunctions = Collections.synchronizedSet(new HashSet<>());
    // </editor-fold>
    private final Set<String> falseFunctions = Collections.synchronizedSet(new HashSet<>());

    private final JSqlParser jSqlParser;

    public CountSqlParser() {
        this.jSqlParser = JSqlParser.DEFAULT;
    }

    public CountSqlParser(JSqlParser jSqlParser) {
        this.jSqlParser = jSqlParser;
    }

    /**
     * 添加到聚合函数,可以是逗号隔开的多个函数前缀
     *
     * @param functions 函数
     */
    public static void addAggregateFunctions(String functions) {
        if (StringKit.isNotEmpty(functions)) {
            String[] funs = functions.split(Symbol.COMMA);
            for (int i = 0; i < funs.length; i++) {
                AGGREGATE_FUNCTIONS.add(funs[i].toUpperCase());
            }
        }
    }

    /**
     * 获取智能的countSql
     *
     * @param sql sql
     * @return the string
     */
    public String getSmartCountSql(String sql) {
        return getSmartCountSql(sql, Symbol.ZERO);
    }

    /**
     * 获取智能的countSql
     *
     * @param sql    sql
     * @param column 列名,默认 0
     * @return the string
     */
    public String getSmartCountSql(String sql, String column) {
        // 解析SQL
        Statement stmt;
        // 特殊sql不需要去掉order by时,使用注释前缀
        if (sql.indexOf(KEEP_ORDERBY) >= 0) {
            return getSimpleCountSql(sql, column);
        }
        try {
            stmt = jSqlParser.parse(sql);
        } catch (Throwable e) {
            // 无法解析的用一般方法返回count语句
            return getSimpleCountSql(sql, column);
        }
        Select select = (Select) stmt;
        SelectBody selectBody = select.getSelectBody();
        try {
            // 处理body-去order by
            processSelectBody(selectBody);
        } catch (Exception e) {
            // 当 sql 包含 group by 时,不去除 order by
            return getSimpleCountSql(sql, column);
        }
        // 处理with-去order by
        processWithItemsList(select.getWithItemsList());
        // 处理为count查询
        sqlToCount(select, column);
        String result = select.toString();
        if (selectBody instanceof PlainSelect) {
            Token token = ((PlainSelect) selectBody).getASTNode().jjtGetFirstToken().specialToken;
            if (token != null) {
                String hints = token.toString().trim();
                // 这里判断是否存在hint, 且result是不包含hint的
                if (hints.startsWith("/*") && hints.endsWith("*/") && !result.startsWith("/*")) {
                    result = hints + result;
                }
            }
        }
        return result;
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql 原查询sql
     * @return 返回count查询sql
     */
    public String getSimpleCountSql(final String sql) {
        return getSimpleCountSql(sql, Symbol.ZERO);
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql  原查询sql
     * @param name 名称
     * @return 返回count查询sql
     */
    public String getSimpleCountSql(final String sql, String name) {
        StringBuilder stringBuilder = new StringBuilder(sql.length() + 40);
        stringBuilder.append("select count(");
        stringBuilder.append(name);
        stringBuilder.append(") from ( \n");
        stringBuilder.append(sql);
        stringBuilder.append("\n ) tmp_count");
        return stringBuilder.toString();
    }

    /**
     * 将sql转换为count查询
     *
     * @param select 查询
     * @param name   名称
     */
    public void sqlToCount(Select select, String name) {
        SelectBody selectBody = select.getSelectBody();
        // 是否能简化count查询
        List<SelectItem> COUNT_ITEM = new ArrayList<>();
        COUNT_ITEM.add(new SelectExpressionItem(new Column("count(" + name + ")")));
        if (selectBody instanceof PlainSelect && isSimpleCount((PlainSelect) selectBody)) {
            ((PlainSelect) selectBody).setSelectItems(COUNT_ITEM);
        } else {
            PlainSelect plainSelect = new PlainSelect();
            SubSelect subSelect = new SubSelect();
            subSelect.setSelectBody(selectBody);
            subSelect.setAlias(TABLE_ALIAS);
            plainSelect.setFromItem(subSelect);
            plainSelect.setSelectItems(COUNT_ITEM);
            select.setSelectBody(plainSelect);
        }
    }

    /**
     * 是否可以用简单的count查询方式
     *
     * @param select 查询
     * @return the boolean
     */
    public boolean isSimpleCount(PlainSelect select) {
        // 包含group by的时候不可以
        if (null != select.getGroupBy()) {
            return false;
        }
        // 包含distinct的时候不可以
        if (null != select.getDistinct()) {
            return false;
        }

        // 包含having时不可以
        if (select.getHaving() != null) {
            return false;
        }

        for (SelectItem item : select.getSelectItems()) {
            //select列中包含参数的时候不可以,否则会引起参数个数错误
            if (item.toString().contains(Symbol.QUESTION_MARK)) {
                return false;
            }
            // 如果查询列中包含函数,也不可以,函数可能会聚合列
            if (item instanceof SelectExpressionItem) {
                Expression expression = ((SelectExpressionItem) item).getExpression();
                if (expression instanceof Function) {
                    String name = ((Function) expression).getName();
                    if (null != name) {
                        String NAME = name.toUpperCase();
                        if (skipFunctions.contains(NAME)) {
                            //go on
                        } else if (falseFunctions.contains(NAME)) {
                            return false;
                        } else {
                            for (String aggregateFunction : AGGREGATE_FUNCTIONS) {
                                if (NAME.startsWith(aggregateFunction)) {
                                    falseFunctions.add(NAME);
                                    return false;
                                }
                            }
                            skipFunctions.add(NAME);
                        }
                    }
                } else if (expression instanceof Parenthesis && ((SelectExpressionItem) item).getAlias() != null) {
                    // 当存在 (a+b) as c 时，c 如果出现了 order by 或者 having 中时，会找不到对应的列，
                    // 这里想要更智能，需要在整个SQL中查找别名出现的位置，暂时不考虑，直接排除
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 处理selectBody去除Order by
     *
     * @param selectBody 查询信息
     */
    public void processSelectBody(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (null != withItem.getSubSelect()) {
                processSelectBody(withItem.getSubSelect().getSelectBody());
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (null != operationList.getSelects() && operationList.getSelects().size() > 0) {
                List<SelectBody> plainSelects = operationList.getSelects();
                for (SelectBody plainSelect : plainSelects) {
                    processSelectBody(plainSelect);
                }
            }
            if (!orderByHashParameters(operationList.getOrderByElements())) {
                operationList.setOrderByElements(null);
            }
        }
    }

    /**
     * 处理PlainSelect类型的selectBody
     *
     * @param plainSelect 计划查询
     */
    public void processPlainSelect(PlainSelect plainSelect) {
        if (!orderByHashParameters(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }
        if (null != plainSelect.getFromItem()) {
            processFromItem(plainSelect.getFromItem());
        }
        if (null != plainSelect.getJoins() && plainSelect.getJoins().size() > 0) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (null != join.getRightItem()) {
                    processFromItem(join.getRightItem());
                }
            }
        }
    }

    /**
     * 处理WithItem
     *
     * @param withItemsList WithItem
     */
    public void processWithItemsList(List<WithItem> withItemsList) {
        if (null != withItemsList && withItemsList.size() > 0) {
            for (WithItem item : withItemsList) {
                if (null != item.getSubSelect()) {
                    processSelectBody(item.getSubSelect().getSelectBody());
                }
            }
        }
    }

    /**
     * 处理子查询
     *
     * @param fromItem FromItem
     */
    public void processFromItem(FromItem fromItem) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (null != subJoin.getJoinList() && subJoin.getJoinList().size() > 0) {
                for (Join join : subJoin.getJoinList()) {
                    if (null != join.getRightItem()) {
                        processFromItem(join.getRightItem());
                    }
                }
            }
            if (null != subJoin.getLeft()) {
                processFromItem(subJoin.getLeft());
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (null != subSelect.getSelectBody()) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {

        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody());
                }
            }
        }
        // Table时不用处理
    }

    /**
     * 判断Orderby是否包含参数,有参数的不能去
     *
     * @param orderByElements OrderByElement
     * @return the boolean
     */
    public boolean orderByHashParameters(List<OrderByElement> orderByElements) {
        if (null == orderByElements) {
            return false;
        }
        for (OrderByElement orderByElement : orderByElements) {
            if (orderByElement.toString().contains(Symbol.QUESTION_MARK)) {
                return true;
            }
        }
        return false;
    }

}
