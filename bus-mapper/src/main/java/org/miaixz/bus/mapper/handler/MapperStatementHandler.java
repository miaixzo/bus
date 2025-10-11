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
 * A utility class for manipulating MyBatis {@link org.apache.ibatis.executor.statement.StatementHandler} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperStatementHandler {

    /**
     * The meta-object for reflecting on the StatementHandler.
     */
    private final MetaObject statementHandler;

    /**
     * Constructs a new MapperStatementHandler.
     * 
     * @param statementHandler The MetaObject for the StatementHandler.
     */
    MapperStatementHandler(MetaObject statementHandler) {
        this.statementHandler = statementHandler;
    }

    /**
     * Gets the parameter handler.
     * 
     * @return The {@link ParameterHandler} object.
     */
    public ParameterHandler parameterHandler() {
        return get("parameterHandler");
    }

    /**
     * Gets the mapped statement.
     * 
     * @return The {@link MappedStatement} object.
     */
    public MappedStatement mappedStatement() {
        return get("mappedStatement");
    }

    /**
     * Gets the executor.
     * 
     * @return The {@link Executor} object.
     */
    public Executor executor() {
        return get("executor");
    }

    /**
     * Gets the {@link MapperBoundSql} object.
     * 
     * @return The {@link MapperBoundSql} object.
     */
    public MapperBoundSql mapperBoundSql() {
        return new MapperBoundSql(boundSql());
    }

    /**
     * Gets the {@link BoundSql} object.
     * 
     * @return The {@link BoundSql} object.
     */
    public BoundSql boundSql() {
        return get("boundSql");
    }

    /**
     * Gets the configuration object.
     * 
     * @return The {@link Configuration} object.
     */
    public Configuration configuration() {
        return get("configuration");
    }

    /**
     * A generic method to get a property value.
     * 
     * @param property The name of the property.
     * @param <T>      The type of the return value.
     * @return The value of the property.
     */
    private <T> T get(String property) {
        return (T) statementHandler.getValue(property);
    }

}
