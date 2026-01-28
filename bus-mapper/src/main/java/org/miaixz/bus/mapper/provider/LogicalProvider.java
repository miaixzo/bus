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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Logical;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides dynamic SQL operations with logical deletion support.
 * <p>
 * Note: When using, the entity class field must be annotated with {@code @Logical}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LogicalProvider {

    /**
     * Selects records that are not logically deleted based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String select(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for selecting records that are not logically deleted.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName() + where(
                        () -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)) + logicalCondition(entity, false))
                        + entity.groupByColumn().orElse(Normal.EMPTY) + entity.havingColumn().orElse(Normal.EMPTY)
                        + entity.orderByColumn().orElse(Normal.EMPTY);
            }
        });
    }

    /**
     * Selects records that are not logically deleted based on entity field conditions, with dynamic selection of query
     * fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectColumns(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for selecting columns with dynamic field selection.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + choose(
                        () -> whenTest("fns != null and fns.isNotEmpty()", () -> "${fns.baseColumnAsPropertyList()}")
                                + otherwise(() -> entity.baseColumnAsPropertyList()))
                        + " FROM " + entity.tableName()
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> ifParameterNotNull(
                                        () -> where(
                                                () -> entity.whereColumns().stream().map(
                                                        column -> ifTest(
                                                                column.notNullTest("entity."),
                                                                () -> "AND " + column.columnEqualsProperty("entity.")))
                                                        .collect(Collectors.joining(Symbol.LF))))
                                        + logicalCondition(entity, false))
                        + entity.groupByColumn().orElse(Normal.EMPTY) + entity.havingColumn().orElse(Normal.EMPTY)
                        + entity.orderByColumn().orElse(Normal.EMPTY);
            }
        });
    }

    /**
     * Selects records that are not logically deleted based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for selecting records based on a Condition object.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT "
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest("selectColumns != null and selectColumns != ''", () -> "${selectColumns}")
                        + ifTest("selectColumns == null or selectColumns == ''", entity::baseColumnAsPropertyList)
                        + " FROM " + entity.tableName()
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                                        + logicalCondition(entity, false))
                        + ifTest("orderByClause != null", () -> " Order BY ${orderByClause}")
                        + ifTest("orderByClause == null", () -> entity.orderByColumn().orElse(Normal.EMPTY))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * Counts the total number of records that are not logically deleted based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String countByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for counting records based on a Condition object.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "SELECT COUNT("
                        + ifTest("distinct", () -> "distinct ")
                        + ifTest(
                                "simpleSelectColumns != null and simpleSelectColumns != ''",
                                () -> "${simpleSelectColumns}")
                        + ifTest("simpleSelectColumns == null or simpleSelectColumns == ''", () -> "*") + ") FROM "
                        + entity.tableName()
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> ifParameterNotNull(() -> Args.CONDITION_WHERE_CLAUSE)
                                        + logicalCondition(entity, false))
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * Selects a record that is not logically deleted by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for selecting a record by primary key.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + where(
                                () -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                        .collect(Collectors.joining(" AND ")))
                        + logicalCondition(entity, false);
            }
        });
    }

    /**
     * Counts the total number of records that are not logically deleted based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectCount(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for counting records based on entity conditions.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return "SELECT COUNT(*)  FROM " + entity.tableName() + Symbol.LF + where(
                        () -> entity.whereColumns().stream().map(
                                column -> ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty()))
                                .collect(Collectors.joining(Symbol.LF)) + logicalCondition(entity, false));
            }
        });
    }

    /**
     * Batch updates non-logically deleted entity information based on a Condition object, updating all fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating all fields by Condition.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + "UPDATE " + entity.tableName()
                        + set(
                                () -> entity.updateColumns().stream()
                                        .map(column -> column.columnEqualsProperty("entity."))
                                        .collect(Collectors.joining(Symbol.COMMA)))
                        + variableNotNull("condition", "Condition cannot be null")
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalCondition(entity, false))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Batch updates non-logically deleted entity non-null fields based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByConditionSelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating non-null fields by Condition.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
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
                        + (entity.getBoolean("updateByConditionSelective.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalCondition(entity, false))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Batch updates non-logically deleted entity information based on a Condition object, using specified set values.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByConditionSetValues(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating with specified set values by Condition.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return ifTest("condition.startSql != null and condition.startSql != ''", () -> "${condition.startSql}")
                        + variableNotEmpty("condition.setValues", "Condition setValues cannot be empty") + "UPDATE "
                        + entity.tableName() + Args.CONDITION_SET_CLAUSE_INNER_WHEN
                        + variableNotNull("condition", "Condition cannot be null")
                        + (entity.getBoolean("updateByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("condition.isEmpty()", "Condition Criteria cannot be empty"))
                        + trim(
                                "WHERE",
                                "",
                                "WHERE |OR |AND ",
                                "",
                                () -> Args.UPDATE_BY_CONDITION_WHERE_CLAUSE + logicalCondition(entity, false))
                        + ifTest("condition.endSql != null and condition.endSql != ''", () -> "${condition.endSql}");
            }
        });
    }

    /**
     * Updates all fields of a non-logically deleted entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating all fields by primary key.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                return "UPDATE " + entity.tableName() + " SET "
                        + entity.updateColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                .collect(Collectors.joining(Symbol.COMMA))
                        + where(
                                () -> entity.idColumns().stream().map(ColumnMeta::columnEqualsProperty)
                                        .collect(Collectors.joining(" AND ")))
                        + logicalCondition(entity, false);
            }
        });
    }

    /**
     * Updates non-null fields of a non-logically deleted entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating non-null fields by primary key.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
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
                                        .collect(Collectors.joining(" AND ")))
                        + logicalCondition(entity, false);
            }
        });
    }

    /**
     * Updates non-null fields of a non-logically deleted entity by its primary key, and forcibly updates specified
     * fields (regardless of nullness).
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelectiveWithForceFields(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for updating non-null fields with forced fields by primary key.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
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
                                        .collect(Collectors.joining(" AND ")))
                        + logicalCondition(entity, false);
            }
        });
    }

    /**
     * Logically deletes records in batch based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String delete(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for logically deleting records by conditions.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                ColumnMeta logicColumn = getLogical(entity);
                return "UPDATE " + entity.tableName() + " SET "
                        + columnEqualsValue(logicColumn, deleteValue(logicColumn))
                        + parameterNotNull("Parameter cannot be null")
                        + where(
                                () -> entity.columns().stream()
                                        .map(
                                                column -> ifTest(
                                                        column.notNullTest(),
                                                        () -> "AND " + column.columnEqualsProperty()))
                                        .collect(Collectors.joining(Symbol.LF)) + logicalCondition(entity, true));
            }
        });
    }

    /**
     * Logically deletes a record by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String deleteByPrimaryKey(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for logically deleting a record by primary key.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                ColumnMeta logicColumn = getLogical(entity);
                return "UPDATE " + entity.tableName() + " SET "
                        + columnEqualsValue(logicColumn, deleteValue(logicColumn)) + " WHERE " + entity.idColumns()
                                .stream().map(ColumnMeta::columnEqualsProperty).collect(Collectors.joining(" AND "))
                        + logicalCondition(entity, true);
            }
        });
    }

    /**
     * Logically deletes records in batch based on a Condition object.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String deleteByCondition(ProviderContext providerContext) {
        return SqlScript.caching(providerContext, new LogicalSqlScript() {

            /**
             * Generates SQL for logically deleting records by Condition.
             *
             * @param entity the table metadata entity
             * @return the generated SQL string
             */
            @Override
            public String getSql(TableMeta entity) {
                ColumnMeta logicColumn = getLogical(entity);
                return ifTest("startSql != null and startSql != ''", () -> "${startSql}") + "UPDATE "
                        + entity.tableName() + " SET " + columnEqualsValue(logicColumn, deleteValue(logicColumn))
                        + parameterNotNull("Condition cannot be null")
                        + (entity.getBoolean("deleteByCondition.allowEmpty", true) ? ""
                                : variableIsFalse("_parameter.isEmpty()", "Condition Criteria cannot be empty"))
                        + Args.CONDITION_WHERE_CLAUSE + " AND " + logicalCondition(entity, true)
                        + ifTest("endSql != null and endSql != ''", () -> "${endSql}");
            }
        });
    }

    /**
     * Gets the column marked with {@code @Logical}, ensuring there is exactly one such field.
     *
     * @param entity The entity table information.
     * @return The logical deletion column.
     * @throws IllegalStateException if there are no or multiple fields annotated with {@code @Logical}.
     */
    private static ColumnMeta getLogical(TableMeta entity) {
        List<ColumnMeta> logicColumns = entity.columns().stream()
                .filter(c -> c.fieldMeta().isAnnotationPresent(Logical.class)).collect(Collectors.toList());
        Assert.isTrue(logicColumns.size() == 1, "There are no or multiple fields marked with @Logical");
        return logicColumns.get(0);
    }

    /**
     * Gets the deletion value for the logical deletion column.
     *
     * @param logicColumn The logical deletion column.
     * @return The deletion value.
     */
    private static String deleteValue(ColumnMeta logicColumn) {
        return logicColumn.fieldMeta().getAnnotation(Logical.class).value();
    }

    /**
     * Gets the valid status value for the logical deletion column.
     *
     * @param logicColumn The logical deletion column.
     * @return The valid status value.
     */
    private static String validValue(ColumnMeta logicColumn) {
        return logicColumn.fieldMeta().getAnnotation(Logical.class).valid();
    }

    /**
     * Checks if an equality condition should be used for the logical deletion column.
     *
     * @param logicColumn The logical deletion column.
     * @return {@code true} if an equality condition should be used, {@code false} otherwise.
     */
    private static boolean useEqualsCondition(ColumnMeta logicColumn) {
        return logicColumn.fieldMeta().getAnnotation(Logical.class).useEqualsCondition();
    }

    /**
     * Generates a condition string for a column equal to a specified value.
     *
     * @param c     The column.
     * @param value The value.
     * @return The condition string.
     */
    private static String columnEqualsValueCondition(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + choiceEqualsOperator(value) + value + Symbol.SPACE;
    }

    /**
     * Generates a SET clause string for a column equal to a specified value.
     *
     * @param c     The column.
     * @param value The value.
     * @return The SET clause string.
     */
    private static String columnEqualsValue(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + " = " + value + Symbol.SPACE;
    }

    /**
     * Generates a condition string for a column not equal to a specified value.
     *
     * @param c     The column.
     * @param value The value.
     * @return The condition string.
     */
    private static String columnNotEqualsValueCondition(ColumnMeta c, String value) {
        return Symbol.SPACE + c.column() + choiceNotEqualsOperator(value) + value;
    }

    /**
     * Selects the appropriate equality operator, handling null values.
     *
     * @param value The value.
     * @return The equality operator (" = " or " IS ").
     */
    private static String choiceEqualsOperator(String value) {
        if ("null".compareToIgnoreCase(value) == 0) {
            return " IS ";
        }
        return " = ";
    }

    /**
     * Selects the appropriate inequality operator, handling null values.
     *
     * @param value The value.
     * @return The inequality operator (" != " or " IS NOT ").
     */
    private static String choiceNotEqualsOperator(String value) {
        if ("null".compareToIgnoreCase(value) == 0) {
            return " IS NOT ";
        }
        return " != ";
    }

    /**
     * An SQL script interface that adds logical deletion conditions.
     */
    private interface LogicalSqlScript extends SqlScript {

        /**
         * Generates the SQL fragment for logical deletion conditions. Uses an equality condition or inequality
         * condition based on configuration.
         *
         * @param entity            The entity table information.
         * @param isDeleteOperation Whether it is a delete operation. Delete operations always use an inequality
         *                          condition.
         * @return The logical deletion condition.
         */
        default String logicalCondition(TableMeta entity, boolean isDeleteOperation) {
            ColumnMeta logicalColumn = getLogical(entity);

            // Delete operations always use an inequality condition to ensure only non-logically deleted records are
            // affected.
            if (isDeleteOperation) {
                return columnNotEqualsValueCondition(logicalColumn, deleteValue(logicalColumn)) + Symbol.LF;
            }

            // Query and update operations use an equality or inequality condition based on configuration.
            if (useEqualsCondition(logicalColumn)) {
                // Use equality condition: status = 1
                return " AND " + columnEqualsValueCondition(logicalColumn, validValue(logicalColumn)) + Symbol.LF;
            } else {
                // Use inequality condition: status != -1 (original logic)
                return " AND " + columnNotEqualsValueCondition(logicalColumn, deleteValue(logicalColumn)) + Symbol.LF;
            }
        }
    }

}
