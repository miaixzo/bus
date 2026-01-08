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
package org.miaixz.bus.mapper.parsing;

import static org.miaixz.bus.mapper.Args.DELIMITER;

import java.util.Objects;
import java.util.regex.Matcher;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.support.keygen.GenId;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents the mapping between an entity field and a database column, storing column information provided on the
 * field.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class ColumnMeta extends PropertyMeta<ColumnMeta> {

    /**
     * The entity class field.
     */
    protected final FieldMeta fieldMeta;

    /**
     * The entity class this column belongs to.
     */
    protected TableMeta tableMeta;

    /**
     * The column name.
     */
    protected String column;

    /**
     * Whether this column is a primary key.
     */
    protected boolean id;

    /**
     * Whether this column can be null.
     */
    protected boolean nullable;

    /**
     * Primary key strategy 1 (Priority 1): Whether to use JDBC to get the primary key. This has the highest priority.
     */
    protected boolean useGeneratedKeys;

    /**
     * Primary key strategy 2 (Priority 2): The SQL to get the primary key, executed after an INSERT statement.
     */
    protected String afterSql;

    /**
     * Primary key strategy 3 (Priority 3): Generate the primary key in Java, can be used with an ID generator service.
     */
    protected Class<? extends GenId> genId;

    /**
     * The timing for executing `genId`, only effective when `genId` is not null. Defaults to before insertion.
     */
    protected boolean genIdExecuteBefore;

    /**
     * The sort order for this column.
     */
    protected String orderBy;

    /**
     * The priority of the sort order, lower values have higher priority.
     */
    protected int orderByPriority;

    /**
     * Whether this column is selectable.
     */
    protected boolean selectable = true;

    /**
     * Whether this column is insertable.
     */
    protected boolean insertable = true;

    /**
     * Whether this column is updatable.
     */
    protected boolean updatable = true;

    /**
     * The JDBC type of the column.
     */
    protected JdbcType jdbcType;

    /**
     * The type handler for this column.
     */
    protected Class<? extends TypeHandler> typeHandler;

    /**
     * The numeric scale for this column.
     */
    protected String numericScale;

    /**
     * Constructs a new MapperColumn.
     *
     * @param fieldMeta The entity class field.
     */
    protected ColumnMeta(FieldMeta fieldMeta) {
        this.fieldMeta = fieldMeta;
    }

    /**
     * Creates a new MapperColumn instance.
     *
     * @param field The entity class field.
     * @return A new MapperColumn instance.
     */
    public static ColumnMeta of(FieldMeta field) {
        return new ColumnMeta(field);
    }

    /**
     * Sets the entity table.
     *
     * @param entityTable The entity table information.
     * @return This MapperColumn instance.
     */
    public ColumnMeta tableMeta(TableMeta entityTable) {
        this.tableMeta = entityTable;
        return this;
    }

    /**
     * Sets the column name.
     *
     * @param column The column name.
     * @return This MapperColumn instance.
     */
    public ColumnMeta column(String column) {
        this.column = column;
        return this;
    }

    /**
     * Sets whether this column is a primary key.
     *
     * @param id Whether this column is a primary key.
     * @return This MapperColumn instance.
     */
    public ColumnMeta id(boolean id) {
        this.id = id;
        return this;
    }

    /**
     * Sets whether this column can be null.
     *
     * @param nullable Whether this column can be null.
     * @return This MapperColumn instance.
     */
    public ColumnMeta nullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * Sets whether to use JDBC to get the primary key.
     *
     * @param useGeneratedKeys Whether to use JDBC.
     * @return This MapperColumn instance.
     */
    public ColumnMeta useGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
        return this;
    }

    /**
     * Sets the SQL to get the primary key.
     *
     * @param afterSql The primary key SQL.
     * @return This MapperColumn instance.
     */
    public ColumnMeta afterSql(String afterSql) {
        this.afterSql = afterSql;
        return this;
    }

    /**
     * Sets the class for generating the primary key in Java.
     *
     * @param genId The primary key generator class.
     * @return This MapperColumn instance.
     */
    public ColumnMeta genId(Class<? extends GenId> genId) {
        this.genId = genId;
        return this;
    }

    /**
     * Sets the timing for executing `genId`.
     *
     * @param genIdExecuteBefore Whether to execute before insertion.
     * @return This MapperColumn instance.
     */
    public ColumnMeta genIdExecuteBefore(boolean genIdExecuteBefore) {
        this.genIdExecuteBefore = genIdExecuteBefore;
        return this;
    }

    /**
     * Sets the sort order.
     *
     * @param orderBy The sort order.
     * @return This MapperColumn instance.
     */
    public ColumnMeta orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * Sets the sort priority.
     *
     * @param orderByPriority The sort priority.
     * @return This MapperColumn instance.
     */
    public ColumnMeta orderByPriority(int orderByPriority) {
        this.orderByPriority = orderByPriority;
        return this;
    }

    /**
     * Sets whether this is a selectable column.
     *
     * @param selectable Whether this is a selectable column.
     * @return This MapperColumn instance.
     */
    public ColumnMeta selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * Sets whether this is an insertable column.
     *
     * @param insertable Whether this is an insertable column.
     * @return This MapperColumn instance.
     */
    public ColumnMeta insertable(boolean insertable) {
        this.insertable = insertable;
        return this;
    }

    /**
     * Sets whether this is an updatable column.
     *
     * @param updatable Whether this is an updatable column.
     * @return This MapperColumn instance.
     */
    public ColumnMeta updatable(boolean updatable) {
        this.updatable = updatable;
        return this;
    }

    /**
     * Sets the JDBC type.
     *
     * @param jdbcType The JDBC type.
     * @return This MapperColumn instance.
     */
    public ColumnMeta jdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    /**
     * Sets the type handler.
     *
     * @param typeHandler The type handler class.
     * @return This MapperColumn instance.
     */
    public ColumnMeta typeHandler(Class<? extends TypeHandler> typeHandler) {
        this.typeHandler = typeHandler;
        return this;
    }

    /**
     * Sets the numeric scale.
     *
     * @param numericScale The numeric scale.
     * @return This MapperColumn instance.
     */
    public ColumnMeta numericScale(String numericScale) {
        this.numericScale = numericScale;
        return this;
    }

    /**
     * Gets the Java type of the field.
     *
     * @return The Java type of the field.
     */
    public Class<?> javaType() {
        return fieldMeta().getType();
    }

    /**
     * Gets the property name.
     *
     * @return The property name.
     */
    public String property() {
        return property("");
    }

    /**
     * Gets the property name with a specified prefix.
     *
     * @param prefix The prefix, which should include a ".".
     * @return The prefixed property name.
     */
    public String property(String prefix) {
        return prefix + fieldMeta().getName();
    }

    /**
     * Returns an XML variable in the form #{property}.
     *
     * @return The XML variable string.
     */
    public String variables() {
        return variables("");
    }

    /**
     * Returns a prefixed XML variable in the form #{prefix.property}.
     *
     * @param prefix The prefix, which should include a ".".
     * @return The prefixed XML variable string.
     */
    public String variables(String prefix) {
        return "#{" + property(prefix) + jdbcTypeVariables().orElse("") + javaTypeVariables().orElse("")
                + typeHandlerVariables().orElse("") + numericScaleVariables().orElse("") + "}";
    }

    /**
     * Gets the Java type variable string, e.g., ", javaType=java.lang.String".
     *
     * @return An {@link Optional} containing the Java type variable string.
     */
    public Optional<String> javaTypeVariables() {
        Class<?> javaType = this.javaType();
        if (javaType != null) {
            return Optional.of(", javaType=" + javaType.getName());
        }
        return Optional.empty();
    }

    /**
     * Gets the database type variable string, e.g., ", jdbcType=VARCHAR".
     *
     * @return An {@link Optional} containing the database type variable string.
     */
    public Optional<String> jdbcTypeVariables() {
        if (this.jdbcType != null && this.jdbcType != JdbcType.UNDEFINED) {
            return Optional.of(", jdbcType=" + jdbcType);
        }
        return Optional.empty();
    }

    /**
     * Gets the type handler variable string, e.g., ", typeHandler=XXTypeHandler".
     *
     * @return An {@link Optional} containing the type handler variable string.
     */
    public Optional<String> typeHandlerVariables() {
        if (this.typeHandler != null && this.typeHandler != UnknownTypeHandler.class) {
            return Optional.of(", typeHandler=" + typeHandler.getName());
        }
        return Optional.empty();
    }

    /**
     * Gets the numeric scale variable string, e.g., ", numericScale=2".
     *
     * @return An {@link Optional} containing the numeric scale variable string.
     */
    public Optional<String> numericScaleVariables() {
        if (StringKit.isNotEmpty(this.numericScale)) {
            return Optional.of(", numericScale=" + numericScale);
        }
        return Optional.empty();
    }

    /**
     * Returns a string in the format "column AS property". No alias is used if column and property are the same.
     *
     * @return The "column AS property" string.
     */
    public String columnAsProperty() {
        return columnAsProperty("");
    }

    /**
     * Returns a string in the format "column AS prefix.property".
     *
     * @param prefix The prefix, which should include a ".".
     * @return The "column AS prefix.property" string.
     */
    public String columnAsProperty(String prefix) {
        // When comparing column and property, ignore delimiters (e.g., `order` in MySQL should be treated as the same
        // as the field 'order').
        String column = column();
        Matcher matcher = DELIMITER.matcher(column());
        if (matcher.find()) {
            column = matcher.group(1);
        }
        if (!Objects.equals(column, property(prefix))) {
            return column() + " AS " + property(prefix);
        }
        return column();
    }

    /**
     * Returns a string in the format "column = #{property}".
     *
     * @return The "column = #{property}" string.
     */
    public String columnEqualsProperty() {
        return columnEqualsProperty("");
    }

    /**
     * Returns a prefixed string in the format "column = #{prefix.property}".
     *
     * @param prefix The prefix, which should include a ".".
     * @return The "column = #{prefix.property}" string.
     */
    public String columnEqualsProperty(String prefix) {
        return column() + " = " + variables(prefix);
    }

    /**
     * Returns a string in the format "property != null".
     *
     * @return The "property != null" string.
     */
    public String notNullTest() {
        return notNullTest("");
    }

    /**
     * Returns a prefixed string in the format "prefix.property != null".
     *
     * @param prefix The prefix, which should include a ".".
     * @return The "prefix.property != null" string.
     */
    public String notNullTest(String prefix) {
        return property(prefix) + " != null";
    }

    /**
     * Returns "property != null and property != ''" for String types; otherwise, same as `notNullTest`.
     *
     * @return The non-empty check string.
     */
    public String notEmptyTest() {
        return notEmptyTest("");
    }

    /**
     * Returns "prefix.property != null and prefix.property != ''" for String types; otherwise, same as `notNullTest`.
     *
     * @param prefix The prefix, which should include a ".".
     * @return The non-empty check string.
     */
    public String notEmptyTest(String prefix) {
        if (fieldMeta().getType() == String.class) {
            return notNullTest(prefix) + " and " + property(prefix) + " != \'\' ";
        }
        return notNullTest();
    }

    /**
     * Checks if a primary key strategy is set for this column.
     *
     * @return {@code true} if a primary key strategy is set, {@code false} otherwise.
     */
    public boolean hasPrimaryKeyStrategy() {
        return id && (useGeneratedKeys || (afterSql != null && !afterSql.isEmpty())
                || (genId != null && genId != GenId.NULL.class));
    }

    /**
     * Compares this MapperColumn with another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ColumnMeta that))
            return false;
        return column().equals(that.column());
    }

    /**
     * Computes the hash code for this object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(column());
    }

    /**
     * Returns a string representation in the format "column = #{property}".
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return columnEqualsProperty();
    }

}
