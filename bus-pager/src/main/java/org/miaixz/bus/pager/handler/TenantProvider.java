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

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * 租户：行级多租户
 */
public interface TenantProvider {

    /**
     * 获取租户 ID 值表达式，仅支持单个 ID 值
     *
     * @return 租户 ID 表达式
     */
    Expression getTenantId();

    /**
     * 获取租户字段名
     *
     * @return 租户字段名，默认为 "tenant_id"
     */
    default String getColumn() {
        return "tenant_id";
    }

    /**
     * 判断是否忽略表的多租户条件拼接
     *
     * @param name 表名
     * @return true 表示忽略，false 表示需要拼接
     */
    default boolean ignore(String name) {
        return false;
    }

    /**
     * 判断是否忽略插入租户字段
     *
     * @param columns 插入的字段列表
     * @param column  租户 ID 字段名
     * @return true 表示忽略，false 表示需要插入
     */
    default boolean ignore(List<Column> columns, String column) {
        return columns.stream().map(Column::getColumnName).anyMatch(name -> name.equalsIgnoreCase(column));
    }

}