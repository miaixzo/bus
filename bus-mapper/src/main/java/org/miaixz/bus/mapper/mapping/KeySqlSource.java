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
package org.miaixz.bus.mapper.mapping;

import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;

/**
 * 支持定制化处理 {@link SqlSource} 的接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface KeySqlSource {

    /**
     * 默认 SPI 实现，加载并依次调用所有 KeySqlSource 实现类
     */
    KeySqlSource SPI = new KeySqlSource() {
        /**
         * 通过 SPI 加载的定制化 KeySqlSource 实现列表
         */
        private final List<KeySqlSource> customizes = NormalSpiLoader.loadList(false, KeySqlSource.class);

        /**
         * 依次调用所有定制化实现对 SqlSource 进行处理
         *
         * @param sqlSource 原始 SqlSource
         * @param entity    实体表信息
         * @param ms        MappedStatement
         * @param context   调用方法上下文
         * @return 定制化后的 SqlSource
         */
        @Override
        public SqlSource customize(SqlSource sqlSource, MapperTable entity, MappedStatement ms,
                ProviderContext context) {
            for (KeySqlSource customize : customizes) {
                sqlSource = customize.customize(sqlSource, entity, ms, context);
            }
            return sqlSource;
        }
    };

    /**
     * 定制化处理 SqlSource
     *
     * @param sqlSource 原始 SqlSource
     * @param entity    实体表信息
     * @param ms        MappedStatement
     * @param context   调用方法上下文
     * @return 定制化后的 SqlSource
     */
    SqlSource customize(SqlSource sqlSource, MapperTable entity, MappedStatement ms, ProviderContext context);

}