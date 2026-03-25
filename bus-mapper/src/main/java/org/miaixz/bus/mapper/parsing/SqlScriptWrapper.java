/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.parsing;

import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.Order;

/**
 * SPI interface for processing the final SQL script.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SqlScriptWrapper extends Order {

    /**
     * Wraps an SQL script by applying all {@link SqlScriptWrapper} implementations.
     *
     * @param context   The current interface and method information.
     * @param entity    The entity class information.
     * @param sqlScript The SQL script to be wrapped.
     * @return The wrapped SQL script.
     */
    static SqlScript wrapSqlScript(ProviderContext context, TableMeta entity, SqlScript sqlScript) {
        for (SqlScriptWrapper wrapper : Holder.sqlScriptWrappers) {
            sqlScript = wrapper.wrap(context, entity, sqlScript);
        }
        return sqlScript;
    }

    /**
     * Processes and modifies the SQL script.
     *
     * @param context   The current interface and method information.
     * @param entity    The entity class information.
     * @param sqlScript The SQL script.
     * @return The processed SQL script.
     */
    SqlScript wrap(ProviderContext context, TableMeta entity, SqlScript sqlScript);

    /**
     * A holder class that manages {@link SqlScriptWrapper} SPI implementations.
     */
    class Holder {

        /**
         * A list of {@link SqlScriptWrapper} implementations loaded via SPI.
         */
        static final List<SqlScriptWrapper> sqlScriptWrappers = NormalSpiLoader.loadList(false, SqlScriptWrapper.class);
    }

}
