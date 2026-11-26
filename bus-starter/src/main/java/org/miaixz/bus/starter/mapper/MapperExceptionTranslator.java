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
package org.miaixz.bus.starter.mapper;

import org.mybatis.spring.MyBatisExceptionTranslator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import org.miaixz.bus.spring.jdbc.JdbcErrorTranslator;

/**
 * Classifies mapper failures once and exposes the Bus exception hierarchy to callers.
 *
 * @author Kimi Liu
 */
final class MapperExceptionTranslator implements PersistenceExceptionTranslator {

    /**
     * MyBatis error classifier used before a persistence failure is converted to a Bus exception.
     */
    private final MyBatisExceptionTranslator delegate;

    /**
     * Creates a lazily initialized MyBatis classifier for the effective data source.
     *
     * @param dataSource effective data source used by the mapper session
     */
    MapperExceptionTranslator(javax.sql.DataSource dataSource) {
        delegate = new MyBatisExceptionTranslator(dataSource, true);
    }

    /**
     * The Spring SPI only permits DataAccessException as a return type. A classified Bus exception is therefore thrown;
     * unrecognized failures retain MyBatis' normal handling.
     *
     * @param failure original mapper persistence failure
     * @return null when the error cannot be classified; recognized failures are thrown as Bus exceptions
     */
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException failure) {
        DataAccessException translated = delegate.translateExceptionIfPossible(failure);
        if (translated == null)
            return null;
        throw JdbcErrorTranslator.translate(translated);
    }

}
