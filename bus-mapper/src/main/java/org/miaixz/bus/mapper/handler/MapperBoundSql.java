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
package org.miaixz.bus.mapper.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

/**
 * BoundSql的封装类，用于操作MyBatis的BoundSql对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperBoundSql {

    /**
     * BoundSql的反射元对象
     */
    private final MetaObject boundSql;

    /**
     * 原始BoundSql对象
     */
    private final BoundSql delegate;

    /**
     * 构造函数，初始化MapperBoundSql
     * 
     * @param boundSql 原始BoundSql对象
     */
    MapperBoundSql(BoundSql boundSql) {
        this.delegate = boundSql;
        this.boundSql = AbstractSqlHandler.getMetaObject(boundSql);
    }

    /**
     * 获取SQL语句
     * 
     * @return SQL语句字符串
     */
    public String sql() {
        return delegate.getSql();
    }

    /**
     * 设置SQL语句
     * 
     * @param sql 要设置的SQL语句
     */
    public void sql(String sql) {
        boundSql.setValue("sql", sql);
    }

    /**
     * 获取参数映射列表
     * 
     * @return 参数映射列表的副本
     */
    public List<ParameterMapping> parameterMappings() {
        List<ParameterMapping> parameterMappings = delegate.getParameterMappings();
        return new ArrayList<>(parameterMappings);
    }

    /**
     * 设置参数映射列表
     * 
     * @param parameterMappings 要设置的参数映射列表
     */
    public void parameterMappings(List<ParameterMapping> parameterMappings) {
        boundSql.setValue("parameterMappings", Collections.unmodifiableList(parameterMappings));
    }

    /**
     * 获取参数对象
     * 
     * @return 参数对象
     */
    public Object parameterObject() {
        return get("parameterObject");
    }

    /**
     * 获取附加参数映射
     * 
     * @return 附加参数的Map
     */
    public Map<String, Object> additionalParameters() {
        return get("additionalParameters");
    }

    /**
     * 通用获取属性方法
     * 
     * @param property 属性名称
     * @return 属性值
     * @param <T> 返回值类型
     */
    private <T> T get(String property) {
        return (T) boundSql.getValue(property);
    }

}