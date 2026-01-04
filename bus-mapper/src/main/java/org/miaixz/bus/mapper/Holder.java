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
package org.miaixz.bus.mapper;

import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.builder.ColumnSchemaBuilder;
import org.miaixz.bus.mapper.builder.ColumnSchemaChain;
import org.miaixz.bus.mapper.builder.TableSchemaBuilder;
import org.miaixz.bus.mapper.builder.TableSchemaChain;

/**
 * Holds factory instances and manages processing chains for table and column schemas.
 * <p>
 * This class is responsible for maintaining the context for data source switching and the builder chains for table and
 * column structures. It uses the SPI (Service Provider Interface) mechanism to load table and column builders and
 * provides a thread-safe implementation for data source switching.
 * </p>
 *
 * @param <T> the type of object held
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder<T> implements org.miaixz.bus.core.Holder<T> {

    /**
     * The processing chain for table schemas, loaded via SPI.
     * <p>
     * This chain of processors is responsible for building table models based on database table structure information.
     * It loads all classes that implement the {@link TableSchemaBuilder} interface via SPI and organizes them into a
     * processing chain.
     * </p>
     */
    public static final TableSchemaBuilder.Chain TABLE_SCHEMA_CHAIN = new TableSchemaChain(
            NormalSpiLoader.loadList(false, TableSchemaBuilder.class));

    /**
     * The processing chain for column schemas, loaded via SPI.
     * <p>
     * This chain of processors is responsible for building column models based on database column structure
     * information. It loads all classes that implement the {@link ColumnSchemaBuilder} interface via SPI and organizes
     * them into a processing chain.
     * </p>
     */
    public static final ColumnSchemaBuilder.Chain COLUMN_SCHEMA_CHAIN = new ColumnSchemaChain(
            NormalSpiLoader.loadList(false, ColumnSchemaBuilder.class));

    /**
     * A {@link ThreadLocal} variable to hold the data source key for the current thread.
     * <p>
     * This ensures thread safety for data source switching in a multi-threaded environment. Each thread can
     * independently set and get its own data source key without interfering with others.
     * </p>
     */
    private static final ThreadLocal<String> DATA_SOURCE_KEY = new ThreadLocal<>();

    /**
     * The name of the default data source.
     * <p>
     * When no data source is explicitly specified, the system will use this default data source. This value is
     * typically set at system startup via configuration or code and remains constant throughout the application
     * lifecycle.
     * </p>
     */
    private static String DEFAULT_KEY = "default";

    /**
     * Gets the data source key for the current thread, including the default value.
     * <p>
     * If no data source key is set for the current thread, it returns the default key. This method is typically called
     * during data source routing to determine which data source to use.
     * </p>
     *
     * @return The data source key for the current thread, or the default key if not set.
     */
    public static String getKey() {
        String dataSource = DATA_SOURCE_KEY.get();
        return dataSource != null ? dataSource : getDefault();
    }

    /**
     * Sets the data source key for the current thread.
     * <p>
     * This method should be called before performing a database operation to specify which data source to use. The set
     * key will affect all subsequent database operations in the current thread until it is cleared or changed.
     * </p>
     *
     * @param key The key identifying the specific data source.
     */
    public static void setKey(String key) {
        DATA_SOURCE_KEY.set(key);
    }

    /**
     * Clears the data source key setting for the current thread.
     * <p>
     * This method should be called after a database operation is complete to clear the data source key setting,
     * preventing it from affecting subsequent operations. This is especially important in thread-pooled environments to
     * avoid using the wrong data source when threads are reused.
     * </p>
     */
    public static void remove() {
        DATA_SOURCE_KEY.remove();
    }

    /**
     * Sets the name of the default data source.
     * <p>
     * This method is called during system initialization to set the default data source. The default data source is
     * used automatically when no specific data source is specified.
     * </p>
     *
     * @param name The name of the default data source, which cannot be null or empty.
     */
    public static void setDefault(String name) {
        DEFAULT_KEY = name;
    }

    /**
     * Gets the name of the default data source.
     * <p>
     * Retrieves the configured name of the default data source for the system. This default data source is used when no
     * data source is explicitly specified.
     * </p>
     *
     * @return The name of the default data source, which may be null if not set.
     */
    public static String getDefault() {
        return DEFAULT_KEY;
    }

}
