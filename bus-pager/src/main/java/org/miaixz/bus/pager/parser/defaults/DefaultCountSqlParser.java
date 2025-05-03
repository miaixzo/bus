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
package org.miaixz.bus.pager.parser.defaults;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.builtin.PageMethod;
import org.miaixz.bus.pager.parser.CountSqlParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * sql解析类，提供更智能的count查询sql
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultCountSqlParser implements CountSqlParser {

    public static final String KEEP_ORDERBY = "/*keep orderby*/";
    protected static final Alias TABLE_ALIAS;

    static {
        TABLE_ALIAS = new Alias("table_count");
        TABLE_ALIAS.setUseAs(false);
    }

    // 使用同步集合存储函数名，确保线程安全
    private final Set<String> skipFunctions = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> falseFunctions = Collections.synchronizedSet(new HashSet<>());

    /**
     * 获取智能的 COUNT SQL，自动检测是否需要保留 ORDER BY。 优化字符串处理和解析逻辑，减少性能开销。
     *
     * @param sql         原始 SQL 查询
     * @param countColumn COUNT 列名，默认为 "0"
     * @return 优化的 COUNT SQL
     */
    @Override
    public String getSmartCountSql(String sql, String countColumn) {
        // 快速检查是否需要保留 ORDER BY
        if (sql.contains(KEEP_ORDERBY) || keepOrderBy()) {
            return getSimpleCountSql(sql, countColumn);
        }

        try {
            // 解析 SQL
            Statement stmt = Builder.parse(sql);
            Select select = (Select) stmt;

            // 处理 SELECT 主体，移除不必要的 ORDER BY
            processSelect(select);

            // 优化 WITH 子句处理
            processWithItemsList(select.getWithItemsList());

            // 转换为 COUNT 查询
            Select countSelect = sqlToCount(select, countColumn);

            // 保留原始 SQL 的注释（如 hints）
            String result = countSelect.toString();
            if (select instanceof PlainSelect && select.getASTNode() != null) {
                Token token = select.getASTNode().jjtGetFirstToken();
                if (token != null && token.specialToken != null) {
                    String hints = token.specialToken.toString().trim();
                    if (hints.startsWith("/*") && hints.endsWith("*/") && !result.startsWith("/*")) {
                        result = hints + " " + result;
                    }
                }
            }
            return result;

        } catch (Throwable e) {
            // 解析失败时回退到简单 COUNT
            return getSimpleCountSql(sql, countColumn);
        }
    }

    /**
     * 获取简单的 COUNT SQL，适用于无法解析或复杂场景。
     *
     * @param sql 原查询 SQL
     * @return 简单的 COUNT SQL
     */
    public String getSimpleCountSql(final String sql) {
        return getSimpleCountSql(sql, "0");
    }

    /**
     * 获取简单的 COUNT SQL，指定 COUNT 列名。 优化 StringBuilder 容量预估，减少扩容。
     *
     * @param sql  原查询 SQL
     * @param name COUNT 列名
     * @return 简单的 COUNT SQL
     */
    public String getSimpleCountSql(final String sql, String name) {
        StringBuilder stringBuilder = new StringBuilder(sql.length() + 50);
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
     * @param select 原查询sql
     * @param name   名称
     * @return 返回count查询sql
     */
    public Select sqlToCount(Select select, String name) {
        List<SelectItem<?>> countItem = Collections.singletonList(new SelectItem<>(new Column("COUNT(" + name + ")")));

        if (select instanceof PlainSelect && isSimpleCount((PlainSelect) select)) {
            // 简单场景直接替换 SELECT 项
            ((PlainSelect) select).setSelectItems(countItem);
            return select;
        }

        // 复杂场景，包装为子查询
        PlainSelect plainSelect = new PlainSelect();
        ParenthesedSelect subSelect = new ParenthesedSelect();
        subSelect.setSelect(select);
        subSelect.setAlias(TABLE_ALIAS);
        plainSelect.setFromItem(subSelect);
        plainSelect.setSelectItems(countItem);

        // 转移 WITH 子句
        if (select.getWithItemsList() != null) {
            plainSelect.setWithItemsList(select.getWithItemsList());
            select.setWithItemsList(null);
        }

        return plainSelect;
    }

    /**
     * 判断是否可以使用简单的 COUNT 查询方式。 避免使用过时的 Parenthesis 类，改用 ParenthesedExpressionList 或通用 Expression 处理。
     *
     * @param select 查询
     * @return 是否为简单 COUNT
     */
    public boolean isSimpleCount(PlainSelect select) {
        // GROUP BY、DISTINCT 或 HAVING 存在时无法简化
        if (select.getGroupBy() != null || select.getDistinct() != null || select.getHaving() != null) {
            return false;
        }

        for (SelectItem<?> item : select.getSelectItems()) {
            String itemStr = item.toString();
            // 包含参数（?）时无法简化
            if (itemStr.contains("?")) {
                return false;
            }

            Expression expression = item.getExpression();
            if (expression instanceof Function) {
                String name = ((Function) expression).getName();
                if (name != null) {
                    String upperName = name.toUpperCase();
                    if (skipFunctions.contains(upperName)) {
                        continue;
                    }
                    if (falseFunctions.contains(upperName)) {
                        return false;
                    }
                    // 检查是否为聚合函数
                    for (String aggFunc : AGGREGATE_FUNCTIONS) {
                        if (upperName.startsWith(aggFunc)) {
                            falseFunctions.add(upperName);
                            return false;
                        }
                    }
                    skipFunctions.add(upperName);
                }
            } else if (expression instanceof ParenthesedExpressionList && item.getAlias() != null) {
                // 带别名的括号表达式列表可能在 ORDER BY 或 HAVING 中引用
                return false;
            } else if (item.getAlias() != null && expression.toString().startsWith("(")
                    && expression.toString().endsWith(")")) {
                // 检测括号包裹的单表达式（替代 Parenthesis 的逻辑）
                return false;
            }
        }
        return true;
    }

    /**
     * 处理 SELECT 主体，移除不必要的 ORDER BY。 优化递归处理逻辑，减少重复调用。
     *
     * @param select 查询信息
     */
    public void processSelect(Select select) {
        if (select == null) {
            return;
        }

        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select);
        } else if (select instanceof ParenthesedSelect) {
            processSelect(((ParenthesedSelect) select).getSelect());
        } else if (select instanceof SetOperationList) {
            SetOperationList setOpList = (SetOperationList) select;
            for (Select sel : setOpList.getSelects()) {
                processSelect(sel);
            }
            if (!orderByHashParameters(setOpList.getOrderByElements())) {
                setOpList.setOrderByElements(null);
            }
        }
    }

    /**
     * 处理 PlainSelect 类型的 SELECT 主体。 优化 JOIN 和 FROM 项处理逻辑。
     *
     * @param plainSelect 查询
     */
    public void processPlainSelect(PlainSelect plainSelect) {
        if (!orderByHashParameters(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }

        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem());
        }

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem());
                }
            }
        }
    }

    /**
     * 处理 WITH 子句，移除不必要的 ORDER BY。
     * <ol>
     * <li>使用 List&lt;WithItem&lt;?&gt;&gt; 适配 JSqlParser 5.1 的泛型 API。</li>
     * <li>提前检查 keepSubSelectOrderBy 和空列表，减少不必要循环。</li>
     * <li>使用 for-each 循环，减少迭代器创建。</li>
     * <li>提前检查 select 非空，减少无效递归。</li>
     * </ol>
     *
     * @param withItemsList WITH 子句列表
     */
    public void processWithItemsList(List<WithItem<?>> withItemsList) {
        if (withItemsList == null || withItemsList.isEmpty() || keepSubSelectOrderBy()) {
            return; // 提前退出，避免不必要的循环
        }

        for (WithItem<?> item : withItemsList) {
            Select select = item.getSelect();
            if (select != null) {
                processSelect(select);
            }
        }
    }

    /**
     * 处理 FROM 子句中的子查询。 优化类型检查和递归逻辑。
     *
     * @param fromItem FROM 子句项
     */
    public void processFromItem(FromItem fromItem) {
        if (fromItem instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
            Select select = parenthesedSelect.getSelect();
            if (select != null && !keepSubSelectOrderBy()) {
                processSelect(select);
            }
        } else if (fromItem instanceof Select) {
            processSelect((Select) fromItem);
        } else if (fromItem instanceof ParenthesedFromItem) {
            processFromItem(((ParenthesedFromItem) fromItem).getFromItem());
        }
    }

    /**
     * 检查是否需要保留 ORDER BY。 使用 PageMethod 的配置判断。
     *
     * @return 是否保留 ORDER BY
     */
    protected boolean keepOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepOrderBy();
    }

    /**
     * 检查是否需要保留子查询的 ORDER BY。
     *
     * @return 是否保留子查询 ORDER BY
     */
    protected boolean keepSubSelectOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepSubSelectOrderBy();
    }

    /**
     * 判断 ORDER BY 是否包含参数（?）。 优化空检查和循环效率。
     *
     * @param orderByElements ORDER BY 元素列表
     * @return 是否包含参数
     */
    public boolean orderByHashParameters(List<OrderByElement> orderByElements) {
        if (orderByElements == null || orderByElements.isEmpty()) {
            return false;
        }

        for (OrderByElement orderByElement : orderByElements) {
            if (orderByElement.toString().contains("?")) {
                return true;
            }
        }
        return false;
    }

}