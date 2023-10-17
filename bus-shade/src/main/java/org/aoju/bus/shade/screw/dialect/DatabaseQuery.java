/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.shade.screw.dialect;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.shade.screw.metadata.Column;
import org.aoju.bus.shade.screw.metadata.Database;
import org.aoju.bus.shade.screw.metadata.PrimaryKey;
import org.aoju.bus.shade.screw.metadata.Table;

import java.util.List;

/**
 * 通用查询接口
 * 查询数据库信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DatabaseQuery {

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     * @throws InternalException 异常
     */
    Database getDataBase() throws InternalException;

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     * @throws InternalException 异常
     */
    List<? extends Table> getTables() throws InternalException;

    /**
     * 获取列信息
     *
     * @param table {@link String} 表名
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    List<? extends Column> getTableColumns(String table) throws InternalException;

    /**
     * 获取所有列信息
     *
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    List<? extends Column> getTableColumns() throws InternalException;

    /**
     * 根据表名获取主键
     *
     * @param table {@link String}
     * @return {@link List}
     * @throws InternalException 异常
     */
    List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException;

    /**
     * 获取主键
     *
     * @return {@link List}
     * @throws InternalException 异常
     */
    List<? extends PrimaryKey> getPrimaryKeys() throws InternalException;

}
