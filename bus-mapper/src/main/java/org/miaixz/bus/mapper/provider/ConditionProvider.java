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
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides dynamic SQL generation based on conditions for basic CRUD operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ConditionProvider {

    /**
     * Deletes records based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String deleteByCondition(ProviderContext providerContext) {
        return SqlScript.caching(
                providerContext,
                (entity, util) -> util.ifTest("startSql != null and startSql != ''", () -> "${startSql}")
                        + "DELETE FROM " + entity.tableName() + util.parameterNotNull("Condition cannot be null")
                        // Whether to allow empty conditions; defaults to true, allowing deletion without a WHERE
                        // clause.
                        + (entity.getBoolean("deleteByCondition.allowEmpty", true) ? ""
                                : util.variableIsFalse("_parameter.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.CONDITION_WHERE_CLAUSE
                        + util.ifTest("endSql != null and endSql != ''", () -> "${endSql}"));
    }

    /**
     * Updates entity information in batch based on a Condition object, updating all fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String updateByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(
                                () -> entity.updateColumns().stream()
                                        .map(column -> column.columnEqualsProperty("entity."))
                                        .collect(Collectors.joining(Symbol.COMMA)))
                        + variableNotNull("condition", "Condition cannot be null")
                // Whether to allow empty conditions; defaults to true.
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Updates entity information in batch based on a Condition object, using specified set values.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String updateByConditionSetValues(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + variableNotEmpty("condition.setValues", "Condition setValues cannot be empty") + "UPDATE "
                        + entity.tableName() + Args.CONDITION_SET_CLAUSE_INNER_WHEN
                        + variableNotNull("condition", "Condition cannot be null")
                // Whether to allow empty conditions; defaults to true.
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Updates non-null fields of an entity in batch based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String updateByConditionSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(
                                () -> entity.updateColumns().stream()
                                        .map(
                                                column -> ifTest(
                                                        column.notNullTest("entity."),
                                                        () -> column.columnEqualsProperty("entity.") + Symbol.COMMA))
                                        .collect(Collectors.joining(Symbol.LF)))
                        + variableNotNull("condition", "Condition cannot be null")
                // Whether to allow empty conditions; defaults to true.
                        + (entity.getBoolean("updateByConditionSelective.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.UPDATE_BY_CONDITION_WHERE_CLAUSE
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Selects records in batch based on a Condition object. The number of results is defined by the method.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String selectByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT "
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
                        + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
                        + " FROM " + entity.tableName() + ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                        + ifTest("orderByClause != null", () -> " Order BY ${orderByClause}")
                        + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(""))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * Counts the total number of records based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The generated SQL cache key.
     */
    public static String countByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new SqlScript() {

            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT COUNT("
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest(
                                "simpleSelectColumns != null and simpleSelectColumns != ''",
                                () -> "${simpleSelectColumns}")
                        + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*") + ") FROM "
                        + entity.tableName() + ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

}
