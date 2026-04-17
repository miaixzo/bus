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

import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides dynamic SQL operations based on specified fields.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FunctionProvider {

    /**
     * Updates non-null fields of an entity by its primary key, and forcibly updates specified fields (regardless of
     * nullness).
     *
     * @param context The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext context) {
        return SqlScript.caching(context, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + set(
                        () -> entity.updateColumns().stream().map(
                                column -> choose(
                                        () -> whenTest(
                                                "fns != null and fns.fieldNames().contains('" + column.property()
                                                        + "')",
                                                () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)
                                                + whenTest(
                                                        column.notNullTest("entity."),
                                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)))
                                .collect(Collectors.joining(Symbol.LF)))
                        + where(
                                () -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity."))
                                        .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * Updates a specified list of fields by the primary key.
     *
     * @param context The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateForFieldListByPrimaryKey(ProviderContext context) {
        return SqlScript.caching(context, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + set(
                        () -> entity.updateColumns().stream()
                                .map(
                                        column -> choose(
                                                () -> whenTest(
                                                        "fns != null and fns.fieldNames().contains('"
                                                                + column.property() + "')",
                                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA)))
                                .collect(Collectors.joining(Symbol.LF)))
                        + where(
                                () -> entity.idColumns().stream().map(column -> column.columnEqualsProperty("entity."))
                                        .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * Selects a single entity or a batch of entities based on entity field conditions, with support for dynamic
     * selection of query fields. The number of results is defined by the method.
     *
     * @param context The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectColumns(ProviderContext context) {
        return SqlScript.caching(context, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + choose(
                        () -> whenTest("fns != null and fns.isNotEmpty()", () -> "${fns.baseColumnAsPropertyList()}")
                                + otherwise(() -> entity.baseColumnAsPropertyList()))
                        + " FROM " + entity.tableName()
                        + ifParameterNotNull(
                                () -> where(
                                        () -> entity.whereColumns().stream()
                                                .map(
                                                        column -> ifTest(
                                                                column.notNullTest("entity."),
                                                                () -> "AND " + column.columnEqualsProperty("entity.")))
                                                .collect(Collectors.joining(Symbol.LF))))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

}
