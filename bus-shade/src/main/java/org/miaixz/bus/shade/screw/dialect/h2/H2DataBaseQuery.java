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
package org.miaixz.bus.shade.screw.dialect.h2;

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
 * H2 database query implementation. This class is a placeholder for H2 support, and all methods currently throw an
 * {@link InternalException} to indicate that the functionality is not yet implemented.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class H2DataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs an {@code H2DataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public H2DataBaseQuery(DataSource dataSource) {
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
