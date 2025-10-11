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
package org.miaixz.bus.mapper.parsing;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.builder.GenericTypeResolver;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents the metadata of an entity table, recording the relationship between an entity and its corresponding table.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class TableMeta extends PropertyMeta<TableMeta> {

    /**
     * The original table name. When constructing SQL, the {@code tableName()} method should be used, which may return a
     * value processed by a proxy method.
     */
    protected String table;

    /**
     * The catalog name. If configured, the catalog will be prepended to the table name.
     */
    protected String catalog;

    /**
     * The schema name. If configured, the schema will be prepended to the table name.
     */
    protected String schema;

    /**
     * The naming style for converting entity classes and fields to table and column names.
     */
    protected String style;

    /**
     * The entity class.
     */
    protected Class<?> entityClass;

    /**
     * The list of column metadata for this table.
     */
    protected List<ColumnMeta> columns;

    /**
     * Indicates if the initialization is complete and the table metadata is ready for use.
     */
    protected boolean ready;

    /**
     * The ID of the specific {@link ResultMap} to use.
     */
    protected String resultMap;

    /**
     * Whether to automatically generate a {@link ResultMap} based on the fields.
     */
    protected boolean autoResultMap;

    /**
     * The list of initialized {@link ResultMap}s.
     */
    protected List<ResultMap> resultMaps;

    /**
     * An array of superclasses whose fields should be excluded from mapping.
     */
    protected Class<?>[] excludeSuperClasses;

    /**
     * An array of field types to be excluded from mapping.
     */
    protected Class<?>[] excludeFieldTypes;

    /**
     * An array of field names to be excluded from mapping.
     */
    protected String[] excludeFields;

    /**
     * A set of configurations that have already been initialized.
     */
    protected Set<Configuration> initConfiguration = new HashSet<>();

    /**
     * Constructs a new TableMeta instance.
     *
     * @param entityClass The entity class.
     */
    protected TableMeta(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Creates a new TableMeta instance.
     *
     * @param entityClass The entity class.
     * @return A new TableMeta instance.
     */
    public static TableMeta of(Class<?> entityClass) {
        return new TableMeta(entityClass);
    }

    /**
     * Gets the table name used in SQL statements.
     *
     * @return The table name, formatted as {@code catalog.schema.tableName}.
     */
    public String tableName() {
        return Stream.of(catalog(), schema(), table()).filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("."));
    }

    /**
     * Gets all column metadata.
     *
     * @return A list of all column metadata.
     */
    public List<ColumnMeta> columns() {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        return columns;
    }

    /**
     * Gets all field metadata.
     *
     * @return A list of all field metadata.
     */
    public List<FieldMeta> fieldMetas() {
        return columns().stream().map(ColumnMeta::fieldMeta).collect(Collectors.toList());
    }

    /**
     * Gets all column names.
     *
     * @return A list of all column names.
     */
    public List<String> columnNames() {
        return columns().stream().map(ColumnMeta::column).collect(Collectors.toList());
    }

    /**
     * Gets all property names.
     *
     * @return A list of all property names.
     */
    public List<String> fieldNames() {
        return columns().stream().map(ColumnMeta::property).collect(Collectors.toList());
    }

    /**
     * Adds a column to the table metadata.
     *
     * @param column The column metadata to add.
     */
    public void addColumn(ColumnMeta column) {
        if (!columns().contains(column)) {
            if (column.fieldMeta().getDeclaringClass() != entityClass()) {
                columns().add(0, column);
            } else {
                columns().add(column);
            }
            column.tableMeta(this);
        } else {
            // If a column with the same name exists in a superclass, it means it's overridden by a subclass,
            // so the field order should be prioritized.
            ColumnMeta existsColumn = columns().remove(columns().indexOf(column));
            columns().add(0, existsColumn);
        }
    }

    /**
     * Determines if {@link ResultMap}s can be used.
     *
     * @param providerContext The current method information.
     * @param cacheKey        The cache key, unique for each method (defaults to msId).
     * @return {@code true} if {@link ResultMap}s can be used, {@code false} otherwise.
     */
    protected boolean canUseResultMaps(ProviderContext providerContext, String cacheKey) {
        if (resultMaps != null && !resultMaps.isEmpty()
                && providerContext.getMapperMethod().isAnnotationPresent(SelectProvider.class)) {
            Class<?> resultType = resultMaps.get(0).getType();
            if (resultType == providerContext.getMapperMethod().getReturnType()) {
                return true;
            }
            Class<?> returnType = GenericTypeResolver
                    .getReturnType(providerContext.getMapperMethod(), providerContext.getMapperType());
            return resultType == returnType;
        }
        return false;
    }

    /**
     * Determines if the current entity class uses a {@link ResultMap}.
     *
     * @return {@code true} if a {@link ResultMap} is used, {@code false} otherwise.
     */
    public boolean useResultMaps() {
        return resultMaps != null || autoResultMap || StringKit.isNotEmpty(resultMap);
    }

    /**
     * Checks if the {@link ResultMap} has already been replaced.
     *
     * @param configuration The MyBatis configuration.
     * @param cacheKey      The cache key, unique for each method.
     * @return {@code true} if replaced, {@code false} otherwise.
     */
    protected boolean hasBeenReplaced(Configuration configuration, String cacheKey) {
        MappedStatement mappedStatement = configuration.getMappedStatement(cacheKey);
        if (mappedStatement.getResultMaps() != null && mappedStatement.getResultMaps().size() > 0) {
            return mappedStatement.getResultMaps().get(0) == resultMaps.get(0);
        }
        return false;
    }

    /**
     * Initializes runtime information. This method is executed once per method and must be idempotent.
     *
     * @param configuration   The MyBatis configuration.
     * @param providerContext The current method information.
     * @param cacheKey        The cache key, unique for each method.
     */
    public void initRuntimeContext(Configuration configuration, ProviderContext providerContext, String cacheKey) {
        if (!initConfiguration.contains(configuration)) {
            initResultMap(configuration, providerContext, cacheKey);
            initConfiguration.add(configuration);
        }
        if (canUseResultMaps(providerContext, cacheKey)) {
            synchronized (cacheKey) {
                if (!hasBeenReplaced(configuration, cacheKey)) {
                    MetaObject metaObject = configuration.newMetaObject(configuration.getMappedStatement(cacheKey));
                    metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
                }
            }
        }
    }

    /**
     * Initializes the {@link ResultMap}.
     *
     * @param configuration   The MyBatis configuration.
     * @param providerContext The current method information.
     * @param cacheKey        The cache key.
     */
    protected void initResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
        if (StringKit.isNotEmpty(resultMap)) {
            synchronized (this) {
                if (resultMaps == null) {
                    resultMaps = new ArrayList<>();
                    String resultMapId = generateResultMapId(providerContext, resultMap);
                    if (configuration.hasResultMap(resultMapId)) {
                        resultMaps.add(configuration.getResultMap(resultMapId));
                    } else if (configuration.hasResultMap(resultMap)) {
                        resultMaps.add(configuration.getResultMap(resultMap));
                    } else {
                        throw new RuntimeException(
                                entityClass().getName() + " configured resultMap: " + resultMap + " not found");
                    }
                }
            }
        } else if (autoResultMap) {
            synchronized (this) {
                if (resultMaps == null) {
                    resultMaps = new ArrayList<>();
                    ResultMap resultMap = genResultMap(configuration, providerContext, cacheKey);
                    resultMaps.add(resultMap);
                    configuration.addResultMap(resultMap);
                }
            }
        }
    }

    /**
     * Generates a {@link ResultMap} ID.
     *
     * @param providerContext The provider context.
     * @param resultMapId     The {@link ResultMap} ID.
     * @return The complete {@link ResultMap} ID.
     */
    protected String generateResultMapId(ProviderContext providerContext, String resultMapId) {
        if (resultMapId.indexOf(".") > 0) {
            return resultMapId;
        }
        return providerContext.getMapperType().getName() + "." + resultMapId;
    }

    /**
     * Generates a {@link ResultMap}.
     *
     * @param configuration   The MyBatis configuration.
     * @param providerContext The provider context.
     * @param cacheKey        The cache key.
     * @return A {@link ResultMap} instance.
     */
    protected ResultMap genResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
        List<ResultMapping> resultMappings = new ArrayList<>();
        for (ColumnMeta columnMeta : selectColumns()) {
            String column = columnMeta.column();
            Matcher matcher = Args.DELIMITER.matcher(column);
            if (matcher.find()) {
                column = matcher.group(1);
            }
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, columnMeta.property(), column,
                    columnMeta.javaType());
            if (columnMeta.jdbcType != null && columnMeta.jdbcType != JdbcType.UNDEFINED) {
                builder.jdbcType(columnMeta.jdbcType);
            }
            if (columnMeta.typeHandler != null && columnMeta.typeHandler != UnknownTypeHandler.class) {
                try {
                    builder.typeHandler(getTypeHandlerInstance(columnMeta.javaType(), columnMeta.typeHandler));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (columnMeta.id) {
                flags.add(ResultFlag.ID);
            }
            builder.flags(flags);
            resultMappings.add(builder.build());
        }
        String resultMapId = generateResultMapId(providerContext, Args.RESULT_MAP_NAME);
        ResultMap.Builder builder = new ResultMap.Builder(configuration, resultMapId, entityClass(), resultMappings,
                true);
        return builder.build();
    }

    /**
     * Instantiates a {@link TypeHandler}.
     *
     * @param javaTypeClass    The Java type.
     * @param typeHandlerClass The {@link TypeHandler} type.
     * @return A {@link TypeHandler} instance.
     */
    public TypeHandler getTypeHandlerInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (TypeHandler) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (TypeHandler) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }

    /**
     * Gets primary key columns. If no primary keys are defined, all columns are returned.
     *
     * @return A list of primary key columns.
     */
    public List<ColumnMeta> idColumns() {
        List<ColumnMeta> idColumns = columns().stream().filter(ColumnMeta::id).collect(Collectors.toList());
        if (idColumns.isEmpty()) {
            return columns();
        }
        return idColumns;
    }

    /**
     * Gets normal columns, excluding primary key fields.
     *
     * @return A list of normal columns.
     */
    public List<ColumnMeta> normalColumns() {
        return columns().stream().filter(column -> !column.id()).collect(Collectors.toList());
    }

    /**
     * Gets selectable columns.
     *
     * @return A list of selectable columns.
     */
    public List<ColumnMeta> selectColumns() {
        return columns().stream().filter(ColumnMeta::selectable).collect(Collectors.toList());
    }

    /**
     * Gets query condition columns. By default, all columns are considered.
     *
     * @return A list of query condition columns.
     */
    public List<ColumnMeta> whereColumns() {
        return columns();
    }

    /**
     * Gets insertable columns.
     *
     * @return A list of insertable columns.
     */
    public List<ColumnMeta> insertColumns() {
        return columns().stream().filter(ColumnMeta::insertable).collect(Collectors.toList());
    }

    /**
     * Gets updatable columns.
     *
     * @return A list of updatable columns.
     */
    public List<ColumnMeta> updateColumns() {
        return columns().stream().filter(ColumnMeta::updatable).collect(Collectors.toList());
    }

    /**
     * Gets GROUP BY columns. Returns empty by default.
     *
     * @return An {@link Optional} containing a list of GROUP BY columns.
     */
    public Optional<List<ColumnMeta>> groupByColumns() {
        return Optional.empty();
    }

    /**
     * Gets HAVING columns. Returns empty by default.
     *
     * @return An {@link Optional} containing a list of HAVING columns.
     */
    public Optional<List<ColumnMeta>> havingColumns() {
        return Optional.empty();
    }

    /**
     * Gets ORDER BY columns.
     *
     * @return An {@link Optional} containing a list of ORDER BY columns.
     */
    public Optional<List<ColumnMeta>> orderByColumns() {
        List<ColumnMeta> orderByColumns = columns().stream().filter(c -> StringKit.isNotEmpty(c.orderBy))
                .sorted(Comparator.comparing(ColumnMeta::orderByPriority)).collect(Collectors.toList());
        if (orderByColumns.size() > 0) {
            return Optional.of(orderByColumns);
        }
        return Optional.empty();
    }

    /**
     * Gets all selectable columns in the format "column1, column2, ...".
     *
     * @return The comma-separated string of selectable columns.
     */
    public String baseColumnList() {
        return selectColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * Gets all selectable columns in the format "column1 AS property1, column2 AS property2, ...". If
     * {@link #useResultMaps()} is true, it returns {@link #baseColumnList()}.
     *
     * @return The comma-separated string of selectable columns with aliases.
     */
    public String baseColumnAsPropertyList() {
        if (useResultMaps()) {
            return baseColumnList();
        }
        return selectColumns().stream().map(ColumnMeta::columnAsProperty).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * Gets all insertable columns in the format "column1, column2, ...".
     *
     * @return The comma-separated string of insertable columns.
     */
    public String insertColumnList() {
        return insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * Gets the GROUP BY column list in the format "column1, column2, ...".
     *
     * @return An {@link Optional} containing the comma-separated string of GROUP BY columns.
     */
    public Optional<String> groupByColumnList() {
        Optional<List<ColumnMeta>> groupByColumns = groupByColumns();
        return groupByColumns.map(
                entityColumns -> entityColumns.stream().map(ColumnMeta::column)
                        .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * Gets the GROUP BY column string with the " GROUP BY " prefix.
     *
     * @return An {@link Optional} containing the prefixed GROUP BY column string.
     */
    public Optional<String> groupByColumn() {
        Optional<String> groupByColumnList = groupByColumnList();
        return groupByColumnList.map(s -> " GROUP BY " + s);
    }

    /**
     * Gets the HAVING column list in the format "column1, column2, ...".
     *
     * @return An {@link Optional} containing the comma-separated string of HAVING columns.
     */
    public Optional<String> havingColumnList() {
        Optional<List<ColumnMeta>> havingColumns = havingColumns();
        return havingColumns.map(
                entityColumns -> entityColumns.stream().map(ColumnMeta::column)
                        .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * Gets the HAVING column string with the " HAVING " prefix.
     *
     * @return An {@link Optional} containing the prefixed HAVING column string.
     */
    public Optional<String> havingColumn() {
        Optional<String> havingColumnList = havingColumnList();
        return havingColumnList.map(s -> " HAVING " + s);
    }

    /**
     * Gets the ORDER BY column list in the format "column1 ASC, column2 DESC, ...".
     *
     * @return An {@link Optional} containing the comma-separated string of ORDER BY columns.
     */
    public Optional<String> orderByColumnList() {
        Optional<List<ColumnMeta>> orderByColumns = orderByColumns();
        return orderByColumns.map(
                entityColumns -> entityColumns.stream().map(column -> column.column() + Symbol.SPACE + column.orderBy())
                        .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * Gets the ORDER BY column string with the " ORDER BY " prefix.
     *
     * @return An {@link Optional} containing the prefixed ORDER BY column string.
     */
    public Optional<String> orderByColumn() {
        Optional<String> orderColumnList = orderByColumnList();
        return orderColumnList.map(s -> " ORDER BY " + s);
    }

    /**
     * Determines if a superclass should be excluded.
     *
     * @param superClass The superclass to check.
     * @return {@code true} if the superclass should be excluded, {@code false} otherwise.
     */
    public boolean isExcludeSuperClass(Class<?> superClass) {
        if (excludeSuperClasses != null) {
            for (Class<?> clazz : excludeSuperClasses) {
                if (clazz == superClass) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a specific field should be excluded.
     *
     * @param field The field to check.
     * @return {@code true} if the field should be excluded, {@code false} otherwise.
     */
    public boolean isExcludeField(FieldMeta field) {
        if (excludeFieldTypes != null) {
            Class<?> fieldType = field.getType();
            for (Class<?> clazz : excludeFieldTypes) {
                if (clazz == fieldType) {
                    return true;
                }
            }
        }
        if (excludeFields != null) {
            String fieldName = field.getName();
            for (String excludeField : excludeFields) {
                if (excludeField.equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares this TableMeta with another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TableMeta entity))
            return false;
        return tableName().equals(entity.tableName());
    }

    /**
     * Computes the hash code for this object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(tableName());
    }

    /**
     * Returns a string representation of the object.
     *
     * @return The table name.
     */
    @Override
    public String toString() {
        return tableName();
    }

}
