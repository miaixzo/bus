/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * 自动获取方言
 *
 * @param <K> 缓存key类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AutoDialect<K> {

    /**
     * 获取用于缓存 {@link #extractDialect } 方法返回值的 key，当返回 null 时不缓存，返回值时先判断是否已存在，不存在时调用 {@link #extractDialect } 再缓存
     *
     * @param ms         执行映射的语句
     * @param dataSource 数据源
     * @param properties 配置属性
     * @return the object
     */
    K extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties);

    /**
     * 提取 dialect
     *
     * @param dialectKey 数据方言对象
     * @param ms         执行映射的语句
     * @param dataSource 数据源
     * @param properties 配置属性
     * @return the object
     */
    AbstractPaging extractDialect(K dialectKey, MappedStatement ms, DataSource dataSource, Properties properties);

}
