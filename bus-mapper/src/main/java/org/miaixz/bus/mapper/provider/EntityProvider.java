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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.provider;

import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides dynamic SQL generation for basic CRUD operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityProvider {

    /**
     * Marks a method as unavailable and throws an exception.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     * @throws UnsupportedOperationException if the method is not available.
     */
    public static String unsupported(ProviderContext providerContext) {
        throw new UnsupportedOperationException(providerContext.getMapperMethod().getName() + " method not available");
    }

    /**
     * Saves an entity by inserting all its fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insert(ProviderContext providerContext) {
        return SqlScript.caching(
                providerContext,
                entity -> "INSERT INTO " + entity.tableName() + "(" + entity.insertColumnList() + ")" + " VALUES ("
                        + entity.insertColumns().stream().map(ColumnMeta::variables)
                                .collect(Collectors.joining(Symbol.COMMA))
                        + ")");
    }

    /**
     * Saves only the non-null fields of an entity.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insertSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "INSERT INTO " + entity.tableName() + trimSuffixOverrides(
                        "(",
                        ")",
                        Symbol.COMMA,
                        () -> entity.insertColumns().stream()
                                .map(column -> ifTest(column.notNullTest(), () -> column.column() + Symbol.COMMA))
                                .collect(Collectors.joining(Symbol.LF)))
                        + trimSuffixOverrides(
                                " VALUES (",
                                ")",
                                Symbol.COMMA,
                                () -> entity.insertColumns().stream().map(
                                        column -> ifTest(column.notNullTest(), () -> column.variables() + Symbol.COMMA))
                                        .collect(Collectors.joining(Symbol.LF)));
            }
        });
    }

    /**
     * Deletes a record by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String deleteByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(
                providerContext,
                entity -> "DELETE FROM " + entity.tableName() + " WHERE " + entity.idColumns().stream()
                        .map(ColumnMeta::columnEqualsProperty).collect(Collectors.joining(" AND ")));
    }

    /**
     * Deletes records in batch based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String delete(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "DELETE FROM " + entity.tableName() + parameterNotNull("Parameter cannot be null") + where(
                        () -> entity.columns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)));
            }
        });
    }

    /**
     * Updates all fields of an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + " SET "
                        + entity.updateColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(Symbol.COMMA))
                        + where(
                                () -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                        .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * Updates non-null fields of an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName()
                        + set(
                                () -> entity.updateColumns().stream()
                                        .map(
                                                column -> ifTest(
                                                        column.notNullTest(),
                                                        () -> column.columnEqualsProperty() + Symbol.COMMA))
                                        .collect(Collectors.joining(Symbol.LF)))
                        + where(
                                () -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                        .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * Selects an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + where(
                                () -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                        .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * Selects a single entity or a batch of entities based on entity field conditions. The number of results is defined
     * by the method.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String select(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + ifParameterNotNull(
                                () -> where(
                                        () -> entity.whereColumns().stream()
                                                .map(
                                                        column -> ifTest(
                                                                column.notNullTest(),
                                                                () -> "AND " + column.columnEqualsProperty()))
                                                .collect(Collectors.joining(Symbol.LF))))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

    /**
     * Counts the total number of records based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectCount(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return "SELECT COUNT(*)  FROM " + entity.tableName() + Symbol.LF
                        + ifParameterNotNull(
                                () -> where(
                                        () -> entity.whereColumns().stream()
                                                .map(
                                                        column -> ifTest(
                                                                column.notNullTest(),
                                                                () -> "AND " + column.columnEqualsProperty()))
                                                .collect(Collectors.joining(Symbol.LF))));
            }
        });
    }

}
