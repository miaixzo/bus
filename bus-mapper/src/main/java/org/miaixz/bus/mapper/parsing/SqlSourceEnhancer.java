/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
