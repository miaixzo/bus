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

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Base class for providers, unifying SQL caching logic and reducing code duplication.
 *
 * <p>
 * This class provides common SQL building methods that all Provider classes should extend. Through the template method
 * pattern, SQL building logic is extracted to the base class, reducing code duplication by over 50%.
 * </p>
 *
 * <p>
 * Design goals:
 * </p>
 * <ul>
 * <li>Unified SQL caching logic</li>
 * <li>Provide common SQL building methods</li>
 * <li>Reduce code duplication in subclasses</li>
 * <li>Improve code maintainability</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class BasicProvider {

    /**
     * Template method for caching SQL scripts.
     *
     * <p>
     * This method encapsulates the common logic for SQL caching; subclasses only need to provide the SQL building
     * function.
     * </p>
     *
     * @param providerContext Provider context
     * @param sqlBuilder      SQL building function
     * @return Cache key
     */
    protected static String cacheSql(ProviderContext providerContext, Function<TableMeta, String> sqlBuilder) {
        return SqlScript.caching(providerContext, entity -> sqlBuilder.apply(entity));
    }

    /**
     * Build INSERT statement (insert all fields).
     *
     * @param entity Table metadata
     * @return INSERT SQL
     */
    protected static String buildInsertAll(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + " (" + entity.insertColumnList() + ")" + " VALUES ("
                + entity.insertColumns().stream().map(ColumnMeta::variables).collect(Collectors.joining(Symbol.COMMA))
                + ")";
    }

    /**
     * Build INSERT statement (insert only non-null fields).
     *
     * @param entity Table metadata
     * @return INSERT SQL (dynamic SQL)
     */
    protected static String buildInsertSelective(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + "<trim prefix='(' suffix=')' suffixOverrides=','>"
                + entity.insertColumns().stream()
                        .map(col -> "<if test='" + col.notNullTest() + "'>" + col.column() + "," + "</if>")
                        .collect(Collectors.joining("\n"))
                + "</trim>" + "<trim prefix='VALUES (' suffix=')' suffixOverrides=','>"
                + entity.insertColumns().stream()
                        .map(col -> "<if test='" + col.notNullTest() + "'>" + col.variables() + "," + "</if>")
                        .collect(Collectors.joining("\n"))
                + "</trim>";
    }

    /**
     * Build UPDATE statement (update all fields).
     *
     * @param entity Table metadata
     * @return UPDATE SQL
     */
    protected static String buildUpdateAll(TableMeta entity) {
        return "UPDATE "
                + entity.tableName() + " SET " + entity.updateColumns().stream()
                        .map(col -> col.column() + " = " + col.variables()).collect(Collectors.joining(", "))
                + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build UPDATE statement (update only non-null fields).
     *
     * @param entity Table metadata
     * @return UPDATE SQL (dynamic SQL)
     */
    protected static String buildUpdateSelective(TableMeta entity) {
        return "UPDATE " + entity.tableName() + "<set>" + entity.updateColumns().stream().map(
                col -> "<if test='" + col.notNullTest() + "'>" + col.column() + " = " + col.variables() + "," + "</if>")
                .collect(Collectors.joining("\n")) + "</set>" + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build SELECT statement (query by primary key).
     *
     * @param entity Table metadata
     * @return SELECT SQL
     */
    protected static String buildSelectByPrimaryKey(TableMeta entity) {
        return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build SELECT statement (query all).
     *
     * @param entity Table metadata
     * @return SELECT SQL
     */
    protected static String buildSelectAll(TableMeta entity) {
        return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName();
    }

    /**
     * Build DELETE statement (delete by primary key).
     *
     * @param entity Table metadata
     * @return DELETE SQL
     */
    protected static String buildDeleteByPrimaryKey(TableMeta entity) {
        return "DELETE FROM " + entity.tableName() + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build WHERE clause (primary key condition).
     *
     * @param entity      Table metadata
     * @param paramPrefix Parameter prefix (optional)
     * @return WHERE clause
     */
    protected static String buildWherePrimaryKey(TableMeta entity, String paramPrefix) {
        StringBuilder sql = new StringBuilder();
        sql.append("\n<where>");

        entity.idColumns().forEach(pk -> {
            sql.append("\n  AND ").append(pk.column()).append(" = ");
            if (paramPrefix != null) {
                sql.append(pk.variables(paramPrefix));
            } else {
                sql.append(pk.variables());
            }
        });

        sql.append("\n</where>");
        return sql.toString();
    }

    /**
     * Build WHERE clause (entity object condition, only non-null fields).
     *
     * @param entity    Table metadata
     * @param paramName Parameter name
     * @return WHERE clause (dynamic SQL)
     */
    protected static String buildWhereSelective(TableMeta entity, String paramName) {
        StringBuilder sql = new StringBuilder();
        sql.append("\n<where>");

        entity.columns().forEach(col -> {
            String test = paramName != null ? paramName + "." + col.property() + " != null"
                    : col.property() + " != null";
            String variable = paramName != null ? col.variables(paramName) : col.variables();

            sql.append("\n  <if test='").append(test).append("'>");
            sql.append("AND ").append(col.column()).append(" = ").append(variable);
            sql.append("</if>");
        });

        sql.append("\n</where>");
        return sql.toString();
    }

    /**
     * Build COUNT statement.
     *
     * @param entity Table metadata
     * @return COUNT SQL
     */
    protected static String buildCount(TableMeta entity) {
        return "SELECT COUNT(*) FROM " + entity.tableName();
    }

    /**
     * Build COUNT statement (with condition).
     *
     * @param entity    Table metadata
     * @param paramName Parameter name
     * @return COUNT SQL (dynamic SQL)
     */
    protected static String buildCountSelective(TableMeta entity, String paramName) {
        return "SELECT COUNT(*) FROM " + entity.tableName() + buildWhereSelective(entity, paramName);
    }

    /**
     * Build EXISTS statement (check if primary key exists).
     *
     * @param entity Table metadata
     * @return EXISTS SQL
     */
    protected static String buildExistsByPrimaryKey(TableMeta entity) {
        return "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM " + entity.tableName()
                + buildWherePrimaryKey(entity, null);
    }

}
