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
package org.miaixz.bus.mapper.binding.function;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.mapper.OGNL;
import org.miaixz.bus.mapper.parsing.ClassField;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A utility interface for method references, used to obtain field and column information.
 *
 * @param <T> The type of the entity class.
 * @param <R> The return type of the method reference.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Fn<T, R> extends Function<T, R>, Serializable {

    /**
     * Caches the mapping between method references and their corresponding column metadata.
     */
    Map<Fn<?, ?>, ColumnMeta> FN_COLUMN_MAP = new ConcurrentHashMap<>();

    /**
     * Caches the mapping between method references and their corresponding field information.
     */
    Map<Fn<?, ?>, ClassField> FN_CLASS_FIELD_MAP = new ConcurrentHashMap<>();

    /**
     * Creates a virtual table containing the specified fields, suitable for base classes or generic base classes.
     *
     * @param entityClass The entity class type.
     * @param fns         An array of method references.
     * @param <E>         The entity type.
     * @return A virtual table object (`FnArray`).
     */
    @SafeVarargs
    static <E> FnArray<E> of(Class<E> entityClass, Fn<E, Object>... fns) {
        return new FnArray<>(entityClass, fns);
    }

    /**
     * Creates a virtual table containing the specified fields.
     *
     * @param fns An array of method references.
     * @param <E> The entity type.
     * @return A virtual table object (`FnArray`).
     */
    @SafeVarargs
    static <E> FnArray<E> of(Fn<E, Object>... fns) {
        return of(null, fns);
    }

    /**
     * Creates a virtual table containing the specified column names.
     *
     * @param entityClass The entity class type.
     * @param columnNames An array of column names.
     * @param <E>         The entity type.
     * @return A virtual table object (`FnArray`).
     */
    static <E> FnArray<E> of(Class<E> entityClass, String... columnNames) {
        TableMeta entityTable = MapperFactory.create(entityClass);
        Set<String> columnNameSet = Arrays.stream(columnNames).collect(Collectors.toSet());
        List<ColumnMeta> columns = entityTable.columns().stream()
                .filter(column -> columnNameSet.contains(column.property())).collect(Collectors.toList());
        return new FnArray<>(entityClass, entityTable.tableName(), columns);
    }

    /**
     * Specifies a field within an entity class using a method reference.
     *
     * @param entityClass The entity class type.
     * @param field       The method reference.
     * @param <T>         The entity type.
     * @return A method reference object.
     */
    static <T> Fn<T, Object> field(Class<T> entityClass, Fn<T, Object> field) {
        return field.in(entityClass);
    }

    /**
     * Specifies a field within an entity class by its name.
     *
     * @param entityClass The entity class type.
     * @param field       The name of the field.
     * @param <T>         The entity type.
     * @return A method reference object.
     */
    static <T> Fn<T, Object> field(Class<T> entityClass, String field) {
        return new FnName<>(entityClass, field);
    }

    /**
     * Specifies a column within an entity class by its name.
     *
     * @param entityClass The entity class type.
     * @param column      The name of the column.
     * @param <T>         The entity type.
     * @return A method reference object.
     */
    static <T> Fn<T, Object> column(Class<T> entityClass, String column) {
        return new FnName<>(entityClass, column, true);
    }

    /**
     * Specifies the entity class to which this method reference belongs, useful in inheritance scenarios.
     *
     * @param entityClass The entity class type.
     * @return An {@code Fn} object associated with the specified entity class.
     */
    default Fn<T, R> in(Class<?> entityClass) {
        return new FnType<>(this, entityClass);
    }

    /**
     * Gets the field name corresponding to this method reference.
     *
     * @return The field name.
     */
    default String toField() {
        return toClassField().getField();
    }

    /**
     * Gets the column name corresponding to this method reference.
     *
     * @return The column name.
     */
    default String toColumn() {
        return toEntityColumn().column();
    }

    /**
     * Gets the field information (name and owning class) corresponding to this method reference.
     *
     * @return The field information as a {@link ClassField} object.
     */
    default ClassField toClassField() {
        return FN_CLASS_FIELD_MAP.computeIfAbsent(this, key -> OGNL.fnToFieldName(key));
    }

    /**
     * Gets the column metadata corresponding to this method reference.
     *
     * @return The column metadata as a {@link ColumnMeta} object.
     */
    default ColumnMeta toEntityColumn() {
        return FN_COLUMN_MAP.computeIfAbsent(this, key -> {
            ClassField classField = toClassField();
            List<ColumnMeta> columns = MapperFactory.create(classField.getClazz()).columns();
            return columns.stream().filter(column -> column.property().equals(classField.getField())).findFirst()
                    .orElseGet(
                            () -> columns.stream().filter(classField).findFirst().orElseThrow(
                                    () -> new RuntimeException(classField.getField()
                                            + " does not mark database column field annotations, unable to obtain column information")));
        });
    }

    /**
     * A method reference wrapper that includes a specific entity class type.
     *
     * @param <T> The entity type.
     * @param <R> The return type.
     */
    class FnType<T, R> implements Fn<T, R> {

        /**
         * The original method reference.
         */
        public final Fn<T, R> fn;
        /**
         * The entity class type.
         */
        public final Class<?> entityClass;

        /**
         * Constructs an FnType with the original method reference and entity class.
         *
         * @param fn          The original method reference.
         * @param entityClass The entity class type.
         */
        public FnType(Fn<T, R> fn, Class<?> entityClass) {
            this.fn = fn;
            this.entityClass = entityClass;
        }

        /**
         * Applies the method reference to the given object.
         *
         * @param t The input object.
         * @return The result of applying the method reference.
         */
        @Override
        public R apply(T t) {
            return fn.apply(t);
        }

        /**
         * Compares this FnType object with another object for equality.
         *
         * @param o The object to compare with.
         * @return {@code true} if the objects are equal, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FnType<?, ?> fnType = (FnType<?, ?>) o;
            return Objects.equals(entityClass, fnType.entityClass) && Objects.equals(fn, fnType.fn);
        }

        /**
         * Computes the hash code for this object.
         *
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            return Objects.hash(entityClass, fn);
        }
    }

    /**
     * A method reference that supports specifying a field or column name directly.
     *
     * @param <T> The entity type.
     * @param <R> The return type.
     */
    class FnName<T, R> implements Fn<T, R> {

        /**
         * The entity class type.
         */
        public final Class<?> entityClass;
        /**
         * The name of the field or column.
         */
        public final String name;
        /**
         * Indicates whether the name is a column name (true) or a field name (false).
         */
        public final boolean column;

        /**
         * Constructs an FnName with a specified field name.
         *
         * @param entityClass The entity class type.
         * @param name        The field name.
         */
        public FnName(Class<?> entityClass, String name) {
            this(entityClass, name, false);
        }

        /**
         * Constructs an FnName with a specified field or column name.
         *
         * @param entityClass The entity class type.
         * @param name        The field or column name.
         * @param column      {@code true} if the name is a column name, {@code false} if it is a field name.
         */
        public FnName(Class<?> entityClass, String name, boolean column) {
            this.entityClass = entityClass;
            this.name = name;
            this.column = column;
        }

        /**
         * Specifies the entity class to which this method reference belongs.
         *
         * @param entityClass The entity class type.
         * @return A new {@code FnName} object.
         */
        @Override
        public Fn<T, R> in(Class<?> entityClass) {
            return new FnName<>(entityClass, this.name, this.column);
        }

        /**
         * Applies the method reference to the given object (placeholder implementation, returns null).
         *
         * @param o The input object.
         * @return Always returns null.
         */
        @Override
        public R apply(Object o) {
            return null;
        }

        /**
         * Compares this FnName object with another object for equality.
         *
         * @param o The object to compare with.
         * @return {@code true} if the objects are equal, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FnName<?, ?> fnName = (FnName<?, ?>) o;
            return column == fnName.column && Objects.equals(entityClass, fnName.entityClass)
                    && Objects.equals(name, fnName.name);
        }

        /**
         * Computes the hash code for this object.
         *
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            return Objects.hash(entityClass, name, column);
        }
    }

    /**
     * Represents a virtual table with a subset of fields, defined by an array of method references.
     *
     * @param <E> The entity type.
     */
    class FnArray<E> extends TableMeta {

        /**
         * Constructs a virtual table from a list of column metadata.
         *
         * @param entityClass The entity class type.
         * @param table       The table name.
         * @param columns     A list of column metadata.
         */
        private FnArray(Class<E> entityClass, String table, List<ColumnMeta> columns) {
            super(entityClass);
            this.table = table;
            this.columns = columns;
        }

        /**
         * Constructs a virtual table from an array of method references.
         *
         * @param entityClass The entity class type.
         * @param fns         An array of method references.
         */
        @SafeVarargs
        private FnArray(Class<E> entityClass, Fn<E, Object>... fns) {
            super(entityClass);
            this.columns = new ArrayList<>(fns.length);
            for (int i = 0; i < fns.length; i++) {
                if (entityClass != null) {
                    this.columns.add(fns[i].in(entityClass).toEntityColumn());
                } else {
                    this.columns.add(fns[i].toEntityColumn());
                }
                if (i == 0) {
                    TableMeta entityTable = this.columns.get(i).tableMeta();
                    this.table = entityTable.tableName();
                    this.style = entityTable.style();
                    this.entityClass = entityTable.entityClass();
                    this.resultMap = entityTable.resultMap();
                    this.autoResultMap = entityTable.autoResultMap();
                }
            }
        }

        /**
         * Checks if the virtual table has any fields.
         *
         * @return {@code true} if not empty, {@code false} otherwise.
         */
        public boolean isNotEmpty() {
            return !columns().isEmpty();
        }
    }

}
