/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.shade.screw.Config;
import org.miaixz.bus.shade.screw.engine.EngineFileType;
import org.miaixz.bus.shade.screw.metadata.*;

/**
 * Abstract base class for data processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProcess implements Process {

    /**
     * Configuration object.
     */
    protected Config config;
    /**
     * Cache for table information.
     */
    volatile Map<String, List<? extends Table>> tablesCaching = new ConcurrentHashMap<>();
    /**
     * Cache for column information.
     */
    volatile Map<String, List<Column>> columnsCaching = new ConcurrentHashMap<>();
    /**
     * Cache for primary key information.
     */
    volatile Map<String, List<PrimaryKey>> primaryKeysCaching = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    protected AbstractProcess() {

    }

    /**
     * Constructor with configuration.
     *
     * @param config {@link Config}
     */
    protected AbstractProcess(Config config) {
        Assert.notNull(config, "Configuration can not be empty!");
        this.config = config;
    }

    /**
     * Filters tables based on the configuration. If 'designated' rules are present, only specified tables are included.
     * Otherwise, 'ignore' rules are applied.
     *
     * @param tables The list of tables to be filtered.
     * @return The filtered list of tables.
     */
    protected List<TableSchema> filterTables(List<TableSchema> tables) {
        ProcessConfig produceConfig = config.getProduceConfig();
        if (!Objects.isNull(config) && !Objects.isNull(config.getProduceConfig())) {
            // If any 'designated' rules (by name, prefix, or suffix) are set, apply them.
            // Ignore rules are skipped if designation rules are active.
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableName())
                    || CollKit.isNotEmpty(produceConfig.getDesignatedTablePrefix())
                    || CollKit.isNotEmpty(produceConfig.getDesignatedTableSuffix())) {
                return handleDesignated(tables);
            }
            // Otherwise, apply ignore rules.
            else {
                return handleIgnore(tables);
            }
        }
        return tables;
    }

    /**
     * Handles table filtering based on 'designated' (inclusion) rules.
     *
     * @param tables The original list of tables.
     * @return A new list containing only the designated tables.
     */
    private List<TableSchema> handleDesignated(List<TableSchema> tables) {
        List<TableSchema> tableSchemas = new ArrayList<>();
        ProcessConfig produceConfig = this.config.getProduceConfig();
        if (!Objects.isNull(config) && !Objects.isNull(produceConfig)) {
            // By designated table name
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableName())) {
                List<String> list = produceConfig.getDesignatedTableName();
                for (String name : list) {
                    tableSchemas.addAll(
                            tables.stream().filter(j -> j.getTableName().equals(name)).collect(Collectors.toList()));
                }
            }
            // By designated table prefix
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTablePrefix())) {
                List<String> list = produceConfig.getDesignatedTablePrefix();
                for (String prefix : list) {
                    tableSchemas.addAll(
                            tables.stream().filter(j -> j.getTableName().startsWith(prefix))
                                    .collect(Collectors.toList()));
                }
            }
            // By designated table suffix
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableSuffix())) {
                List<String> list = produceConfig.getDesignatedTableSuffix();
                for (String suffix : list) {
                    tableSchemas.addAll(
                            tables.stream().filter(j -> j.getTableName().endsWith(suffix))
                                    .collect(Collectors.toList()));
                }
            }
            return tableSchemas;
        }
        return tableSchemas;
    }

    /**
     * Handles table filtering based on 'ignore' (exclusion) rules.
     *
     * @param tables The original list of tables.
     * @return A new list with ignored tables removed.
     */
    private List<TableSchema> handleIgnore(List<TableSchema> tables) {
        ProcessConfig produceConfig = this.config.getProduceConfig();
        if (!Objects.isNull(this.config) && !Objects.isNull(produceConfig)) {
            // Ignore by table name
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTableName())) {
                List<String> list = produceConfig.getIgnoreTableName();
                for (String name : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().equals(name)).collect(Collectors.toList());
                }
            }
            // Ignore by table prefix
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTablePrefix())) {
                List<String> list = produceConfig.getIgnoreTablePrefix();
                for (String prefix : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().startsWith(prefix))
                            .collect(Collectors.toList());
                }
            }
            // Ignore by table suffix
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTableSuffix())) {
                List<String> list = produceConfig.getIgnoreTableSuffix();
                for (String suffix : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().endsWith(suffix))
                            .collect(Collectors.toList());
                }
            }
            return tables;
        }
        return tables;
    }

    /**
     * Optimizes the data model, such as trimming string fields.
     *
     * @param dataModel {@link DataSchema}
     */
    public void optimizeData(DataSchema dataModel) {
        // Trim string fields in the main data model
        BeanKit.trimStringField(dataModel);
        // tables
        List<TableSchema> tables = dataModel.getTables();
        // columns
        tables.forEach(i -> {
            // Trim string fields in the table object
            BeanKit.trimStringField(i);
            List<ColumnSchema> columns = i.getColumns();
            // Trim string fields in column objects
            columns.forEach(BeanKit::trimStringField);
        });
        // Special handling if file type is Word
        if (config.getEngineConfig().getFileType().equals(EngineFileType.WORD)) {
            BeanKit.trimStringField(dataModel);
            tables.forEach(i -> {
                BeanKit.trimStringField(i);
                List<ColumnSchema> columns = i.getColumns();
                columns.forEach(BeanKit::trimStringField);
            });
        }
        // Special handling if file type is Markdown
        if (config.getEngineConfig().getFileType().equals(EngineFileType.MD)) {
            BeanKit.trimStringField(dataModel);
            tables.forEach(i -> {
                BeanKit.trimStringField(i);
                List<ColumnSchema> columns = i.getColumns();
                columns.forEach(BeanKit::trimStringField);
            });
        }
    }

}
