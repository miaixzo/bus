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
package org.miaixz.bus.mapper.builder;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A table factory that supports caching entity class information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CachingTableSchemaBuilder implements TableSchemaBuilder {

    /**
     * Caches entity class information, with the entity class as the key and the corresponding {@link TableMeta} as the
     * value.
     */
    private final Map<Class<?>, TableMeta> ENTITY_CLASS_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Creates entity table information, with caching support to avoid redundant creation.
     *
     * @param entityClass The entity class.
     * @param chain       The table factory chain.
     * @return The entity table information, or null on failure.
     */
    @Override
    public TableMeta createTable(Class<?> entityClass, Chain chain) {
        TableMeta tableMeta = ENTITY_CLASS_MAP.get(entityClass);
        if (tableMeta == null) {
            // Double-checked locking
            synchronized (ENTITY_CLASS_MAP) {
                tableMeta = ENTITY_CLASS_MAP.get(entityClass);
                if (tableMeta == null) {
                    tableMeta = chain.createTable(entityClass);
                    if (tableMeta != null) {
                        ENTITY_CLASS_MAP.put(entityClass, tableMeta);
                    }
                }
            }
        }
        return tableMeta;
    }

    /**
     * Gets the priority order of the factory.
     *
     * @return The priority value. {@link Integer#MAX_VALUE} indicates the highest priority.
     */
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

}
