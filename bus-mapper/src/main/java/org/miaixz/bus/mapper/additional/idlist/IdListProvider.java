/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.additional.idlist;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;

/**
 * 通过 ids 字符串的各种操作 ids 如 "1,2,3"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IdListProvider extends MapperTemplate {

    public IdListProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 保证 idList 不能为空
     *
     * @param list     列表
     * @param errorMsg 错误信息
     */
    public static void notEmpty(List<?> list, String errorMsg) {
        if (list == null || list.size() == 0) {
            throw new MapperException(errorMsg);
        }
    }

    /**
     * 根据主键字符串进行删除，类中只有存在一个带有@Id注解的字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByIdList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.deleteFromTable(entityClass, tableName(entityClass)));
        appendWhereIdList(sql, entityClass, getConfig().isSafeDelete());
        return sql.toString();
    }

    /**
     * 根据主键字符串进行查询，类中只有存在一个带有@Id注解的字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByIdList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        // 将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectAllColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        appendWhereIdList(sql, entityClass, isNotEmpty());
        return sql.toString();
    }

    /**
     * 拼接条件
     *
     * @param sql         字符串SQL
     * @param entityClass 实体Class对象
     */
    private void appendWhereIdList(StringBuilder sql, Class<?> entityClass, boolean notEmpty) {
        Set<EntityColumn> columnList = EntityBuilder.getPKColumns(entityClass);
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            if (notEmpty) {
                sql.append(
                        "<bind name=\"notEmptyListCheck\" value=\"@org.miaixz.bus.mapper.additional.idlist.IdListProvider@notEmpty(");
                sql.append("idList, 'idList 不能为空')\"/>");
            }
            sql.append("<where>");
            sql.append("<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"");
            sql.append(column.getColumn());
            sql.append(" in ");
            sql.append("(\" close=\")\">");
            sql.append("#{id}");
            sql.append("</foreach>");
            sql.append("</where>");
        } else {
            throw new MapperException("继承 ByIdList 方法的实体类[" + entityClass.getName() + "]中必须只有一个带有 @Id 注解的字段");
        }
    }

}
