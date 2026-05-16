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

import org.miaixz.bus.mapper.behavior.OptionsBehavior;
import org.miaixz.bus.mapper.behavior.PagingBehavior;
import org.miaixz.bus.mapper.behavior.SchemaBehavior;

/**
 * Database dialect interface providing database-specific pagination, UPSERT, and schema capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Dialect extends PagingBehavior, OptionsBehavior, SchemaBehavior {

    /**
     * Gets the database product name.
     *
     * @return the database product name
     */
    String getDatabase();

    /**
     * Resolves the supplied JDBC URL to a final dialect instance.
     *
     * <p>
     * Implementations should return {@code null} when the URL does not belong to the dialect. Stateless dialects
     * usually return {@code this} when the URL matches. Product-family dialects such as Polardb may inspect the URL
     * further and return a new resolved dialect instance that carries the concrete engine state needed for downstream
     * SQL generation.
     * </p>
     *
     * @param jdbcUrl the JDBC URL to resolve
     * @return the resolved dialect instance, or {@code null} if the URL does not belong to this dialect
     */
    Dialect resolve(String jdbcUrl);

}
