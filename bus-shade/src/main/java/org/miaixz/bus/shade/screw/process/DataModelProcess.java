/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.Config;
import org.miaixz.bus.shade.screw.dialect.DatabaseQuery;
import org.miaixz.bus.shade.screw.dialect.DatabaseQueryFactory;
import org.miaixz.bus.shade.screw.metadata.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据模型处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DataModelProcess extends AbstractProcess {

    /**
     * 构造方法
     *
     * @param config {@link Config}
     */
    public DataModelProcess(Config config) {
        super(config);
    }

    /**
     * 处理
     *
     * @return {@link DataSchema}
     */
    @Override
    public DataSchema process() {
        // 获取query对象
        DatabaseQuery query = new DatabaseQueryFactory(config.getDataSource()).newInstance();
        DataSchema model = new DataSchema();
        // title
        model.setTitle(config.getTitle());
        // org
        model.setOrganization(config.getOrganization());
        // org url
        model.setOrganizationUrl(config.getOrganizationUrl());
        // version
        model.setVersion(config.getVersion());
        // description
        model.setDescription(config.getDescription());

        long start = System.currentTimeMillis();
        // 获取数据库
        Database database = query.getDataBase();
        Logger.debug("query the database time consuming:{}ms", (System.currentTimeMillis() - start));
        model.setDatabase(database.getDatabase());
        start = System.currentTimeMillis();
        // 获取全部表
        List<? extends Table> tables = query.getTables();
        Logger.debug("query the table time consuming:{}ms", (System.currentTimeMillis() - start));
        // 获取全部列
        start = System.currentTimeMillis();
        List<? extends Column> columns = query.getTableColumns();
        Logger.debug("query the column time consuming:{}ms", (System.currentTimeMillis() - start));
        // 获取主键
        start = System.currentTimeMillis();
        List<? extends PrimaryKey> primaryKeys = query.getPrimaryKeys();
        Logger.debug("query the primary key time consuming:{}ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        List<TableSchema> tableSchemas = new ArrayList<>();
        tablesCaching.put(database.getDatabase(), tables);
        for (Table table : tables) {
            // 处理列，表名为key，列名为值
            columnsCaching.put(table.getTableName(), columns.stream()
                    .filter(i -> i.getTableName().equals(table.getTableName())).collect(Collectors.toList()));
            // 处理主键，表名为key，主键为值
            primaryKeysCaching.put(table.getTableName(), primaryKeys.stream()
                    .filter(i -> i.getTableName().equals(table.getTableName())).collect(Collectors.toList()));
        }
        for (Table table : tables) {
            TableSchema tableSchema = new TableSchema();
            // 表名称
            tableSchema.setTableName(table.getTableName());
            // 说明
            tableSchema.setRemarks(table.getRemarks());
            // 添加表
            tableSchemas.add(tableSchema);
            // 处理列
            List<ColumnSchema> columnSchemas = new ArrayList<>();
            // 获取主键
            List<String> key = primaryKeysCaching.get(table.getTableName()).stream().map(PrimaryKey::getColumnName)
                    .collect(Collectors.toList());
            for (Column column : columnsCaching.get(table.getTableName())) {
                packageColumn(columnSchemas, key, column);
            }
            // 放入列
            tableSchema.setColumns(columnSchemas);
        }
        // 设置表
        model.setTables(filterTables(tableSchemas));
        // 优化数据
        optimizeData(model);
        Logger.debug("encapsulation processing data time consuming:{}ms", (System.currentTimeMillis() - start));
        return model;
    }

    /**
     * packageColumn
     *
     * @param columnSchemas {@link List}
     * @param keyList       {@link List}
     * @param column        {@link Column}
     */
    private void packageColumn(List<ColumnSchema> columnSchemas, List<String> keyList, Column column) {
        ColumnSchema columnSchema = new ColumnSchema();
        // 表中的列的索引（从 1 开始）
        columnSchema.setOrdinalPosition(column.getOrdinalPosition());
        // 列名称
        columnSchema.setColumnName(column.getColumnName());
        // 类型
        columnSchema.setColumnType(column.getColumnType());
        // 字段名称
        columnSchema.setTypeName(column.getTypeName());
        // 长度
        columnSchema.setColumnLength(column.getColumnLength());
        columnSchema.setColumnLength(column.getColumnLength());
        // 大小
        columnSchema.setColumnSize(column.getColumnSize());
        // 小数位
        columnSchema.setDecimalDigits(ObjectKit.defaultIfNull(column.getDecimalDigits(), Builder.ZERO_DECIMAL_DIGITS));
        // 可为空
        columnSchema.setNullable(Builder.ZERO.equals(column.getNullable()) ? Builder.N : Builder.Y);
        // 是否主键
        columnSchema.setPrimaryKey(keyList.contains(column.getColumnName()) ? Builder.Y : Builder.N);
        // 默认值
        columnSchema.setColumnDef(column.getColumnDef());
        // 说明
        columnSchema.setRemarks(column.getRemarks());
        // 放入集合
        columnSchemas.add(columnSchema);
    }

}
