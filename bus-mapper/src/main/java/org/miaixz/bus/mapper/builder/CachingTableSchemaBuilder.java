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
