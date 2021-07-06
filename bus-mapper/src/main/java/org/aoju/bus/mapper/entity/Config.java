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

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.criteria.Identity;
import org.aoju.bus.mapper.criteria.SimpleType;
import org.aoju.bus.mapper.criteria.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 通用Mapper属性配置
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
public class Config {

    public static final String PREFIX = "mapper";

    private List<Class> mappers = new ArrayList<>();
    private String UUID;
    private String identity;
    private String seqFormat;
    private String catalog;
    private String schema;
    private boolean BEFORE;
    //校验调用方法时,Condition(entityClass)和Mapper<EntityClass>是否一致
    private boolean checkEntityClass;
    //使用简单类型
    private boolean useSimpleType = true;

    private boolean enumAsSimpleType;
    /**
     * 是否支持方法上的注解,默认false
     */
    private boolean enableMethodAnnotation;
    /**
     * 对于一般的getAllIfColumnNode,是否判断!='',默认不判断
     */
    private boolean notEmpty;
    /**
     * 字段转换风格,默认驼峰转下划线
     */
    private Style style;
    /**
     * 处理关键字,默认空,mysql可以设置为 `{0}`, sqlserver 为 [{0}],{0} 代表的列名
     */
    private String wrapKeyword = Normal.EMPTY;

    public String getCatalog() {
        return catalog;
    }

    /**
     * 设置全局的catalog,默认为空,如果设置了值,操作表时的sql会是catalog.tablename
     *
     * @param catalog string
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /**
     * 获取主键自增回写SQL
     *
     * @return string
     */
    public String getIdentity() {
        if (Assert.isNotEmpty(this.identity)) {
            return this.identity;
        }
        //针对mysql的默认值
        return Identity.MYSQL.getIdentityRetrievalStatement();
    }

    /**
     * 主键自增回写方法,默认值MYSQL,详细说明请看文档
     *
     * @param IDENTITY string
     */
    public void setIdentity(String IDENTITY) {
        Identity identity = Identity.getDatabaseDialect(IDENTITY);
        if (null != identity) {
            this.identity = identity.getIdentityRetrievalStatement();
        } else {
            this.identity = IDENTITY;
        }
    }

    /**
     * 获取表前缀,带catalog或schema
     *
     * @return string
     */
    public String getPrefix() {
        if (Assert.isNotEmpty(this.catalog)) {
            return this.catalog;
        }
        if (Assert.isNotEmpty(this.schema)) {
            return this.schema;
        }
        return Normal.EMPTY;
    }

    public String getSchema() {
        return schema;
    }

    /**
     * 设置全局的schema,默认为空,如果设置了值,操作表时的sql会是schema.tablename
     * 如果同时设置了catalog,优先使用catalog.tablename
     *
     * @param schema schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取序列格式化模板
     *
     * @return string
     */
    public String getSeqFormat() {
        if (Assert.isNotEmpty(this.seqFormat)) {
            return this.seqFormat;
        }
        return "{0}.nextval";
    }

    /**
     * 序列的获取规则,使用{num}格式化参数,默认值为{0}.nextval,针对Oracle
     * 可选参数一共3个,对应0,1,2,3分别为SequenceName,ColumnName, PropertyName,TableName
     *
     * @param seqFormat sql
     */
    public void setSeqFormat(String seqFormat) {
        this.seqFormat = seqFormat;
    }

