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
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.Order;

/**
 * SPI interface for processing the final SQL script.
 *
 * @author Kimi Liu
 * @since Java 17+
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
