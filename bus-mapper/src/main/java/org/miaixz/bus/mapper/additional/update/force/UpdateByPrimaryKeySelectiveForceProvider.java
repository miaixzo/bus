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
package org.miaixz.bus.mapper.additional.update.force;

import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.VersionException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.annotation.Version;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;

/**
 * 通用Mapper接口, 更新, 强制，实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpdateByPrimaryKeySelectiveForceProvider extends MapperTemplate {

    public static final String FORCE_UPDATE_PROPERTIES = "forceUpdateProperties";

    public UpdateByPrimaryKeySelectiveForceProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    public String updateByPrimaryKeySelectiveForce(MappedStatement ms) {
        Class entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass), "record"));
        sql.append(this.updateSetColumnsForce(entityClass, "record", true, isNotEmpty()));
        sql.append(SqlBuilder.wherePKColumns(entityClass, "record", true));

        return sql.toString();
    }

    /**
     * update set列
     *
     * @param entityClass 实体Class对象
     * @param entityName  实体映射名
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public String updateSetColumnsForce(Class<?> entityClass, String entityName, boolean notNull, boolean notEmpty) {
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
                    throw new VersionException(
                            entityClass.getName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getName();
                    sql.append(column.getColumn()).append(" = ${@org.miaixz.bus.mapper.Version@nextVersion(")
                            .append(Symbol.AT).append(versionClass).append("@class, ");
                    // 虽然从函数调用上来看entityName必为"record"，但还是判断一下
                    if (StringKit.isNotEmpty(entityName)) {
                        sql.append(entityName).append('.');
                    }
                    sql.append(column.getProperty()).append(")},");
                } else if (notNull) {
                    sql.append(this.getIfNotNull(entityName, column,
                            column.getColumnEqualsHolder(entityName) + Symbol.COMMA, notEmpty));
                } else {
                    sql.append(column.getColumnEqualsHolder(entityName)).append(Symbol.COMMA);
                }
            } else if (column.isId() && column.isUpdatable()) {
                sql.append(column.getColumn()).append(" = ").append(column.getColumn()).append(Symbol.COMMA);
            }
        }
        sql.append("</set>");
        return sql.toString();
    }

    /**
     * 判断自动!=null的条件结构
     *
     * @param entityName 实体映射名
     * @param column     列明
     * @param contents   内容
     * @param empty      是否为空
     * @return the string
     */
    public String getIfNotNull(String entityName, EntityColumn column, String contents, boolean empty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<choose>");
        sql.append("<when test=\"");
        if (StringKit.isNotEmpty(entityName)) {
            sql.append(entityName).append(".");
        }
        sql.append(column.getProperty()).append(" != null");
        if (empty && column.getJavaType().equals(String.class)) {
            sql.append(" and ");
            if (StringKit.isNotEmpty(entityName)) {
                sql.append(entityName).append(".");
            }
            sql.append(column.getProperty()).append(" != '' ");
        }
        sql.append("\">");
        sql.append(contents);
        sql.append("</when>");

        // 指定的字段会被强制更新
        sql.append("<when test=\"");
        sql.append(FORCE_UPDATE_PROPERTIES).append(" != null and ").append(FORCE_UPDATE_PROPERTIES)
                .append(".contains('");
        sql.append(column.getProperty());
        sql.append("')\">");
        sql.append(contents);
        sql.append("</when>");

        sql.append("<otherwise></otherwise>");
        sql.append("</choose>");
        return sql.toString();
    }

}
