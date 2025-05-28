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

import java.util.concurrent.Executor;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

/**
 * 用于操作MyBatis的StatementHandler对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperStatementHandler {

    /**
     * StatementHandler的反射元对象
     */
    private final MetaObject statementHandler;

    /**
     * 构造函数，初始化MapperStatementHandler
     * 
     * @param statementHandler StatementHandler的MetaObject对象
     */
    MapperStatementHandler(MetaObject statementHandler) {
        this.statementHandler = statementHandler;
    }

    /**
     * 获取参数处理器
     * 
     * @return ParameterHandler对象
     */
    public ParameterHandler parameterHandler() {
        return get("parameterHandler");
    }

    /**
     * 获取映射语句
     * 
     * @return MappedStatement对象
     */
    public MappedStatement mappedStatement() {
        return get("mappedStatement");
    }

    /**
     * 获取执行器
     * 
     * @return Executor对象
     */
    public Executor executor() {
        return get("executor");
    }

    /**
     * 获取MapperBoundSql对象
     * 
     * @return MapperBoundSql对象
     */
    public MapperBoundSql mPBoundSql() {
        return new MapperBoundSql(boundSql());
    }

    /**
     * 获取BoundSql对象
     * 
     * @return BoundSql对象
     */
    public BoundSql boundSql() {
        return get("boundSql");
    }

    /**
     * 获取配置对象
     * 
     * @return Configuration对象
     */
    public Configuration configuration() {
        return get("configuration");
    }

    /**
     * 通用获取属性方法
     * 
     * @param property 属性名称
     * @return 属性值
     * @param <T> 返回值类型
     */
    private <T> T get(String property) {
        return (T) statementHandler.getValue(property);
    }

}