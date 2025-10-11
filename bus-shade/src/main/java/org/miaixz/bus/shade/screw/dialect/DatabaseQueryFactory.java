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
package org.miaixz.bus.shade.screw.dialect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.exception.InternalException;

import lombok.Getter;
import lombok.Setter;

/**
 * A factory for creating {@link DatabaseQuery} instances based on the database type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class DatabaseQueryFactory implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * The JDBC data source used to determine the database type and create the query instance.
     */
    private DataSource dataSource;

    /**
     * Private constructor to prevent instantiation via the 'new' keyword.
     */
    private DatabaseQueryFactory() {

    }

    /**
     * Constructs a new {@code DatabaseQueryFactory} with the specified data source.
     *
     * @param source The {@link DataSource} to be used.
     */
    public DatabaseQueryFactory(DataSource source) {
        dataSource = source;
    }

    /**
     * Creates a new instance of a {@link DatabaseQuery} implementation suitable for the configured database type. It
     * determines the database type from the connection URL and instantiates the corresponding query class.
     *
     * @return A new {@link DatabaseQuery} object for the specific database dialect.
     * @throws InternalException if the query class cannot be instantiated or if a database access error occurs.
     */
    public DatabaseQuery newInstance() {
        try {
            // Get the database URL to determine the database type
            String url = this.getDataSource().getConnection().getMetaData().getURL();
            // Get the implementation class for the database type
            Class<? extends DatabaseQuery> query = DatabaseType.getDbType(url).getImplClass();
            // Get the constructor that accepts a DataSource
            Constructor<? extends DatabaseQuery> constructor = query.getConstructor(DataSource.class);
            // Instantiate the query class
            return constructor.newInstance(dataSource);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                | SQLException e) {
            throw new InternalException(e);
        }
    }

}
