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
package org.miaixz.bus.mapper.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides an interface for naming styles, supporting custom naming rules extensible via SPI.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NamingProvider extends Provider {

    /**
     * Stores the mapping between naming styles and their implementation classes.
     */
    Map<String, NamingProvider> styleMap = new HashMap() {

        {
            List<NamingProvider> instances = NormalSpiLoader.loadList(false, NamingProvider.class);
            for (NamingProvider instance : instances) {
                put(instance.type(), instance);
            }
        }
    };

    /**
     * Gets the default naming style handler instance.
     *
     * @return The default naming style implementation.
     */
    static NamingProvider getDefaultStyle() {
        return type(null);
    }

    /**
     * Gets a naming style handler instance by style name.
     *
     * @param style The name of the style. If null or empty, the global configuration or default style will be used.
     * @return The naming style implementation.
     * @throws IllegalArgumentException if the style name is invalid.
     */
    static NamingProvider type(String style) {
        if (style == null || style.isEmpty()) {
            style = Context.INSTANCE.getProperty(Args.NAMING_KEY, Args.CAMEL_UNDERLINE_LOWER_CASE);
        }
        if (style == null || style.isEmpty()) {
            style = Args.CAMEL_UNDERLINE_LOWER_CASE;
        }

        if (styleMap.containsKey(style)) {
            return styleMap.get(style);
        } else {
            throw new IllegalArgumentException("illegal style：" + style);
        }
    }

    /**
     * Converts an entity class to a table name.
     *
     * @param entityClass The entity class.
     * @return The corresponding table name.
     */
    String tableName(Class<?> entityClass);

    /**
     * Converts a field to a column name.
     *
     * @param entityTable The entity table information.
     * @param field       The entity field information.
     * @return The corresponding column name.
     */
    String columnName(TableMeta entityTable, FieldMeta field);

}
