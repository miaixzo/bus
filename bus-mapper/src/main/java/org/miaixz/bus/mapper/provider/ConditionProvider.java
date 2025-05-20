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
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.mapping.MapperTable;
import org.miaixz.bus.mapper.mapping.SqlScript;

/**
 * 提供基于条件的动态SQL生成，用于基本的增删改查操作。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConditionProvider {

    /**
     * 根据Condition对象删除记录。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String deleteByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext,
                (entity, util) -> util.ifTest("startSql != null and startSql != ''", () -> "${startSql}")
                        + "DELETE FROM " + entity.tableName() + util.parameterNotNull("Condition cannot be null")
                        // 是否允许空条件，默认允许，允许时不检查查询条件
                        + (entity.getPropBoolean("deleteByCondition.allowEmpty", true) ? ""
                                : util.variableIsFalse("_parameter.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.CONDITION_WHERE_CLAUSE
                        + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}"));
    }

    /**
     * 根据Condition对象批量更新实体信息，更新所有字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String updateByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> column.columnEqualsProperty("entity.")).collect(Collectors.joining(",")))
                        + variableNotNull("condition", "Condition cannot be null")
                // 是否允许空条件，默认允许，允许时不检查查询条件
                        + (entity.getPropBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据Condition对象批量更新实体信息，使用指定的设置值。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String updateByConditionSetValues(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + variableNotEmpty("condition.setValues", "Condition setValues cannot be empty") + "UPDATE "
                        + entity.tableName() + Args.CONDITION_SET_CLAUSE_INNER_WHEN
                        + variableNotNull("condition", "Condition cannot be null")
                // 是否允许空条件，默认允许，允许时不检查查询条件
                        + (entity.getPropBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据Condition对象批量更新实体非空字段。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String updateByConditionSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(() -> entity.updateColumns().stream()
                                .map(column -> ifTest(column.notNullTest("entity."),
                                        () -> column.columnEqualsProperty("entity.") + ","))
                                .collect(Collectors.joining(Symbol.LF)))
                        + variableNotNull("condition", "Condition cannot be null")
                // 是否允许空条件，默认允许，允许时不检查查询条件
                        + (entity.getPropBoolean("updateByConditionSelective.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * 根据Condition对象批量查询记录，结果数量由方法定义。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String selectByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT "
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
                        + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
                        + " FROM " + entity.tableName() + ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                        + ifTest("orderByClause != null", () -> " ORDER BY ${orderByClause}")
                        + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(""))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * 根据Condition对象查询记录总数。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 生成的SQL缓存键
     */
    public static String countByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {
            @Override
            public String getSql(MapperTable entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT COUNT("
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("simpleSelectColumns != null and simpleSelectColumns != ''",
                                () -> "${simpleSelectColumns}")
                        + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*") + ") FROM "
                        + entity.tableName() + ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

}