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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Default dialect for unknown databases.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultDialect extends AbstractDialect {

    /**
     * Creates the fallback dialect used when no concrete JDBC URL match is available.
     */
    public DefaultDialect() {
        super("Unknown", Normal.EMPTY);
    }

    /**
     * Indicates that the fallback dialect never resolves any JDBC URL.
     *
     * @param jdbcUrl the JDBC URL to inspect
     * @return always {@code null}
     */
    @Override
    public Dialect resolve(String jdbcUrl) {
        return null;
    }

    /**
     * Returns the UPSERT kind for the fallback dialect.
     *
     * @return {@link Behavior#NONE}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.NONE;
    }

    /**
     * Returns the original SQL unchanged because the unknown dialect cannot safely apply pagination rules.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the original SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        return originalSql;
    }

}
