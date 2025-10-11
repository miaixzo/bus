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
 * @since Java 17+
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
