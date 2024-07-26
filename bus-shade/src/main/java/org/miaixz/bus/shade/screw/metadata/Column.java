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
package org.miaixz.bus.shade.screw.metadata;

/**
 * 表列接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Column {

    /**
     * 获取表名
     *
     * @return {@link String}
     */
    String getTableName();

    /**
     * 表中的列的索引（从 1 开始）
     *
     * @return {@link String}
     */
    String getOrdinalPosition();

    /**
     * 名称
     *
     * @return {@link String}
     */
    String getColumnName();

    /**
     * 列的数据类型名称
     *
     * @return {@link String}
     */
    String getTypeName();

    /**
     * 列表示给定列的指定列大小。 对于数值数据，这是最大精度。 对于字符数据，这是字符长度。 对于日期时间数据类型，这是 String 表示形式的字符长度（假定允许的最大小数秒组件的精度）。 对于二进制数据，这是字节长度。 对于
     * ROWID 数据类型，这是字节长度。对于列大小不适用的数据类型，则返回 Null。
     *
     * @return {@link String}
     */
    String getColumnSize();

    /**
     * 小数位
     *
     * @return {@link String}
     */
    String getDecimalDigits();

    /**
     * 可为空
     *
     * @return {@link String}
     */
    String getNullable();

    /**
     * 是否主键
     *
     * @return {@link Boolean}
     */
    String getPrimaryKey();

    /**
     * 默认值
     *
     * @return {@link String}
     */
    String getColumnDef();

    /**
     * 说明
     *
     * @return {@link String}
     */
    String getRemarks();

    /**
     * 获取列类型
     *
     * @return {@link String}
     */
    String getColumnType();

    /**
     * 获取列长度
     *
     * @return {@link String}
     */
    String getColumnLength();

}
