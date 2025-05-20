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
package org.miaixz.bus.mapper.provider;

import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.mapping.MapperColumn;
import org.miaixz.bus.mapper.mapping.MapperTable;
import org.miaixz.bus.mapper.mapping.SqlScript;

/**
 * 提供基本的增删改查操作，生成动态 SQL。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityProvider {

    /**
     * 标记不可用方法，抛出异常。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     * @throws UnsupportedOperationException 方法不可用
     */
    public static String unsupported(ProviderContext providerContext) {
        throw new UnsupportedOperationException(providerContext.getMapperMethod().getName() + " method not available");
    }

    /**
     * 保存实体，插入所有字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String insert(ProviderContext providerContext) {
        return SqlScript.caching(providerContext,
                entity -> "INSERT INTO " + entity.tableName() + "(" + entity.insertColumnList() + ")" + " VALUES ("
                        + entity.insertColumns().stream().map(MapperColumn::variables).collect(Collectors.joining(","))
                        + ")");
    }

    /**
     * 保存实体中不为空的字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String insertSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "INSERT INTO " + entity.tableName()
                        + trimSuffixOverrides("(", ")", ",",
                                () -> entity.insertColumns().stream()
                                        .map(column -> ifTest(column.notNullTest(), () -> column.column() + ","))
                                        .collect(Collectors.joining(Symbol.LF)))
                        + trimSuffixOverrides(" VALUES (", ")", ",",
                                () -> entity.insertColumns().stream()
                                        .map(column -> ifTest(column.notNullTest(), () -> column.variables() + ","))
                                        .collect(Collectors.joining(Symbol.LF)));
            }
        });
    }

    /**
     * 根据主键删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String deleteByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, entity -> "DELETE FROM " + entity.tableName() + " WHERE " + entity
                .idColumns().stream().map(MapperColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
    }

    /**
     * 根据实体字段条件批量删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String delete(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "DELETE FROM " + entity.tableName() + parameterNotNull("Parameter cannot be null")
                        + where(() -> entity.columns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)));
            }
        });
    }

    /**
     * 根据主键更新实体所有字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "UPDATE " + entity.tableName() + " SET "
                        + entity.updateColumns().stream().map(MapperColumn::columnEqualsProperty)
                                .collect(Collectors.joining(","))
                        + where(() -> entity.idColumns().stream().map(MapperColumn::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * 根据主键更新实体中不为空的字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> ifTest(column.notNullTest(), () -> column.columnEqualsProperty() + ","))
                                .collect(Collectors.joining(Symbol.LF)))
                        + where(() -> entity.idColumns().stream().map(MapperColumn::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * 根据主键查询实体。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + where(() -> entity.idColumns().stream().map(MapperColumn::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")));
            }
        });
    }

    /**
     * 根据实体字段条件查询唯一实体或批量查询，结果数量由方法定义。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String select(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + ifParameterNotNull(() -> where(() -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF))))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

    /**
     * 根据实体字段条件查询记录总数。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectCount(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return "SELECT COUNT(*)  FROM " + entity.tableName() + Symbol.LF
                        + ifParameterNotNull(() -> where(() -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF))));
            }
        });
    }

}