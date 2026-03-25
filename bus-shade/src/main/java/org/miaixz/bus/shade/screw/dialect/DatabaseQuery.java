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
package org.miaixz.bus.shade.screw.dialect;

import java.util.List;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;
import org.miaixz.bus.shade.screw.metadata.Table;

/**
 * Common query interface for retrieving database metadata information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DatabaseQuery {

    /**
     * Retrieves database information.
     *
     * @return {@link Database} object containing database information.
     * @throws InternalException if an error occurs during the query.
     */
    Database getDataBase() throws InternalException;

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link Table} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    List<? extends Table> getTables() throws InternalException;

    /**
     * Retrieves column information for a specific table.
     *
     * @param table The name of the table.
     * @return A list of {@link Column} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    List<? extends Column> getTableColumns(String table) throws InternalException;

    /**
     * Retrieves column information for all tables in the database.
     *
     * @return A list of {@link Column} objects for all tables.
     * @throws InternalException if an error occurs during the query.
     */
    List<? extends Column> getTableColumns() throws InternalException;

    /**
     * Retrieves primary key information for a specific table.
     *
     * @param table The name of the table.
     * @return A list of {@link PrimaryKey} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException;

    /**
     * Retrieves primary key information for all tables in the database.
     *
     * @return A list of {@link PrimaryKey} objects for all tables.
     * @throws InternalException if an error occurs during the query.
     */
    List<? extends PrimaryKey> getPrimaryKeys() throws InternalException;

}
