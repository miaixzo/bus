/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
