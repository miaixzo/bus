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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A wrapper class for {@link BoundSql} to facilitate manipulation of the MyBatis BoundSql object.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapperBoundSql {

    /**
     * The meta-object for reflecting on the BoundSql object.
     */
    private final MetaObject boundSql;

    /**
     * The original BoundSql object.
     */
    private final BoundSql delegate;

    /**
     * Constructs a new MapperBoundSql.
     * 
     * @param boundSql The original BoundSql object.
     */
    MapperBoundSql(BoundSql boundSql) {
        this.delegate = boundSql;
        this.boundSql = AbstractSqlHandler.getMetaObject(boundSql);
    }

    /**
     * Gets the SQL statement.
     * 
     * @return The SQL statement string.
     */
    public String sql() {
        return delegate.getSql();
    }

    /**
     * Sets the SQL statement.
     * 
     * @param sql The SQL statement to set.
     */
    public void sql(String sql) {
        boundSql.setValue("sql", sql);
    }

    /**
     * Gets a copy of the parameter mappings.
     * 
     * @return A copy of the list of parameter mappings.
     */
    public List<ParameterMapping> parameterMappings() {
        return ListKit.of(delegate.getParameterMappings());
    }

    /**
     * Sets the parameter mappings.
     * 
     * @param parameterMappings The list of parameter mappings to set.
     */
    public void parameterMappings(List<ParameterMapping> parameterMappings) {
        boundSql.setValue("parameterMappings", Collections.unmodifiableList(parameterMappings));
    }

    /**
     * Gets the parameter object.
     * 
     * @return The parameter object.
     */
    public Object parameterObject() {
        return get("parameterObject");
    }

    /**
     * Gets the map of additional parameters.
     * 
     * @return A map of additional parameters.
     */
    public Map<String, Object> additionalParameters() {
        return get("additionalParameters");
    }

    /**
     * A generic method to get a property value.
     * 
     * @param property The name of the property.
     * @param <T>      The type of the return value.
     * @return The value of the property.
     */
    private <T> T get(String property) {
        return (T) boundSql.getValue(property);
    }

}
