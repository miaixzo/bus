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
package org.miaixz.bus.mapper.provider;

import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 提供基于指定字段的动态 SQL 操作。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionProvider {

    /**
     * 根据主键更新实体中不为空的字段，强制更新指定字段（不区分是否为 null）。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> choose(() -> whenTest(
                                        "fns != null and fns.fieldNames().contains('" + column.property() + "')",
                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)
                                        + whenTest(column.notNullTest("entity."),
                                                () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)))
                                .collect(Collectors.joining(Symbol.LF)))
                        + where(() -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity."))
                                .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * 根据主键更新指定的字段列表。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateForFieldListByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> choose(() -> whenTest(
                                        "fns != null and fns.fieldNames().contains('" + column.property() + "')",
                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)))
                                .collect(Collectors.joining(Symbol.LF)))
                        + where(() -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity."))
                                .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * 根据实体字段条件查询唯一实体或批量查询，支持动态选择查询字段，结果数量由方法定义。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectColumns(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT "
                        + choose(
                                () -> whenTest("fns != null and fns.isNotEmpty()",
                                        () -> "${fns.baseColumnAsPropertyList()}")
                                        + otherwise(() -> entity.baseColumnAsPropertyList()))
                        + " FROM " + entity.tableName()
                        + ifParameterNotNull(() -> where(() -> entity.whereColumns().stream()
                                .map(column -> ifTest(column.notNullTest("entity."),
                                        () -> "AND " + column.columnEqualsProperty("entity.")))
                                .collect(Collectors.joining(Symbol.LF))))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

}