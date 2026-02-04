/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.support.keygen;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Wraps an {@link SqlSource} to enable primary key generation before insertion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenIdSqlSource implements SqlSource {

    /**
     * The original SQL source.
     */
    private final SqlSource sqlSource;

    /**
     * The primary key generator.
     */
    private final GenIdKeyGenerator keyGenerator;

    /**
     * Constructs a new GenIdSqlSource, initializing the SQL source and key generator.
     *
     * @param sqlSource    The original SQL source.
     * @param keyGenerator The primary key generator.
     */
    public GenIdSqlSource(SqlSource sqlSource, GenIdKeyGenerator keyGenerator) {
        this.sqlSource = sqlSource;
        this.keyGenerator = keyGenerator;
    }

    /**
     * Gets the bound SQL and generates the primary key if necessary before execution.
     *
     * @param parameterObject The parameter object.
     * @return The bound SQL object.
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // Ensure primary key generation for the first time if missed during initialization.
        keyGenerator.prepare(parameterObject);
        return sqlSource.getBoundSql(parameterObject);
    }

}
