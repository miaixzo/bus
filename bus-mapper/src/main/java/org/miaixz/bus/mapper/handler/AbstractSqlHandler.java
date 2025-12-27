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
import org.miaixz.bus.core.xyz.FieldKit;

/**
 * An abstract base class for SQL interception and handling in MyBatis.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSqlHandler {

    /**
     * The property path for the `boundSql` field within a delegate proxy object.
     */
    public static final String DELEGATE_BOUNDSQL = "delegate.boundSql";

    /**
     * The property path for the `boundSql.sql` field within a delegate proxy object.
     */
    public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";

    /**
     * The property path for the `mappedStatement` field within a delegate proxy object.
     */
    public static final String DELEGATE_MAPPEDSTATEMENT = "delegate.mappedStatement";

    /**
     * The property key for the `mappedStatement` field.
     */
    public static final String MAPPEDSTATEMENT = "mappedStatement";

    /**
     * The default reflector factory used by MyBatis for reflection.
     */
    public static final DefaultReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

    /**
     * A cache for the results of SQL parser annotation checks, keyed by the MappedStatement ID or class name.
     */
    private static final Map<String, Boolean> SQL_PARSER_CACHE = new ConcurrentHashMap<>();

    /**
     * Checks if a `SqlParser` annotation is present for the given {@link MetaObject}.
     *
     * @param metaObject The meta-object containing the mapped statement.
     * @return {@code true} if a `SqlParser` annotation is present, {@code false} otherwise.
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
     * Gets the {@link MappedStatement} from the specified {@link MetaObject}.
     *
     * @param metaObject The meta-object containing the mapped statement.
     * @return The {@link MappedStatement} object.
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement) metaObject.getValue(DELEGATE_MAPPEDSTATEMENT);
    }

    /**
     * Gets the {@link MappedStatement} from a property path within the specified {@link MetaObject}.
     *
     * @param metaObject The meta-object containing the mapped statement.
     * @param property   The property path.
     * @return The {@link MappedStatement} object.
     */
    protected static MappedStatement getMappedStatement(MetaObject metaObject, String property) {
        return (MappedStatement) metaObject.getValue(property);
    }

    /**
     * Gets the real target object by unwrapping multiple layers of proxies.
     *
     * @param <T>    The type of the target object.
     * @param target The proxy object.
     * @return The real target object.
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
     * Gets the metadata information for an object.
     *
     * @param object The target object.
     * @return The {@link MetaObject} for the object.
     */
    public static MetaObject getMetaObject(Object object) {
        return MetaObject.forObject(
                object,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                DEFAULT_REFLECTOR_FACTORY);
    }

    /**
     * Sets additional parameters for a {@link BoundSql} object.
     *
     * @param boundSql             The bound SQL object.
     * @param additionalParameters A map of additional parameters.
     */
    public static void setAdditionalParameter(BoundSql boundSql, Map<String, Object> additionalParameters) {
        additionalParameters.forEach(boundSql::setAdditionalParameter);
    }

    /**
     * Creates a new {@link MapperBoundSql} instance.
     *
     * @param boundSql The bound SQL object.
     * @return A new {@link MapperBoundSql} instance.
     */
    public static MapperBoundSql mapperBoundSql(BoundSql boundSql) {
        return new MapperBoundSql(boundSql);
    }

    /**
     * Creates a new {@link MapperStatementHandler} instance.
     *
     * @param statementHandler The statement handler.
     * @return A new {@link MapperStatementHandler} instance.
     */
    public static MapperStatementHandler mapperStatementHandler(StatementHandler statementHandler) {
        statementHandler = realTarget(statementHandler);
        MetaObject object = getMetaObject(statementHandler);
        return new MapperStatementHandler(getMetaObject(object.getValue("delegate")));
    }

    /**
     * Modifies the SQL in a {@link BoundSql} object using reflection.
     *
     * <p>
     * This method uses reflection to directly modify the {@code sql} field in the BoundSql object, which is a private
     * field. This is useful for interceptors that need to modify SQL at runtime.
     * </p>
     *
     * @param boundSql The BoundSql object to modify.
     * @param newSql   The new SQL string to set.
     * @return {@code true} if the modification was successful, {@code false} otherwise.
     */
    protected static boolean setBoundSql(BoundSql boundSql, String newSql) {
        try {
            FieldKit.setFieldValue(boundSql, "sql", newSql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
