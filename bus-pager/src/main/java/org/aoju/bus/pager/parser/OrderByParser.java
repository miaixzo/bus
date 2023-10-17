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

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.aoju.bus.core.exception.PageException;
import org.aoju.bus.logger.Logger;

import java.util.List;

/**
 * 处理 Order by
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OrderByParser {

    /**
     * convert to order by sql
     *
     * @param sql     sql
     * @param orderBy 排序
     * @return the string
     */
    public static String converToOrderBySql(String sql, String orderBy, JSqlParser jSqlParser) {
        // 解析SQL
        Statement stmt;
        try {
            stmt = jSqlParser.parse(sql);
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            // 处理body-去最外层order by
            List<OrderByElement> orderByElements = extraOrderBy(selectBody);
            String defaultOrderBy = PlainSelect.orderByToString(orderByElements);
            if (defaultOrderBy.indexOf('?') != -1) {
                throw new PageException("原SQL[" + sql + "]中的order by包含参数，因此不能使用OrderBy插件进行修改!");
            }
            // 新的sql
            sql = select.toString();
        } catch (Throwable e) {
            Logger.warn("处理排序失败: " + e + "，降级为直接拼接 order by 参数");
        }
        return sql + " order by " + orderBy;
    }

    /**
     * extra order by and set default orderby to null
     *
     * @param selectBody 获取body
     * @return 结果
     */
    public static List<OrderByElement> extraOrderBy(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<OrderByElement> orderByElements = ((PlainSelect) selectBody).getOrderByElements();
            ((PlainSelect) selectBody).setOrderByElements(null);
            return orderByElements;
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (null != withItem.getSubSelect()) {
                return extraOrderBy(withItem.getSubSelect().getSelectBody());
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (null != operationList.getSelects() && operationList.getSelects().size() > 0) {
                List<SelectBody> plainSelects = operationList.getSelects();
                return extraOrderBy(plainSelects.get(plainSelects.size() - 1));
            }
        }
        return null;
    }

    /**
     * convert to order by sql
     *
     * @param sql     SQL
     * @param orderBy 排序属性
     * @return the string
     */
    public static String converToOrderBySql(String sql, String orderBy) {
        return converToOrderBySql(sql, orderBy, JSqlParser.DEFAULT);
    }

}
