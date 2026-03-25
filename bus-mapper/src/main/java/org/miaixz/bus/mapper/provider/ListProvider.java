/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Provides dynamic SQL generation methods for batch operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ListProvider {

    /**
     * Batch inserts a list of entities.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @param entityList      The list of entities, which must be annotated with {@code @Param("entityList")}.
     * @return The cache key.
     * @throws NullPointerException if entityList is null or empty.
     */
    public static String insertList(ProviderContext providerContext, @Param("entityList") List<?> entityList) {
        if (entityList == null || entityList.size() == 0) {
            throw new NullPointerException("Parameter cannot be empty");
        }
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "INSERT INTO " + entity.tableName() + "(" + entity.insertColumnList() + ")" + " VALUES "
                        + foreach(
                                "entityList",
                                "entity",
                                Symbol.COMMA,
                                () -> trimSuffixOverrides(
                                        "(",
                                        ")",
                                        Symbol.COMMA,
                                        () -> entity.insertColumns().stream().map(column -> column.variables("entity."))
                                                .collect(Collectors.joining(Symbol.COMMA))));
            }
        });
    }

    /**
     * Batch updates a list of entities.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @param entityList      The list of entities, which must be annotated with {@code @Param("entityList")}.
     * @return The cache key.
     * @throws NullPointerException if entityList is null or empty.
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
                        + trimSuffixOverrides(
                                "SET",
                                Symbol.SPACE,
                                Symbol.COMMA,
                                () -> entity.updateColumns().stream()
                                        .map(
                                                column -> trimSuffixOverrides(
                                                        column.column() + " = CASE ",
                                                        "end, ",
                                                        "",
                                                        () -> foreach(
                                                                "entityList",
                                                                "entity",
                                                                Symbol.SPACE,
                                                                () -> "WHEN ( " + idColumns.stream()
                                                                        .map(id -> id.columnEqualsProperty("entity."))
                                                                        .collect(Collectors.joining(" AND "))
                                                                        + ") THEN " + column.variables("entity.")

                                                        ))).collect(Collectors.joining("")))
                        + where(
                                () -> "(" + idColumns
                                        .stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA))
                                        + ") in " + " ("
                                        + foreach(
                                                "entityList",
                                                "entity",
                                                "),(",
                                                "(",
                                                ")",
                                                () -> idColumns.stream().map(id -> id.variables("entity."))
                                                        .collect(Collectors.joining(Symbol.COMMA)))
                                        + ")");
                return sql;
            }
        });
    }

    /**
     * Batch updates non-null fields of a list of entities.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @param entityList      The list of entities, which must be annotated with {@code @Param("entityList")}.
     * @return The cache key.
     * @throws NullPointerException if entityList is null or empty.
     */
    public static String updateListSelective(ProviderContext providerContext, @Param("entityList") List<?> entityList) {
        if (entityList == null || entityList.size() == 0) {
            throw new NullPointerException("Parameter cannot be empty");
        }
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                List<ColumnMeta> idColumns = entity.idColumns();
                String sql = "UPDATE " + entity.tableName() + trimSuffixOverrides(
                        "SET",
                        Symbol.SPACE,
                        Symbol.COMMA,
                        () -> entity.updateColumns().stream().map(
                                column -> trimSuffixOverrides(
                                        column.column() + " = CASE ",
                                        "end, ",
                                        "",
                                        () -> foreach(
                                                "entityList",
                                                "entity",
                                                Symbol.SPACE,
                                                () -> choose(
                                                        () -> whenTest(
                                                                column.notNullTest("entity."),
                                                                () -> "WHEN ( " + idColumns.stream()
                                                                        .map(id -> id.columnEqualsProperty("entity."))
                                                                        .collect(Collectors.joining(" AND "))
                                                                        + ") THEN " + column.variables("entity."))
                                                                + otherwise(
                                                                        () -> "WHEN ( "
                                                                                + idColumns.stream().map(
                                                                                        id -> id.columnEqualsProperty(
                                                                                                "entity."))
                                                                                        .collect(
                                                                                                Collectors.joining(
                                                                                                        " AND "))
                                                                                + " ) THEN " + column.column())))))
                                .collect(Collectors.joining("")))

                        + where(
                                () -> "(" + idColumns
                                        .stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA))
                                        + ") in " + " ("
                                        + foreach(
                                                "entityList",
                                                "entity",
                                                "),(",
                                                "(",
                                                ")",
                                                () -> idColumns.stream().map(id -> id.variables("entity."))
                                                        .collect(Collectors.joining(Symbol.COMMA)))
                                        + ")"

                        );
                return sql;
            }
        });
    }

}
