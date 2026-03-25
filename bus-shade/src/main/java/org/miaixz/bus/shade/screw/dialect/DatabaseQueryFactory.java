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

import java.io.Serial;
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
 * @since Java 21+
 */
@Getter
@Setter
public class DatabaseQueryFactory implements Serializable {

    @Serial
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
