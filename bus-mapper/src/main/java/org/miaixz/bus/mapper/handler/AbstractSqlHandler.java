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
package org.miaixz.bus.mapper.handler;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.miaixz.bus.core.lang.Symbol;

/**
 * SQL 拦截处理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSqlHandler implements SQLHandler {

    public static final String DELEGATE_BOUNDSQL = "delegate.boundSql";
    public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";
    public static final String DELEGATE_MAPPEDSTATEMENT = "delegate.mappedStatement";
    public static final String MAPPEDSTATEMENT = "mappedStatement";

    /**
     * SQL 解析缓存 key 可能是 mappedStatement 的 ID,也可能是 class 的 name
     */
    private static final Map<String, Boolean> SQL_PARSER_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 SqlParser 注解信息
     *
     * @param metaObject 元数据对象
     * @return the true/false
     */
    protected static boolean getSqlParserInfo(MetaObject metaObject) {
        String id = getMappedStatement(metaObject).getId();
        Boolean value = SQL_PARSER_CACHE.get(id);
        if (null != value) {
            return value;
        }
        String mapperName = id.substring(0, id.lastIndexOf(Symbol.DOT));
        return SQL_PARSER_CACHE.getOrDefault(mapperName, false);
    }

    /**
     * 获取当前执行 MappedStatement
     *
     * @param metaObject 元对象
     * @return 映射语句
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement) metaObject.getValue(DELEGATE_MAPPEDSTATEMENT);
    }

    /**
     * 获取当前执行 MappedStatement
     *
     * @param metaObject 元对象
     * @param property   元素属性
     * @return 映射语句
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject, String property) {
        return (MappedStatement) metaObject.getValue(property);
    }

    /**
     * 获得真正的处理对象,可能多层代理
     *
     * @param <T>    泛型
     * @param target 对象
     * @return the object
     */
    protected static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

}
