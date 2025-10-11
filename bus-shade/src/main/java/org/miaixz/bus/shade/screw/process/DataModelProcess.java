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
package org.miaixz.bus.shade.screw.process;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.Config;
import org.miaixz.bus.shade.screw.dialect.DatabaseQuery;
import org.miaixz.bus.shade.screw.dialect.DatabaseQueryFactory;
import org.miaixz.bus.shade.screw.metadata.*;

/**
 * Processes the database schema to create a data model for documentation generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DataModelProcess extends AbstractProcess {

    /**
     * Constructs a {@code DataModelProcess} with the given configuration.
     *
     * @param config The {@link Config} object for the process.
     */
    public DataModelProcess(Config config) {
        super(config);
    }

    /**
     * Processes the database metadata and builds the data model.
     *
     * @return The {@link DataSchema} containing the structured database information.
     */
    @Override
    public DataSchema process() {
        // Get the appropriate database query implementation.
        DatabaseQuery query = new DatabaseQueryFactory(config.getDataSource()).newInstance();
        DataSchema model = new DataSchema();
        // Set document metadata.
        model.setTitle(config.getTitle());
        model.setOrganization(config.getOrganization());
        model.setOrganizationUrl(config.getOrganizationUrl());
        model.setVersion(config.getVersion());
        model.setDescription(config.getDescription());

        long start = System.currentTimeMillis();
        // Get database information.
        Database database = query.getDataBase();
        Logger.debug("query the database time consuming:{}ms", (System.currentTimeMillis() - start));
        model.setDatabase(database.getDatabase());

        start = System.currentTimeMillis();
        // Get all tables.
        List<? extends Table> tables = query.getTables();
        Logger.debug("query the table time consuming:{}ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        // Get all columns.
        List<? extends Column> columns = query.getTableColumns();
        Logger.debug("query the column time consuming:{}ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        // Get all primary keys.
        List<? extends PrimaryKey> primaryKeys = query.getPrimaryKeys();
        Logger.debug("query the primary key time consuming:{}ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        List<TableSchema> tableSchemas = new ArrayList<>();
        tablesCaching.put(database.getDatabase(), tables);

        // Organize columns and primary keys by table name for efficient lookup.
        for (Table table : tables) {
            columnsCaching.put(
                    table.getTableName(),
                    columns.stream().filter(i -> i.getTableName().equals(table.getTableName()))
                            .collect(Collectors.toList()));
            primaryKeysCaching.put(
                    table.getTableName(),
                    primaryKeys.stream().filter(i -> i.getTableName().equals(table.getTableName()))
                            .collect(Collectors.toList()));
        }

        // Build the schema for each table.
        for (Table table : tables) {
            TableSchema tableSchema = new TableSchema();
            tableSchema.setTableName(table.getTableName());
            tableSchema.setRemarks(table.getRemarks());
            tableSchemas.add(tableSchema);

            // Process columns for the current table.
            List<ColumnSchema> columnSchemas = new ArrayList<>();
            List<String> key = primaryKeysCaching.get(table.getTableName()).stream().map(PrimaryKey::getColumnName)
                    .collect(Collectors.toList());
            for (Column column : columnsCaching.get(table.getTableName())) {
                packageColumn(columnSchemas, key, column);
            }
            tableSchema.setColumns(columnSchemas);
        }
        // Filter tables based on configuration.
        model.setTables(filterTables(tableSchemas));
        // Optimize data for presentation.
        optimizeData(model);
        Logger.debug("encapsulation processing data time consuming:{}ms", (System.currentTimeMillis() - start));
        return model;
    }

    /**
     * Packages a {@link Column} object into a {@link ColumnSchema} object.
     *
     * @param columnSchemas The list to add the new {@link ColumnSchema} to.
     * @param keyList       A list of primary key column names for the table.
     * @param column        The {@link Column} object to process.
     */
    private void packageColumn(List<ColumnSchema> columnSchemas, List<String> keyList, Column column) {
        ColumnSchema columnSchema = new ColumnSchema();
        columnSchema.setOrdinalPosition(column.getOrdinalPosition());
        columnSchema.setColumnName(column.getColumnName());
        columnSchema.setColumnType(column.getColumnType());
        columnSchema.setTypeName(column.getTypeName());
        columnSchema.setColumnLength(column.getColumnLength());
        columnSchema.setColumnSize(column.getColumnSize());
        columnSchema.setDecimalDigits(ObjectKit.defaultIfNull(column.getDecimalDigits(), Builder.ZERO_DECIMAL_DIGITS));
        columnSchema.setNullable(Builder.ZERO.equals(column.getNullable()) ? Builder.N : Builder.Y);
        columnSchema.setPrimaryKey(keyList.contains(column.getColumnName()) ? Builder.Y : Builder.N);
        columnSchema.setColumnDef(column.getColumnDef());
        columnSchema.setRemarks(column.getRemarks());
        columnSchemas.add(columnSchema);
    }

}
