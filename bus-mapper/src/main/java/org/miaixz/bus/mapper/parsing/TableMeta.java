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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.builder.TypeResolver;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 实体表接口，记录实体和表的关系
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class TableMeta extends PropertyMeta<TableMeta> {

    /**
     * 原始表名，在拼 SQL 中，使用 tableName() 方法，可能返回代理方法加工后的值
     */
    protected String table;

    /**
     * catalog 名称，配置后会在表名前面加上 catalog
     */
    protected String catalog;

    /**
     * schema 名称，配置后会在表名前面加上 schema
     */
    protected String schema;

    /**
     * 实体类和字段转表名和字段名方式
     */
    protected String style;

    /**
     * 实体类
     */
    protected Class<?> entityClass;

    /**
     * 字段信息
     */
    protected List<ColumnMeta> columns;

    /**
     * 初始化完成，可以使用
     */
    protected boolean ready;

    /**
     * 使用指定的 resultMap
     */
    protected String resultMap;

    /**
     * 自动根据字段生成 resultMap
     */
    protected boolean autoResultMap;

    /**
     * 已初始化自动 ResultMap
     */
    protected List<ResultMap> resultMaps;

    /**
     * 排除指定父类的所有字段
     */
    protected Class<?>[] excludeSuperClasses;

    /**
     * 排除指定类型的字段
     */
    protected Class<?>[] excludeFieldTypes;

    /**
     * 排除指定字段名的字段
     */
    protected String[] excludeFields;

    /**
     * 已经初始化的配置
     */
    protected Set<Configuration> initConfiguration = new HashSet<>();

    /**
     * 构造函数，初始化实体表
     *
     * @param entityClass 实体类
     */
    protected TableMeta(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 创建 MapperTable 实例
     *
     * @param entityClass 实体类
     * @return MapperTable 实例
     */
    public static TableMeta of(Class<?> entityClass) {
        return new TableMeta(entityClass);
    }

    /**
     * 获取 SQL 语句中使用的表名
     *
     * @return 表名，格式为 catalog.schema.tableName
     */
    public String tableName() {
        return Stream.of(catalog(), schema(), table()).filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("."));
    }

    /**
     * 获取所有列
     *
     * @return 所有列信息
     */
    public List<ColumnMeta> columns() {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        return columns;
    }

    /**
     * 获取所有字段
     *
     * @return 所有字段
     */
    public List<FieldMeta> fields() {
        return columns().stream().map(ColumnMeta::field).collect(Collectors.toList());
    }

    /**
     * 获取所有列名
     *
     * @return 所有列名
     */
    public List<String> columnNames() {
        return columns().stream().map(ColumnMeta::column).collect(Collectors.toList());
    }

    /**
     * 获取所有属性名
     *
     * @return 所有属性名
     */
    public List<String> fieldNames() {
        return columns().stream().map(ColumnMeta::property).collect(Collectors.toList());
    }

    /**
     * 添加列
     *
     * @param column 列信息
     */
    public void addColumn(ColumnMeta column) {
        if (!columns().contains(column)) {
            if (column.field().getDeclaringClass() != entityClass()) {
                columns().add(0, column);
            } else {
                columns().add(column);
            }
            column.entityTable(this);
        } else {
            // 同名列在父类存在时，说明是子类覆盖的，字段顺序应更靠前
            ColumnMeta existsColumn = columns().remove(columns().indexOf(column));
            columns().add(0, existsColumn);
        }
    }

    /**
     * 判断是否可以使用 resultMaps
     *
     * @param providerContext 当前方法信息
     * @param cacheKey        缓存 key，每个方法唯一，默认与 msId 相同
     * @return true 表示可以使用，false 表示不可用
     */
    protected boolean canUseResultMaps(ProviderContext providerContext, String cacheKey) {
        if (resultMaps != null && !resultMaps.isEmpty()
                && providerContext.getMapperMethod().isAnnotationPresent(SelectProvider.class)) {
            Class<?> resultType = resultMaps.get(0).getType();
            if (resultType == providerContext.getMapperMethod().getReturnType()) {
                return true;
            }
            Class<?> returnType = TypeResolver.getReturnType(providerContext.getMapperMethod(),
                    providerContext.getMapperType());
            return resultType == returnType;
        }
        return false;
    }

    /**
     * 判断当前实体类是否使用 resultMap
     *
     * @return true 表示使用，false 表示不使用
     */
    public boolean useResultMaps() {
        return resultMaps != null || autoResultMap || StringKit.isNotEmpty(resultMap);
    }

    /**
     * 判断是否已替换 resultMap
     *
     * @param configuration MyBatis 配置类
     * @param cacheKey      缓存 key，每个方法唯一
     * @return true 表示已替换，false 表示未替换
     */
    protected boolean hasBeenReplaced(Configuration configuration, String cacheKey) {
        MappedStatement mappedStatement = configuration.getMappedStatement(cacheKey);
        if (mappedStatement.getResultMaps() != null && mappedStatement.getResultMaps().size() > 0) {
            return mappedStatement.getResultMaps().get(0) == resultMaps.get(0);
        }
        return false;
    }

    /**
     * 设置运行时信息，不同方法分别执行一次，需保证幂等
     *
     * @param configuration   MyBatis 配置类
     * @param providerContext 当前方法信息
     * @param cacheKey        缓存 key，每个方法唯一
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
     * 初始化 ResultMap
     *
     * @param configuration   MyBatis 配置类
     * @param providerContext 当前方法信息
     * @param cacheKey        缓存 key
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
     * 生成 ResultMap ID
     *
     * @param providerContext 提供者上下文
     * @param resultMapId     ResultMap ID
     * @return 完整的 ResultMap ID
     */
    protected String generateResultMapId(ProviderContext providerContext, String resultMapId) {
        if (resultMapId.indexOf(".") > 0) {
            return resultMapId;
        }
        return providerContext.getMapperType().getName() + "." + resultMapId;
    }

    /**
     * 生成 ResultMap
     *
     * @param configuration   MyBatis 配置类
     * @param providerContext 提供者上下文
     * @param cacheKey        缓存 key
     * @return ResultMap 实例
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
     * 实例化 TypeHandler
     *
     * @param javaTypeClass    Java 类型
     * @param typeHandlerClass TypeHandler 类型
     * @return TypeHandler 实例
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
     * 获取主键列，若无主键则返回所有列
     *
     * @return 主键列列表
     */
    public List<ColumnMeta> idColumns() {
        List<ColumnMeta> idColumns = columns().stream().filter(ColumnMeta::id).collect(Collectors.toList());
        if (idColumns.isEmpty()) {
            return columns();
        }
        return idColumns;
    }

    /**
     * 获取普通列，排除主键字段
     *
     * @return 普通列列表
     */
    public List<ColumnMeta> normalColumns() {
        return columns().stream().filter(column -> !column.id()).collect(Collectors.toList());
    }

    /**
     * 获取查询列
     *
     * @return 可查询列列表
     */
    public List<ColumnMeta> selectColumns() {
        return columns().stream().filter(ColumnMeta::selectable).collect(Collectors.toList());
    }

    /**
     * 获取查询条件列，默认所有列
     *
     * @return 查询条件列列表
     */
    public List<ColumnMeta> whereColumns() {
        return columns();
    }

    /**
     * 获取插入列
     *
     * @return 可插入列列表
     */
    public List<ColumnMeta> insertColumns() {
        return columns().stream().filter(ColumnMeta::insertable).collect(Collectors.toList());
    }

    /**
     * 获取更新列
     *
     * @return 可更新列列表
     */
    public List<ColumnMeta> updateColumns() {
        return columns().stream().filter(ColumnMeta::updatable).collect(Collectors.toList());
    }

    /**
     * 获取 GROUP BY 列，默认空
     *
     * @return GROUP BY 列的 Optional 包装对象
     */
    public Optional<List<ColumnMeta>> groupByColumns() {
        return Optional.empty();
    }

    /**
     * 获取 HAVING 列，默认空
     *
     * @return HAVING 列的 Optional 包装对象
     */
    public Optional<List<ColumnMeta>> havingColumns() {
        return Optional.empty();
    }

    /**
     * 获取排序列
     *
     * @return 排序列的 Optional 包装对象
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
     * 获取所有查询列，格式为 column1, column2, ...
     *
     * @return 查询列字符串
     */
    public String baseColumnList() {
        return selectColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * 获取所有查询列，格式为 column1 AS property1, column2 AS property2, ...
     *
     * @return 查询列带别名字符串
     */
    public String baseColumnAsPropertyList() {
        if (useResultMaps()) {
            return baseColumnList();
        }
        return selectColumns().stream().map(ColumnMeta::columnAsProperty).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * 获取所有插入列，格式为 column1, column2, ...
     *
     * @return 插入列字符串
     */
    public String insertColumnList() {
        return insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * 获取 GROUP BY 列列表，格式为 column1, column2, ...
     *
     * @return GROUP BY 列的 Optional 包装对象
     */
    public Optional<String> groupByColumnList() {
        Optional<List<ColumnMeta>> groupByColumns = groupByColumns();
        return groupByColumns.map(entityColumns -> entityColumns.stream().map(ColumnMeta::column)
                .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * 获取带 GROUP BY 前缀的列字符串
     *
     * @return 带 GROUP BY 前缀的列字符串的 Optional 包装对象
     */
    public Optional<String> groupByColumn() {
        Optional<String> groupByColumnList = groupByColumnList();
        return groupByColumnList.map(s -> " GROUP BY " + s);
    }

    /**
     * 获取 HAVING 列列表，格式为 column1, column2, ...
     *
     * @return HAVING 列的 Optional 包装对象
     */
    public Optional<String> havingColumnList() {
        Optional<List<ColumnMeta>> havingColumns = havingColumns();
        return havingColumns.map(entityColumns -> entityColumns.stream().map(ColumnMeta::column)
                .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * 获取带 HAVING 前缀的列字符串
     *
     * @return 带 HAVING 前缀的列字符串的 Optional 包装对象
     */
    public Optional<String> havingColumn() {
        Optional<String> havingColumnList = havingColumnList();
        return havingColumnList.map(s -> " HAVING " + s);
    }

    /**
     * 获取 ORDER BY 列列表，格式为 column1 ASC, column2 DESC, ...
     *
     * @return ORDER BY 列的 Optional 包装对象
     */
    public Optional<String> orderByColumnList() {
        Optional<List<ColumnMeta>> orderByColumns = orderByColumns();
        return orderByColumns.map(
                entityColumns -> entityColumns.stream().map(column -> column.column() + Symbol.SPACE + column.orderBy())
                        .collect(Collectors.joining(Symbol.COMMA)));
    }

    /**
     * 获取带 ORDER BY 前缀的列字符串
     *
     * @return 带 ORDER BY 前缀的列字符串的 Optional 包装对象
     */
    public Optional<String> orderByColumn() {
        Optional<String> orderColumnList = orderByColumnList();
        return orderColumnList.map(s -> " ORDER BY " + s);
    }

    /**
     * 判断是否需要排除父类
     *
     * @param superClass 父类
     * @return true 表示需要排除，false 表示不需要
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
     * 判断是否需要排除指定字段
     *
     * @param field 字段
     * @return true 表示需要排除，false 表示不需要
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
     * 判断两个 MapperTable 对象是否相等
     *
     * @param o 比较对象
     * @return true 表示相等，false 表示不相等
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
     * 计算对象的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(tableName());
    }

    /**
     * 返回字符串表示形式
     *
     * @return 表名
     */
    @Override
    public String toString() {
        return tableName();
    }

}