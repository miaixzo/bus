/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Provides dynamic SQL operations based on specified fields.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionProvider {

    /**
     * Updates non-null fields of an entity by its primary key, and forcibly updates specified fields (regardless of
     * nullness).
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

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
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateForFieldListByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

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
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectColumns(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

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
