/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.mapper.entity;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.mapper.criteria.Assert;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * 数据库表对应的列
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public class EntityColumn {

    private EntityTable table;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private Class<? extends TypeHandler<?>> typeHandler;
    private String sequenceName;
    private boolean id = false;
    private boolean uuid = false;
    private boolean identity = false;
    private String generator;
    //排序
    private String orderBy;
    //可插入
    private boolean insertable = true;
    //可更新
    private boolean updatable = true;
    /**
     * 对应的字段信息
     */
    private EntityField entityField;

    public EntityColumn() {
    }

    public EntityColumn(EntityTable table) {
        this.table = table;
    }

    /**
     * 返回格式如:colum = #{age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
     *
     * @param entityName 名称
     * @return string
     */
    public String getColumnEqualsHolder(String entityName) {
        return this.column + " = " + getColumnHolder(entityName);
    }

    /**
     * 返回格式如:#{entityName.age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
     *
     * @param entityName 名称
     * @return string
     */
    public String getColumnHolder(String entityName) {
        return getColumnHolder(entityName, null);
    }

    /**
     * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
     *
     * @param entityName 名称
     * @param suffix     后缀
     * @return the string
     */
    public String getColumnHolder(String entityName, String suffix) {
        return getColumnHolder(entityName, null, null);
    }

    /**
     * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler},
     *
     * @param entityName 名称
     * @param suffix     后缀
     * @return the string
     */
    public String getColumnHolderWithComma(String entityName, String suffix) {
        return getColumnHolder(entityName, suffix, Symbol.COMMA);
    }

    /**
     * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler}+separator
     *
     * @param entityName 名称
     * @param suffix     后缀
     * @param separator  分隔符
     * @return the string
     */
    public String getColumnHolder(String entityName, String suffix, String separator) {
        StringBuffer sb = new StringBuffer("#{");
        if (Assert.isNotEmpty(entityName)) {
            sb.append(entityName);
            sb.append(Symbol.DOT);
        }
        sb.append(this.property);
        if (Assert.isNotEmpty(suffix)) {
            sb.append(suffix);
        }
        if (null != this.jdbcType) {
            sb.append(",jdbcType=");
            sb.append(this.jdbcType.toString());
        } else if (null != this.typeHandler) {
            sb.append(",typeHandler=");
            sb.append(this.typeHandler.getCanonicalName());
        } else if (!this.javaType.isArray()) {//当类型为数组时,不设置javaType#103
            sb.append(",javaType=");
            sb.append(javaType.getCanonicalName());
        }
        sb.append(Symbol.BRACE_RIGHT);
        if (Assert.isNotEmpty(separator)) {
            sb.append(separator);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        EntityColumn that = (EntityColumn) o;

        if (id != that.id) return false;
        if (uuid != that.uuid) return false;
        if (identity != that.identity) return false;
        if (null != table ? !table.equals(that.table) : null != that.table) return false;
        if (null != property ? !property.equals(that.property) : null != that.property) return false;
        if (null != column ? !column.equals(that.column) : null != that.column) return false;
        if (null != javaType ? !javaType.equals(that.javaType) : null != that.javaType) return false;
        if (jdbcType != that.jdbcType) return false;
        if (null != typeHandler ? !typeHandler.equals(that.typeHandler) : null != that.typeHandler) return false;
        if (null != sequenceName ? !sequenceName.equals(that.sequenceName) : null != that.sequenceName) return false;
        if (null != generator ? !generator.equals(that.generator) : null != that.generator) return false;
        return !(null != orderBy ? !orderBy.equals(that.orderBy) : null != that.orderBy);

    }

    @Override
    public int hashCode() {
        int result = null != table ? table.hashCode() : 0;
        result = 31 * result + (null != property ? property.hashCode() : 0);
        result = 31 * result + (null != column ? column.hashCode() : 0);
        result = 31 * result + (null != javaType ? javaType.hashCode() : 0);
        result = 31 * result + (null != jdbcType ? jdbcType.hashCode() : 0);
        result = 31 * result + (null != typeHandler ? typeHandler.hashCode() : 0);
        result = 31 * result + (null != sequenceName ? sequenceName.hashCode() : 0);
        result = 31 * result + (id ? 1 : 0);
        result = 31 * result + (uuid ? 1 : 0);
        result = 31 * result + (identity ? 1 : 0);
        result = 31 * result + (null != generator ? generator.hashCode() : 0);
        result = 31 * result + (null != orderBy ? orderBy.hashCode() : 0);
        return result;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumnEqualsHolder() {
        return getColumnEqualsHolder(null);
    }

    public String getColumnHolder() {
        return getColumnHolder(null);
    }

    public EntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(EntityField entityField) {
        this.entityField = entityField;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public EntityTable getTable() {
        return table;
    }

    public void setTable(EntityTable table) {
        this.table = table;
    }

    public Class<? extends TypeHandler<?>> getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(Class<? extends TypeHandler<?>> typeHandler) {
        this.typeHandler = typeHandler;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean isUuid() {
        return uuid;
    }

    public void setUuid(boolean uuid) {
        this.uuid = uuid;
    }
}
