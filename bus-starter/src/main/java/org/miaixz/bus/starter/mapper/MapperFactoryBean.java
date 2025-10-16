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
package org.miaixz.bus.starter.mapper;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating MyBatis mapper interface proxies. It is configured via a SqlSessionFactory or a
 * pre-configured SqlSessionTemplate.
 *
 * @param <T> The type of the mapper interface.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

    private Class<T> mapperInterface;

    /**
     * Default constructor.
     */
    public MapperFactoryBean() {

    }

    /**
     * Constructor that sets the mapper interface.
     *
     * @param mapperInterface The class of the mapper interface.
     */
    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * Checks that the DAO configuration is valid. It ensures that the {@code mapperInterface} property is set and that
     * the mapper is registered with the MyBatis {@link Configuration}. If the mapper is not already registered, it will
     * be added.
     */
    @Override
    protected void checkDaoConfig() {
        super.checkDaoConfig();

        Assert.notNull(this.mapperInterface, "Property 'mapperInterface' is required");

        Configuration configuration = getSqlSession().getConfiguration();
        if (!configuration.hasMapper(this.mapperInterface)) {
            try {
                configuration.addMapper(this.mapperInterface);
            } catch (Exception e) {
                Logger.error("Error while adding the mapper '{}' to configuration.", this.mapperInterface, e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }

    /**
     * Returns the mapper proxy instance. This is the core method of the {@link FactoryBean} interface.
     *
     * @return The mapper proxy instance.
     */
    @Override
    public T getObject() {
        return getSqlSession().getMapper(this.mapperInterface);
    }

    /**
     * Returns the type of the mapper interface.
     *
     * @return The class of the mapper interface.
     */
    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    /**
     * Indicates that this factory bean produces a singleton object.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Gets the mapper interface type.
     *
     * @return The class of the mapper interface.
     */
    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    /**
     * Sets the mapper interface of the MyBatis mapper.
     *
     * @param mapperInterface The class of the mapper interface.
     */
    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

}
