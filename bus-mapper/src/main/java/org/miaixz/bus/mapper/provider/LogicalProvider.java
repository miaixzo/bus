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

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.annotation.Logical;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 支持逻辑删除的动态 SQL 操作实现。
 * <p>
 * 注意：使用时需在实体类字段上声明 @Logical 注解。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LogicalProvider {

    /**
     * 根据实体字段条件查询未逻辑删除的记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String select(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + where(() -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)) + logicalNotEqualCondition(entity))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

    /**
     * 根据实体字段条件查询未逻辑删除的记录，支持动态选择查询字段，结果数量由方法定义。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectColumns(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + choose(
                        () -> whenTest("fns != null and fns.isNotEmpty()", () -> "${fns.baseColumnAsPropertyList()}")
                                + otherwise(() -> entity.baseColumnAsPropertyList()))
                        + " FROM " + entity.tableName()
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> ifParameterNotNull(() -> where(() -> entity.whereColumns().stream()
                                        .map(column -> ifTest(column.notNullTest("entity."),
                                                () -> "AND " + column.columnEqualsProperty("entity.")))
                                        .collect(Collectors.joining(Symbol.LF)))) + logicalNotEqualCondition(entity))
                        + entity.groupByColumn().orElse("") + entity.havingColumn().orElse("")
                        + entity.orderByColumn().orElse("");
            }
        });
    }

    /**
     * 根据 Condition 条件批量查询未逻辑删除的记录，结果数量由方法定义。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT "
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
                        + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
                        + " FROM " + entity.tableName()
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                                        + logicalNotEqualCondition(entity))
                        + ifTest("orderByClause != null", () -> " ORDER BY ${orderByClause}")
                        + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(""))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * 根据 Condition 条件查询未逻辑删除记录的总数。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String countByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT COUNT("
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("simpleSelectColumns != null and simpleSelectColumns != ''",
                                () -> "${simpleSelectColumns}")
                        + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*") + ") FROM "
                        + entity.tableName()
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                                        + logicalNotEqualCondition(entity))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * 根据主键查询未逻辑删除的记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + where(() -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")))
                        + logicalNotEqualCondition(entity);
            }
        });
    }

    /**
     * 根据实体字段条件查询未逻辑删除记录的总数。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String selectCount(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT COUNT(*)  FROM " + entity.tableName() + Symbol.LF
                        + where(() -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)) + logicalNotEqualCondition(entity));
            }
        });
    }

    /**
     * 根据 Condition 条件批量更新未逻辑删除的实体信息，更新所有字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> column.columnEqualsProperty("entity."))
                                .collect(Collectors.joining(Symbol.COMMA)))
                        + variableNotNull("condition", "Condition cannot be null")
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalNotEqualCondition(entity))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据 Condition 条件批量更新未逻辑删除的实体非空字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByConditionSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> ifTest(column.notNullTest("entity."),
                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA))
                                .collect(Collectors.joining(Symbol.LF)))
                        + variableNotNull("condition", "Condition cannot be null")
                        + (entity.getBoolean("updateByConditionSelective.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalNotEqualCondition(entity))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据 Condition 条件批量更新未逻辑删除的实体信息，使用指定的设置值。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByConditionSetValues(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + variableNotEmpty("condition.setValues", "Condition setValues cannot be empty") + "UPDATE "
                        + entity.tableName() + Args.CONDITION_SET_CLAUSE_INNER_WHEN
                        + variableNotNull("condition", "Condition cannot be null")
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim("WHERE", "", "WHERE |OR |AND ", "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalNotEqualCondition(entity))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据主键更新未逻辑删除的实体所有字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + " SET "
                        + entity.updateColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(Symbol.COMMA))
                        + where(() -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")))
                        + logicalNotEqualCondition(entity);
            }
        });
    }

    /**
     * 根据主键更新未逻辑删除的实体非空字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + set(() -> entity.updateColumns().stream()
                        .map(column -> ifTest(column.notNullTest(), () -> column.columnEqualsProperty() + Symbol.COMMA))
                        .collect(Collectors.joining(Symbol.LF)))
                        + where(() -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(" AND ")))
                        + logicalNotEqualCondition(entity);
            }
        });
    }

    /**
     * 根据主键更新未逻辑删除的实体非空字段，强制更新指定字段（不区分是否为 null）。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
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
                                .collect(Collectors.joining(" AND ")))
                        + logicalNotEqualCondition(entity);
            }
        });
    }

    /**
     * 根据实体字段条件批量逻辑删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String delete(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                ColumnMeta logicColumn = getLogical(entity);
                return "UPDATE " + entity.tableName() + " SET "
                        + columnEqualsValue(logicColumn, deleteValue(logicColumn))
                        + parameterNotNull("Parameter cannot be null")
                        + where(() -> entity.columns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)) + logicalNotEqualCondition(entity));
            }
        });
    }

    /**
     * 根据主键逻辑删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String deleteByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {
            @Override
            public String getSql(TableMeta entity) {
                ColumnMeta logicColumn = getLogical(entity);
                return "UPDATE " + entity.tableName() + " SET "
                        + columnEqualsValue(logicColumn, deleteValue(logicColumn)) + " WHERE " + entity.idColumns()
                                .stream().map(ColumnMeta::columnEqualsProperty).collect(Collectors.joining(" AND "))
                        + logicalNotEqualCondition(entity);
            }
        });
    }

    /**
     * 根据 Condition 条件批量逻辑删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 缓存键
     */
    public static String deleteByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, (entity, util) -> {
            ColumnMeta logicColumn = getLogical(entity);
            return util.ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "UPDATE "
                    + entity.tableName() + " SET " + columnEqualsValue(logicColumn, deleteValue(logicColumn))
                    + util.parameterNotNull("Condition cannot be null")
                    + (entity.getBoolean("deleteByCondition.allowEmpty", true) ? ""
                            : util.variableIsFalse("_parameter.isEmpty()", "Condition Criteria cannot be empty"))
                    + Args.CONDITION_WHERE_CLAUSE + " AND "
                    + columnNotEqualsValueCondition(logicColumn, deleteValue(logicColumn))
                    + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}");
        });
    }

    /**
     * 获取标记为 @Logical 的字段，确保有且仅有一个逻辑删除字段。
     *
     * @param entity 实体表信息
     * @return 逻辑删除字段
     * @throws IllegalStateException 如果没有或存在多个 @Logical 注解字段
     */
    private static ColumnMeta getLogical(TableMeta entity) {
        List<ColumnMeta> logicColumns = entity.columns().stream()
                .filter(c -> c.field().isAnnotationPresent(Logical.class)).collect(Collectors.toList());
        Assert.isTrue(logicColumns.size() == 1, "There are no or multiple fields marked with @Logical");
        return logicColumns.get(0);
    }

    /**
     * 获取逻辑删除字段的删除值。
     *
     * @param logicColumn 逻辑删除字段
     * @return 删除值
     */
    private static String deleteValue(ColumnMeta logicColumn) {
        return logicColumn.field().getAnnotation(Logical.class).value();
    }

    /**
     * 生成字段等于指定值的条件。
     *
     * @param c     字段
     * @param value 值
     * @return 条件字符串
     */
    private static String columnEqualsValueCondition(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + choiceEqualsOperator(value) + value + Symbol.SPACE;
    }

    /**
     * 生成字段等于指定值的 SET 子句。
     *
     * @param c     字段
     * @param value 值
     * @return SET 子句字符串
     */
    private static String columnEqualsValue(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + " = " + value + Symbol.SPACE;
    }

    /**
     * 生成字段不等于指定值的条件。
     *
     * @param c     字段
     * @param value 值
     * @return 条件字符串
     */
    private static String columnNotEqualsValueCondition(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + choiceNotEqualsOperator(value) + value;
    }

    /**
     * 选择等于操作符，处理 null 值情况。
     *
     * @param value 值
     * @return 等于操作符（" = " 或 " IS "）
     */
    private static String choiceEqualsOperator(String value) {
        if ("null".compareToIgnoreCase(value) == 0) {
            return " IS ";
        }
        return " = ";
    }

    /**
     * 选择不等于操作符，处理 null 值情况。
     *
     * @param value 值
     * @return 不等于操作符（" != " 或 " IS NOT "）
     */
    private static String choiceNotEqualsOperator(String value) {
        if ("null".compareToIgnoreCase(value) == 0) {
            return " IS NOT ";
        }
        return " != ";
    }

    /**
     * 逻辑删除 SQL 脚本接口，添加逻辑删除条件。
     */
    private interface LogicalSqlScript extends SqlScript {
        /**
         * 生成逻辑删除不等于条件的 SQL 片段。
         *
         * @param entity 实体表信息
         * @return 逻辑删除条件
         */
        default String logicalNotEqualCondition(TableMeta entity) {
            ColumnMeta logicalColumn = getLogical(entity);
            return " AND " + columnNotEqualsValueCondition(logicalColumn, deleteValue(logicalColumn)) + Symbol.LF;
        }
    }

}