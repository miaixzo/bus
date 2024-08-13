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
 * H2 数据库查询
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class H2DataBaseQuery extends AbstractDatabaseQuery {

    /**
     * 构造函数
     *
     * @param dataSource {@link DataSource}
     */
    public H2DataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     */
    @Override
    public Database getDataBase() throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     */
    @Override
    public List<Table> getTables() {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * 获取列信息
     *
     * @param table {@link String} 表名
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    @Override
    public List<Column> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * 获取所有列信息
     *
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    @Override
    public List<? extends Column> getTableColumns() throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

    /**
     * 根据表名获取主键
     *
     * @param table {@link String}
     * @return {@link List}
     * @throws InternalException 异常
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

}
