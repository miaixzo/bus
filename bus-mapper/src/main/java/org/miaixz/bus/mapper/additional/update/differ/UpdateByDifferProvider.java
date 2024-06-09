/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.additional.update.differ;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.VersionException;
import org.miaixz.bus.mapper.annotation.Version;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;

import java.util.Set;

/**
 * 更新
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpdateByDifferProvider extends MapperTemplate {

    public static final String OLD = "old";
    public static final String NEWER = "newer";

    public UpdateByDifferProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 差异更新
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByDiffer(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass)));
        sql.append(updateSetColumnsByDiffer(entityClass));
        sql.append(wherePKColumns(entityClass, true));
        return sql.toString();
    }

    /**
     * where主键条件
     *
     * @param entityClass 实体Class对象
     * @param useVersion  版本条件
     * @return the string
     */
    public String wherePKColumns(Class<?> entityClass, boolean useVersion) {
        StringBuilder sql = new StringBuilder();
        sql.append("<where>");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getPKColumns(entityClass);
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            sql.append(" AND " + column.getColumnEqualsHolder(NEWER));
        }
        if (useVersion) {
            sql.append(whereVersion(entityClass));
        }
        sql.append("</where>");
        return sql.toString();
    }


    /**
     * 乐观锁字段条件
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public String whereVersion(Class<?> entityClass) {
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        boolean hasVersion = false;
        String result = Normal.EMPTY;
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (hasVersion) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                hasVersion = true;
                result = " AND " + column.getColumnEqualsHolder(NEWER);
            }
        }
        return result;
    }

    /**
     * update set列
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public String updateSetColumnsByDiffer(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<set>");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        // 对乐观锁的支持
        EntityColumn versionColumn = null;
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (versionColumn != null) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getName();
                    sql.append(column.getColumn()).append(" = ${@org.miaixz.bus.mapper.Version@nextVersion(")
                            .append(Symbol.AT).append(versionClass).append("@class, ")
                            .append(NEWER).append('.').append(column.getProperty()).append(")},");
                } else {
                    sql.append(getIfNotEqual(column, column.getColumnEqualsHolder(NEWER) + Symbol.COMMA));
                }
            }
        }
        sql.append("</set>");
        return sql.toString();
    }

    /**
     * 判断自动!=null的条件结构
     *
     * @param column   列
     * @param contents 内容
     * @return the string
     */
    public String getIfNotEqual(EntityColumn column, String contents) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"").append(OLD).append(".").append(column.getProperty());
        sql.append(" != ").append(NEWER).append(".").append(column.getProperty()).append("\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

}
