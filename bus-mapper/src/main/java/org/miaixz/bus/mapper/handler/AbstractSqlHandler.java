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

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.miaixz.bus.core.lang.Symbol;

/**
 * MyBatis 中用于 SQL 拦截和处理的抽象基类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSqlHandler {

    /**
     * 代理对象中 boundSql 的属性路径。
     */
    public static final String DELEGATE_BOUNDSQL = "delegate.boundSql";

    /**
     * 代理对象中 boundSql.sql 的属性路径。
     */
    public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";

    /**
     * 代理对象中 mappedStatement 的属性路径。
     */
    public static final String DELEGATE_MAPPEDSTATEMENT = "delegate.mappedStatement";

    /**
     * mappedStatement 的属性键。
     */
    public static final String MAPPEDSTATEMENT = "mappedStatement";

    /**
     * MyBatis 反射使用的默认反射工厂。
     */
    public static final DefaultReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

    /**
     * SQL 解析注解结果缓存，键为 mappedStatement 的 ID 或类名。
     */
    private static final Map<String, Boolean> SQL_PARSER_CACHE = new ConcurrentHashMap<>();

    /**
     * 检查指定 MetaObject 是否存在 SqlParser 注解。
     *
     * @param metaObject 包含映射语句的元对象
     * @return 若存在 SqlParser 注解返回 true，否则返回 false
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
     * 从指定 MetaObject 获取 MappedStatement。
     *
     * @param metaObject 包含映射语句的元对象
     * @return MappedStatement 对象
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement) metaObject.getValue(DELEGATE_MAPPEDSTATEMENT);
    }

    /**
     * 从指定 MetaObject 的属性路径获取 MappedStatement。
     *
     * @param metaObject 包含映射语句的元对象
     * @param property   属性路径
     * @return MappedStatement 对象
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject, String property) {
        return (MappedStatement) metaObject.getValue(property);
    }

    /**
     * 获取真实的目标对象，解包多层代理。
     *
     * @param <T>    目标对象的类型
     * @param target 代理对象
     * @return 真实的目标对象
     */
    protected static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            Plugin plugin = (Plugin) Proxy.getInvocationHandler(target);
            MetaObject metaObject = getMetaObject(plugin);
            return realTarget(metaObject.getValue("target"));
        }
        return (T) target;
    }

    /**
     * 获取对象的元数据信息。
     *
     * @param object 目标对象
     * @return 元数据对象
     */
    public static MetaObject getMetaObject(Object object) {
        return MetaObject.forObject(object, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
    }

    /**
     * 为 BoundSql 设置附加参数。
     *
     * @param boundSql             绑定 SQL 对象
     * @param additionalParameters 附加参数映射
     */
    public static void setAdditionalParameter(BoundSql boundSql, Map<String, Object> additionalParameters) {
        additionalParameters.forEach(boundSql::setAdditionalParameter);
    }

    /**
     * 创建 MapperBoundSql 实例。
     *
     * @param boundSql 绑定 SQL 对象
     * @return MapperBoundSql 实例
     */
    public static MapperBoundSql mapperBoundSql(BoundSql boundSql) {
        return new MapperBoundSql(boundSql);
    }

    /**
     * 创建 MapperStatementHandler 实例。
     *
     * @param statementHandler 语句处理器
     * @return MapperStatementHandler 实例
     */
    public static MapperStatementHandler mapperStatementHandler(StatementHandler statementHandler) {
        statementHandler = realTarget(statementHandler);
        MetaObject object = getMetaObject(statementHandler);
        return new MapperStatementHandler(getMetaObject(object.getValue("delegate")));
    }

}