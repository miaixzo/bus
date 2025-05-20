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

import static org.miaixz.bus.mapper.Args.DELIMITER;

import java.util.Objects;
import java.util.regex.Matcher;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.support.keysql.GenId;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 实体中字段和列的对应关系接口，记录字段上提供的列信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class ColumnMeta extends ConfigMeta<ColumnMeta> {

    /**
     * 实体类字段
     */
    protected final FieldMeta field;

    /**
     * 所在实体类
     */
    protected TableMeta entityTable;

    /**
     * 列名
     */
    protected String column;

    /**
     * 是否为主键
     */
    protected boolean id;

    /**
     * 是否可以为空
     */
    protected boolean nullable;

    /**
     * 主键策略1，优先级1：是否使用 JDBC 方式获取主键，优先级最高，设置为 true 后，不对其他配置校验
     */
    protected boolean useGeneratedKeys;

    /**
     * 主键策略2，优先级2：取主键的 SQL，当前SQL只能在 INSERT 语句执行后执行
     */
    protected String afterSql;

    /**
     * 主键策略3，优先级3：Java 方式生成主键，可与发号器服务配合使用
     */
    protected Class<? extends GenId> genId;

    /**
     * 执行 genId 的时机，仅当 genId 不为空时有效，默认插入前执行
     */
    protected boolean genIdExecuteBefore;

    /**
     * 排序方式
     */
    protected String orderBy;

    /**
     * 排序的优先级，数值越小优先级越高
     */
    protected int orderByPriority;

    /**
     * 是否查询字段
     */
    protected boolean selectable = true;

    /**
     * 是否插入字段
     */
    protected boolean insertable = true;

    /**
     * 是否更新字段
     */
    protected boolean updatable = true;

    /**
     * JDBC 类型
     */
    protected JdbcType jdbcType;

    /**
     * 类型处理器
     */
    protected Class<? extends TypeHandler> typeHandler;

    /**
     * 精度
     */
    protected String numericScale;

    /**
     * 构造函数，初始化 MapperColumn
     *
     * @param field 实体类字段
     */
    protected ColumnMeta(FieldMeta field) {
        this.field = field;
    }

    /**
     * 创建 MapperColumn 实例
     *
     * @param field 实体类字段
     * @return MapperColumn 实例
     */
    public static ColumnMeta of(FieldMeta field) {
        return new ColumnMeta(field);
    }

    /**
     * 设置实体表
     *
     * @param entityTable 实体表信息
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta entityTable(TableMeta entityTable) {
        this.entityTable = entityTable;
        return this;
    }

    /**
     * 设置列名
     *
     * @param column 列名
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta column(String column) {
        this.column = column;
        return this;
    }

    /**
     * 设置是否为主键
     *
     * @param id 是否为主键
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta id(boolean id) {
        this.id = id;
        return this;
    }

    /**
     * 设置是否可以为空
     *
     * @param nullable 是否可以为空
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta nullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * 设置是否使用 JDBC 方式获取主键
     *
     * @param useGeneratedKeys 是否使用 JDBC 方式
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta useGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
        return this;
    }

    /**
     * 设置取主键的 SQL
     *
     * @param afterSql 主键 SQL
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta afterSql(String afterSql) {
        this.afterSql = afterSql;
        return this;
    }

    /**
     * 设置 Java 方式生成主键的类
     *
     * @param genId 主键生成器类
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta genId(Class<? extends GenId> genId) {
        this.genId = genId;
        return this;
    }

    /**
     * 设置执行 genId 的时机
     *
     * @param genIdExecuteBefore 是否在插入前执行
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta genIdExecuteBefore(boolean genIdExecuteBefore) {
        this.genIdExecuteBefore = genIdExecuteBefore;
        return this;
    }

    /**
     * 设置排序方式
     *
     * @param orderBy 排序方式
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * 设置排序优先级
     *
     * @param orderByPriority 排序优先级
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta orderByPriority(int orderByPriority) {
        this.orderByPriority = orderByPriority;
        return this;
    }

    /**
     * 设置是否为查询字段
     *
     * @param selectable 是否查询字段
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * 设置是否为插入字段
     *
     * @param insertable 是否插入字段
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta insertable(boolean insertable) {
        this.insertable = insertable;
        return this;
    }

    /**
     * 设置是否为更新字段
     *
     * @param updatable 是否更新字段
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta updatable(boolean updatable) {
        this.updatable = updatable;
        return this;
    }

    /**
     * 设置 JDBC 类型
     *
     * @param jdbcType JDBC 类型
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta jdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    /**
     * 设置类型处理器
     *
     * @param typeHandler 类型处理器类
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta typeHandler(Class<? extends TypeHandler> typeHandler) {
        this.typeHandler = typeHandler;
        return this;
    }

    /**
     * 设置精度
     *
     * @param numericScale 精度
     * @return 当前 MapperColumn 实例
     */
    public ColumnMeta numericScale(String numericScale) {
        this.numericScale = numericScale;
        return this;
    }

    /**
     * 获取 Java 类型
     *
     * @return 字段的 Java 类型
     */
    public Class<?> javaType() {
        return field().getType();
    }

    /**
     * 获取属性名
     *
     * @return 属性名
     */
    public String property() {
        return property("");
    }

    /**
     * 获取带指定前缀的属性名
     *
     * @param prefix 指定前缀，需包含 "."
     * @return 带前缀的属性名
     */
    public String property(String prefix) {
        return prefix + field().getName();
    }

    /**
     * 返回 XML 变量形式 #{property}
     *
     * @return XML 变量字符串
     */
    public String variables() {
        return variables("");
    }

    /**
     * 返回带前缀的 XML 变量形式 #{prefixproperty}
     *
     * @param prefix 指定前缀，需包含 "."
     * @return 带前缀的 XML 变量字符串
     */
    public String variables(String prefix) {
        return "#{" + property(prefix) + jdbcTypeVariables().orElse("") + javaTypeVariables().orElse("")
                + typeHandlerVariables().orElse("") + numericScaleVariables().orElse("") + "}";
    }

    /**
     * 获取 Java 类型变量字符串，如 {, javaType=java.lang.String}
     *
     * @return Java 类型变量字符串的 Optional 包装对象
     */
    public Optional<String> javaTypeVariables() {
        Class<?> javaType = this.javaType();
        if (javaType != null) {
            return Optional.of(", javaType=" + javaType.getName());
        }
        return Optional.empty();
    }

    /**
     * 获取数据库类型变量字符串，如 {, jdbcType=VARCHAR}
     *
     * @return 数据库类型变量字符串的 Optional 包装对象
     */
    public Optional<String> jdbcTypeVariables() {
        if (this.jdbcType != null && this.jdbcType != JdbcType.UNDEFINED) {
            return Optional.of(", jdbcType=" + jdbcType);
        }
        return Optional.empty();
    }

    /**
     * 获取类型处理器变量字符串，如 {, typeHandler=XXTypeHandler}
     *
     * @return 类型处理器变量字符串的 Optional 包装对象
     */
    public Optional<String> typeHandlerVariables() {
        if (this.typeHandler != null && this.typeHandler != UnknownTypeHandler.class) {
            return Optional.of(", typeHandler=" + typeHandler.getName());
        }
        return Optional.empty();
    }

    /**
     * 获取精度变量字符串，如 {, numericScale=2}
     *
     * @return 精度变量字符串的 Optional 包装对象
     */
    public Optional<String> numericScaleVariables() {
        if (StringKit.isNotEmpty(this.numericScale)) {
            return Optional.of(", numericScale=" + numericScale);
        }
        return Optional.empty();
    }

    /**
     * 返回 column AS property 形式的字符串，当 column 和 property 相同时无别名
     *
     * @return column AS property 字符串
     */
    public String columnAsProperty() {
        return columnAsProperty("");
    }

    /**
     * 返回 column AS prefixproperty 形式的字符串
     *
     * @param prefix 指定前缀，需包含 "."
     * @return column AS prefixproperty 字符串
     */
    public String columnAsProperty(String prefix) {
        // 比较 column 和 property 时，忽略界定符（如 MySQL 中的 `order` 应视为与 field 的 order 相同）
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
     * 返回 column = #{property} 形式的字符串
     *
     * @return column = #{property} 字符串
     */
    public String columnEqualsProperty() {
        return columnEqualsProperty("");
    }

    /**
     * 返回带前缀的 column = #{prefixproperty} 形式的字符串
     *
     * @param prefix 指定前缀，需包含 "."
     * @return column = #{prefixproperty} 字符串
     */
    public String columnEqualsProperty(String prefix) {
        return column() + " = " + variables(prefix);
    }

    /**
     * 返回 property != null 形式的字符串
     *
     * @return property != null 字符串
     */
    public String notNullTest() {
        return notNullTest("");
    }

    /**
     * 返回带前缀的 prefixproperty != null 形式的字符串
     *
     * @param prefix 指定前缀，需包含 "."
     * @return prefixproperty != null 字符串
     */
    public String notNullTest(String prefix) {
        return property(prefix) + " != null";
    }

    /**
     * 当字段类型为 String 时，返回 property != null and property != '' 形式的字符串；其他类型与 notNullTest 相同
     *
     * @return 非空判断字符串
     */
    public String notEmptyTest() {
        return notEmptyTest("");
    }

    /**
     * 当字段类型为 String 时，返回 prefixproperty != null and prefixproperty != '' 形式的字符串；其他类型与 notNullTest 相同
     *
     * @param prefix 指定前缀，需包含 "."
     * @return 非空判断字符串
     */
    public String notEmptyTest(String prefix) {
        if (field().getType() == String.class) {
            return notNullTest(prefix) + " and " + property(prefix) + " != '' ";
        }
        return notNullTest();
    }

    /**
     * 判断当前字段是否设置了主键策略
     *
     * @return true 表示设置了主键策略，false 表示未设置
     */
    public boolean hasPrimaryKeyStrategy() {
        return id && (useGeneratedKeys || (afterSql != null && !afterSql.isEmpty())
                || (genId != null && genId != GenId.NULL.class));
    }

    /**
     * 判断两个 MapperColumn 对象是否相等
     *
     * @param o 比较对象
     * @return true 表示相等，false 表示不相等
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
     * 计算对象的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(column());
    }

    /**
     * 返回字符串表示形式，格式为 column = #{property}
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return columnEqualsProperty();
    }

}