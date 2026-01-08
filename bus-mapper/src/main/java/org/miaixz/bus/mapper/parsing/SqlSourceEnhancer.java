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
package org.miaixz.bus.mapper.parsing;

import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;

/**
 * An interface for customizing the processing of {@link SqlSource}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SqlSourceEnhancer {

    /**
     * The default SPI implementation, which loads and sequentially calls all {@link SqlSourceEnhancer} implementations.
     */
    SqlSourceEnhancer SPI = new SqlSourceEnhancer() {

        /**
         * A list of customized {@link SqlSourceEnhancer} implementations loaded via SPI.
         */
        private final List<SqlSourceEnhancer> customizes = NormalSpiLoader.loadList(false, SqlSourceEnhancer.class);

        /**
         * Sequentially calls all customized implementations to process the {@link SqlSource}.
         *
         * @param sqlSource The original {@link SqlSource}.
         * @param entity    The entity table information.
         * @param ms        The {@link MappedStatement}.
         * @param context   The invocation method context.
         * @return The customized {@link SqlSource}.
         */
        @Override
        public SqlSource customize(SqlSource sqlSource, TableMeta entity, MappedStatement ms, ProviderContext context) {
            for (SqlSourceEnhancer customize : customizes) {
                sqlSource = customize.customize(sqlSource, entity, ms, context);
            }
            return sqlSource;
        }
    };

    /**
     * Customizes the processing of {@link SqlSource}.
     *
     * @param sqlSource The original {@link SqlSource}.
     * @param entity    The entity table information.
     * @param ms        The {@link MappedStatement}.
     * @param context   The invocation method context.
     * @return The customized {@link SqlSource}.
     */
    SqlSource customize(SqlSource sqlSource, TableMeta entity, MappedStatement ms, ProviderContext context);

}
