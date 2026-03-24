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
package org.miaixz.bus.shade.screw.dialect.db2;

import java.util.List;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;
import org.miaixz.bus.shade.screw.metadata.Table;

/**
 * DB2 database query implementation. This class is a placeholder for DB2 support, and all methods currently throw an
 * {@link InternalException} to indicate that the functionality is not yet implemented.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Db2DataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs a {@code Db2DataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public Db2DataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * This method is not supported for this database type.
     *
     * @return Never returns, always throws an exception.
     * @throws InternalException always, as this database type is not supported.
     */
    @Override
    public Database getDataBase() throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * This method is not supported for this database type.
     *
     * @return Never returns, always throws an exception.
     * @throws InternalException always, as this database type is not supported.
     */
    @Override
    public List<Table> getTables() {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * This method is not supported for this database type.
     *
     * @param table The name of the table.
     * @return Never returns, always throws an exception.
     * @throws InternalException always, as this database type is not supported.
     */
    @Override
    public List<Column> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * This method is not supported for this database type.
     *
     * @return Never returns, always throws an exception.
     * @throws InternalException always, as this database type is not supported.
     */
    @Override
    public List<? extends Column> getTableColumns() throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * This method is not supported for this database type.
     *
     * @param table The name of the table.
     * @return Never returns, always throws an exception.
     * @throws InternalException always, as this database type is not supported.
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

}
