/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 提供批量操作的动态 SQL 生成方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListProvider {

    /**
     * 批量插入实体列表。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @param entityList      实体列表，需使用 @Param("entityList") 注解
     * @return 缓存键
     * @throws NullPointerException 如果 entityList 为 null 或空
     */
    public static String insertList(ProviderContext providerContext, @Param("entityList") List<?> entityList) {
        if (entityList == null || entityList.size() == 0) {
            throw new NullPointerException("Parameter cannot be empty");
        }
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "INSERT INTO " + entity.tableName() + "(" + entity.insertColumnList() + ")" + " VALUES "
                        + foreach("entityList", "entity", Symbol.COMMA,
                                () -> trimSuffixOverrides("(", ")", Symbol.COMMA,
                                        () -> entity.insertColumns().stream().map(column -> column.variables("entity."))
                                                .collect(Collectors.joining(Symbol.COMMA))));
            }
        });
    }

    /**
     * 批量更新实体列表。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @param entityList      实体列表，需使用 @Param("entityList") 注解
     * @return 缓存键
     * @throws NullPointerException 如果 entityList 为 null 或空
     */
    public static String updateList(ProviderContext providerContext, @Param("entityList") List<?> entityList) {
        if (entityList == null || entityList.size() == 0) {
            throw new NullPointerException("Parameter cannot be empty");
        }
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                List<ColumnMeta> idColumns = entity.idColumns();
                String sql = "UPDATE " + entity.tableName()
                        + trimSuffixOverrides("SET", Symbol.SPACE, Symbol.COMMA,
                                () -> entity
                                        .updateColumns().stream().map(
                                                column -> trimSuffixOverrides(column.column() + " = CASE ", "end, ", "",
                                                        () -> foreach("entityList", "entity", Symbol.SPACE,
                                                                () -> "WHEN ( "
                                                                        + idColumns.stream()
                                                                                .map(id -> id.columnEqualsProperty(
                                                                                        "entity."))
                                                                                .collect(Collectors.joining(" AND "))
                                                                        + ") THEN " + column.variables("entity.")

                                                        ))).collect(Collectors.joining("")))
                        + where(() -> "("
                                + idColumns.stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA))
                                + ") in " + " ("
                                + foreach("entityList", "entity", "),(", "(", ")", () -> idColumns.stream()
                                        .map(id -> id.variables("entity.")).collect(Collectors.joining(Symbol.COMMA)))
                                + ")");
                return sql;
            }
        });
    }

    /**
     * 批量更新实体列表中非空字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @param entityList      实体列表，需使用 @Param("entityList") 注解
     * @return 缓存键
     * @throws NullPointerException 如果 entityList 为 null 或空
     */
    public static String updateListSelective(ProviderContext providerContext, @Param("entityList") List<?> entityList) {
        if (entityList == null || entityList.size() == 0) {
            throw new NullPointerException("Parameter cannot be empty");
        }
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                List<ColumnMeta> idColumns = entity.idColumns();
                String sql = "UPDATE " + entity.tableName()
                        + trimSuffixOverrides("SET", Symbol.SPACE, Symbol.COMMA, () -> entity
                                .updateColumns().stream().map(
                                        column -> trimSuffixOverrides(column.column() + " = CASE ", "end, ", "",
                                                () -> foreach("entityList", "entity", Symbol.SPACE,
                                                        () -> choose(() -> whenTest(column.notNullTest("entity."),
                                                                () -> "WHEN ( "
                                                                        + idColumns.stream()
                                                                                .map(id -> id.columnEqualsProperty(
                                                                                        "entity."))
                                                                                .collect(Collectors.joining(" AND "))
                                                                        + ") THEN " + column.variables("entity."))
                                                                + otherwise(() -> "WHEN ( "
                                                                        + idColumns.stream()
                                                                                .map(id -> id.columnEqualsProperty(
                                                                                        "entity."))
                                                                                .collect(Collectors.joining(" AND "))
                                                                        + " ) THEN " + column.column())))))
                                .collect(Collectors.joining("")))

                        + where(() -> "("
                                + idColumns.stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA))
                                + ") in " + " ("
                                + foreach("entityList", "entity", "),(", "(", ")", () -> idColumns.stream()
                                        .map(id -> id.variables("entity.")).collect(Collectors.joining(Symbol.COMMA)))
                                + ")"

                        );
                return sql;
            }
        });
    }

}