    public Style getStyle() {
        return null == this.style ? Style.camelhump : this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * 获取UUID生成规则
     *
     * @return string
     */
    public String getUUID() {
        if (Assert.isNotEmpty(this.UUID)) {
            return this.UUID;
        }
        return "@java.util.UUID@randomUUID().toString().replace(\"-\", \"\")";
    }

    /**
     * 设置UUID生成策略
     * 配置UUID生成策略需要使用OGNL表达式
     * 默认值32位长度:@java.util.UUID@randomUUID().toString().replace("-", "")
     *
     * @param UUID id
     */
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getWrapKeyword() {
        return wrapKeyword;
    }

    public void setWrapKeyword(String wrapKeyword) {
        this.wrapKeyword = wrapKeyword;
    }

    public boolean isBEFORE() {
        return BEFORE;
    }

    public void setBEFORE(boolean BEFORE) {
        this.BEFORE = BEFORE;
    }

    public boolean isCheckEntityClass() {
        return checkEntityClass;
    }

    public void setCheckEntityClass(boolean checkEntityClass) {
        this.checkEntityClass = checkEntityClass;
    }

    public boolean isEnableMethodAnnotation() {
        return enableMethodAnnotation;
    }

    public void setEnableMethodAnnotation(boolean enableMethodAnnotation) {
        this.enableMethodAnnotation = enableMethodAnnotation;
    }

    public boolean isEnumAsSimpleType() {
        return enumAsSimpleType;
    }

    public void setEnumAsSimpleType(boolean enumAsSimpleType) {
        this.enumAsSimpleType = enumAsSimpleType;
    }

    public boolean isNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public boolean isUseSimpleType() {
        return useSimpleType;
    }

    public void setUseSimpleType(boolean useSimpleType) {
        this.useSimpleType = useSimpleType;
    }

    /**
     * 主键自增回写方法执行顺序,默认AFTER,可选值为(BEFORE|AFTER)
     *
     * @param order string
     */
    public void setOrder(String order) {
        this.BEFORE = "BEFORE".equalsIgnoreCase(order);
    }

    public List<Class> getMappers() {
        return mappers;
    }

    public void setMappers(List<Class> mappers) {
        this.mappers = mappers;
    }

    public String getUuid() {
        return getUUID();
    }

    public void setUuid(String uuid) {
        setUUID(uuid);
    }

    public boolean isBefore() {
        return isBEFORE();
    }

    public void setBefore(boolean before) {
        setBEFORE(before);
    }

    /**
     * 配置属性
     *
     * @param properties 属性
     */
    public void setProperties(Properties properties) {
        if (null == properties) {
            //默认驼峰
            this.style = Style.camelhump;
            return;
        }
        String UUID = properties.getProperty("UUID");
        if (Assert.isNotEmpty(UUID)) {
            setUUID(UUID);
        }
        String identity = properties.getProperty("IDENTITY");
        if (Assert.isNotEmpty(identity)) {
            setIdentity(identity);
        }
        String seqFormat = properties.getProperty("seqFormat");
        if (Assert.isNotEmpty(seqFormat)) {
            setSeqFormat(seqFormat);
        }
        String catalog = properties.getProperty("catalog");
        if (Assert.isNotEmpty(catalog)) {
            setCatalog(catalog);
        }
        String schema = properties.getProperty("schema");
        if (Assert.isNotEmpty(schema)) {
            setSchema(schema);
        }
        String ORDER = properties.getProperty("ORDER");
        if (Assert.isNotEmpty(ORDER)) {
            setOrder(ORDER);
        }
        this.notEmpty = Boolean.valueOf(properties.getProperty("notEmpty"));
        this.enableMethodAnnotation = Boolean.valueOf(properties.getProperty("enableMethodAnnotation"));
        this.checkEntityClass = Boolean.valueOf(properties.getProperty("checkEntityClass"));
        //默认值 true,所以要特殊判断
        String useSimpleTypeStr = properties.getProperty("useSimpleType");
        if (Assert.isNotEmpty(useSimpleTypeStr)) {
            this.useSimpleType = Boolean.valueOf(useSimpleTypeStr);
        }
        this.enumAsSimpleType = Boolean.valueOf(properties.getProperty("enumAsSimpleType"));
        //注册新的基本类型,以逗号隔开,使用全限定类名
        String simpleTypes = properties.getProperty("simpleTypes");
        if (Assert.isNotEmpty(simpleTypes)) {
            SimpleType.registerSimpleType(simpleTypes);
        }
        String styleStr = properties.getProperty("style");
        if (Assert.isNotEmpty(styleStr)) {
            try {
                this.style = Style.valueOf(styleStr);
            } catch (IllegalArgumentException e) {
                throw new InstrumentException(styleStr + "不是合法的Style值!");
            }
        } else {
            //默认驼峰
            this.style = Style.camelhump;
        }
        //处理关键字
        String wrapKeyword = properties.getProperty("wrapKeyword");
        if (Assert.isNotEmpty(wrapKeyword)) {
            this.wrapKeyword = wrapKeyword;
        }
    }

}
