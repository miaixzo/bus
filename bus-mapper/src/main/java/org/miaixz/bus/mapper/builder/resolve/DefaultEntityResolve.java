/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.builder.resolve;

import jakarta.persistence.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.text.NamingCase;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.annotation.ColumnType;
import org.miaixz.bus.mapper.annotation.KeySql;
import org.miaixz.bus.mapper.annotation.NameStyle;
import org.miaixz.bus.mapper.annotation.Order;
import org.miaixz.bus.mapper.builder.FieldBuilder;
import org.miaixz.bus.mapper.criteria.Identity;
import org.miaixz.bus.mapper.criteria.SimpleType;
import org.miaixz.bus.mapper.criteria.Style;
import org.miaixz.bus.mapper.criteria.Words;
import org.miaixz.bus.mapper.entity.Config;
import org.miaixz.bus.mapper.entity.EntityColumn;
import org.miaixz.bus.mapper.entity.EntityField;
import org.miaixz.bus.mapper.entity.EntityTable;
import org.miaixz.bus.mapper.genid.GenId;
import org.miaixz.bus.mapper.gensql.GenSql;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultEntityResolve implements EntityResolve {

    /**
     * 根据指定的样式进行转换
     *
     * @param text  字符串
     * @param style 样式
     * @return the string
     */
    public static String convertByStyle(String text, Style style) {
        switch (style) {
            case camelhump:
                return NamingCase.toUnderlineCase(text);
            case uppercase:
                return text.toUpperCase();
            case lowercase:
                return text.toLowerCase();
            case camelhumpAndLowercase:
                return NamingCase.toUnderlineCase(text).toLowerCase();
            case camelhumpAndUppercase:
                return NamingCase.toUnderlineCase(text).toUpperCase();
            case normal:
            default:
                return text;
        }
    }

    @Override
    public EntityTable resolveEntity(Class<?> entityClass, Config config) {
        Style style = config.getStyle();
        // style，该注解优先于全局配置
        if (entityClass.isAnnotationPresent(NameStyle.class)) {
            NameStyle nameStyle = entityClass.getAnnotation(NameStyle.class);
            style = nameStyle.value();
        }

        // 创建并缓存EntityTable
        EntityTable entityTable = null;
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (!"".equals(table.name())) {
                entityTable = new EntityTable(entityClass);
                entityTable.setTable(table);
            }
        }
        if (entityTable == null) {
            entityTable = new EntityTable(entityClass);
            // 可以通过stye控制
            String tableName = convertByStyle(entityClass.getSimpleName(), style);
            // 自动处理关键字
            if (StringKit.isNotEmpty(config.getWrapKeyword()) && Words.containsWord(tableName)) {
                tableName = MessageFormat.format(config.getWrapKeyword(), tableName);
            }
            entityTable.setName(tableName);
        }
        entityTable.setEntityClassColumns(new LinkedHashSet<>());
        entityTable.setEntityClassPKColumns(new LinkedHashSet<>());
        // 处理所有列
        List<EntityField> fields;
        if (config.isEnableMethodAnnotation()) {
            fields = FieldBuilder.getAll(entityClass);
        } else {
            fields = FieldBuilder.getFields(entityClass);
        }
        for (EntityField field : fields) {
            // 如果启用了简单类型，就做简单类型校验，如果不是简单类型，直接跳过
            if (!config.isUseSimpleType() // 关闭简单类型限制时，所有字段都处理
                    || (config.isUseSimpleType() && SimpleType.isSimpleType(field.getJavaType())) // 开启简单类型时只处理包含的简单类型
                    || field.isAnnotationPresent(Column.class) // 有注解的处理，不考虑类型
                    || field.isAnnotationPresent(ColumnType.class) // 有注解的处理，不考虑类型
                    || (config.isEnumAsSimpleType() && Enum.class.isAssignableFrom(field.getJavaType()))) { //开启枚举作为简单类型时处理
                processField(entityTable, field, config, style);
            }
        }
        // 当pk.size=0的时候使用所有列作为主键
        if (entityTable.getEntityClassPKColumns().size() == 0) {
            entityTable.setEntityClassPKColumns(entityTable.getEntityClassColumns());
        }
        entityTable.initPropertyMap();
        return entityTable;
    }

    /**
     * 处理字段
     *
     * @param entityTable 对象表
     * @param field       字段
     * @param config      配置
     * @param style       样式
     */
    protected void processField(EntityTable entityTable, EntityField field, Config config, Style style) {
        // 排除字段
        if (field.isAnnotationPresent(Transient.class)) {
            return;
        }
        // Id
        EntityColumn entityColumn = new EntityColumn(entityTable);
        // 是否使用 {xx, javaType=xxx}
        entityColumn.setUseJavaType(config.isUseJavaType());
        // 记录 field 信息，方便后续扩展使用
        entityColumn.setEntityField(field);
        if (field.isAnnotationPresent(Id.class)) {
            entityColumn.setId(true);
        }
        // Column
        String columnName = null;
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            columnName = column.name();
            entityColumn.setUpdatable(column.updatable());
            entityColumn.setInsertable(column.insertable());
        }
        // ColumnType
        if (field.isAnnotationPresent(ColumnType.class)) {
            ColumnType columnType = field.getAnnotation(ColumnType.class);
            // 是否为 blob 字段
            entityColumn.setBlob(columnType.isBlob());
            // column可以起到别名的作用
            if (StringKit.isEmpty(columnName) && StringKit.isNotEmpty(columnType.column())) {
                columnName = columnType.column();
            }
            if (columnType.jdbcType() != JdbcType.UNDEFINED) {
                entityColumn.setJdbcType(columnType.jdbcType());
            }
            if (columnType.typeHandler() != UnknownTypeHandler.class) {
                entityColumn.setTypeHandler(columnType.typeHandler());
            }
        }
        // 列名
        if (StringKit.isEmpty(columnName)) {
            columnName = convertByStyle(field.getName(), style);
        }
        // 自动处理关键字
        if (StringKit.isNotEmpty(config.getWrapKeyword()) && Words.containsWord(columnName)) {
            columnName = MessageFormat.format(config.getWrapKeyword(), columnName);
        }
        entityColumn.setProperty(field.getName());
        entityColumn.setColumn(columnName);
        entityColumn.setJavaType(field.getJavaType());
        if (field.getJavaType().isPrimitive()) {
            Logger.warn("通用 Mapper 警告信息: <[" + entityColumn + "]> 使用了基本类型，基本类型在动态 SQL 中由于存在默认值，因此任何时候都不等于 null，建议修改基本类型为对应的包装类型!");
        }
        // OrderBy
        processOrderBy(entityTable, field, entityColumn);
        // 处理主键策略
        processKeyGenerator(entityTable, field, entityColumn);
        entityTable.getEntityClassColumns().add(entityColumn);
        if (entityColumn.isId()) {
            entityTable.getEntityClassPKColumns().add(entityColumn);
        }
    }

    /**
     * 处理排序
     *
     * @param entityTable  对象表
     * @param field        字段信息
     * @param entityColumn 对象列
     */
    protected void processOrderBy(EntityTable entityTable, EntityField field, EntityColumn entityColumn) {
        String orderBy = "";
        if (field.isAnnotationPresent(OrderBy.class)) {
            orderBy = field.getAnnotation(OrderBy.class).value();
            if ("".equals(orderBy)) {
                orderBy = "ASC";
            }
            Logger.warn(OrderBy.class + " is outdated, use " + Order.class + " instead!");
        }
        if (field.isAnnotationPresent(Order.class)) {
            Order order = field.getAnnotation(Order.class);
            if ("".equals(order.value()) && "".equals(orderBy)) {
                orderBy = "ASC";
            } else {
                orderBy = order.value();
            }
            entityColumn.setOrderPriority(order.priority());
        }
        if (StringKit.isNotEmpty(orderBy)) {
            entityColumn.setOrderBy(orderBy);
        }
    }

    /**
     * 处理主键策略
     *
     * @param entityTable  对象表
     * @param field        字段信息
     * @param entityColumn 对象列
     */
    protected void processKeyGenerator(EntityTable entityTable, EntityField field, EntityColumn entityColumn) {
        // KeySql 优先级最高
        if (field.isAnnotationPresent(KeySql.class)) {
            processKeySql(entityTable, entityColumn, field.getAnnotation(KeySql.class));
        } else if (field.isAnnotationPresent(GeneratedValue.class)) {
            // 执行 sql - selectKey
            processGeneratedValue(entityTable, entityColumn, field.getAnnotation(GeneratedValue.class));
        }
    }

    /**
     * 处理 GeneratedValue 注解
     *
     * @param entityTable    对象表
     * @param entityColumn   对象列
     * @param generatedValue 注解
     */
    protected void processGeneratedValue(EntityTable entityTable, EntityColumn entityColumn, GeneratedValue generatedValue) {
        if ("JDBC".equals(generatedValue.generator())) {
            entityColumn.setIdentity(true);
            entityColumn.setGenerator("JDBC");
            entityTable.setKeyProperties(entityColumn.getProperty());
            entityTable.setKeyColumns(entityColumn.getColumn());
        } else {
            // 允许通过generator来设置获取id的sql,例如mysql=CALL IDENTITY(),hsqldb=SELECT SCOPE_IDENTITY()
            // 允许通过拦截器参数设置公共的generator
            if (generatedValue.strategy() == GenerationType.IDENTITY) {
                // mysql的自动增长
                entityColumn.setIdentity(true);
                if (!"".equals(generatedValue.generator())) {
                    String generator;
                    Identity identity = Identity.getDatabaseDialect(generatedValue.generator());
                    if (identity != null) {
                        generator = identity.getIdentityRetrievalStatement();
                    } else {
                        generator = generatedValue.generator();
                    }
                    entityColumn.setGenerator(generator);
                }
            } else {
                throw new InternalException(entityColumn.getProperty()
                        + " - 该字段@GeneratedValue配置只允许以下几种形式:" +
                        "\n1.useGeneratedKeys的@GeneratedValue(generator=\\\"JDBC\\\")  " +
                        "\n2.类似mysql数据库的@GeneratedValue(strategy=GenerationType.IDENTITY[,generator=\"Mysql\"])");
            }
        }
    }

    /**
     * 处理 KeySql 注解
     *
     * @param entityTable  对象表
     * @param entityColumn 对象列
     * @param keySql       sql
     */
    protected void processKeySql(EntityTable entityTable, EntityColumn entityColumn, KeySql keySql) {
        if (keySql.useGeneratedKeys()) {
            entityColumn.setIdentity(true);
            entityColumn.setGenerator("JDBC");
            entityTable.setKeyProperties(entityColumn.getProperty());
            entityTable.setKeyColumns(entityColumn.getColumn());
        } else if (keySql.dialect() == Identity.DEFAULT) {
            entityColumn.setIdentity(true);
            entityColumn.setOrder(org.miaixz.bus.mapper.criteria.Order.AFTER);
        } else if (keySql.dialect() != Identity.NULL) {
            //自动增长
            entityColumn.setIdentity(true);
            entityColumn.setOrder(org.miaixz.bus.mapper.criteria.Order.AFTER);
            entityColumn.setGenerator(keySql.dialect().getIdentityRetrievalStatement());
        } else if (StringKit.isNotEmpty(keySql.sql())) {

            entityColumn.setIdentity(true);
            entityColumn.setOrder(keySql.order());
            entityColumn.setGenerator(keySql.sql());
        } else if (keySql.genSql() != GenSql.NULL.class) {
            entityColumn.setIdentity(true);
            entityColumn.setOrder(keySql.order());
            try {
                GenSql genSql = keySql.genSql().getConstructor().newInstance();
                entityColumn.setGenerator(genSql.genSql(entityTable, entityColumn));
            } catch (Exception e) {
                Logger.error("实例化 GenSql 失败: " + e, e);
                throw new InternalException("实例化 GenSql 失败: " + e, e);
            }
        } else if (keySql.genId() != GenId.NULL.class) {
            entityColumn.setIdentity(false);
            entityColumn.setGenIdClass(keySql.genId());
        } else {
            throw new InternalException(entityTable.getEntityClass().getName()
                    + " 类中的 @KeySql 注解配置无效!");
        }
    }

}
