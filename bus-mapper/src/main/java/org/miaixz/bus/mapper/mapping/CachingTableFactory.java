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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持缓存实体类信息的表工厂
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CachingTableFactory implements TableFactory {

    /**
     * 缓存实体类信息，键为实体类，值为对应的 MapperTable
     */
    private final Map<Class<?>, MapperTable> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 创建实体表信息，支持缓存以避免重复创建
     *
     * @param entityClass 实体类
     * @param chain       表工厂链
     * @return 实体表信息，失败时返回 null
     */
    @Override
    public MapperTable createEntityTable(Class<?> entityClass, Chain chain) {
        if (ENTITY_CLASS_MAP.get(entityClass) == null) {
            synchronized (entityClass) {
                if (ENTITY_CLASS_MAP.get(entityClass) == null) {
                    MapperTable entityTable = chain.createEntityTable(entityClass);
                    if (entityTable != null) {
                        ENTITY_CLASS_MAP.put(entityClass, entityTable);
                    } else {
                        return null;
                    }
                }
            }
        }
        return ENTITY_CLASS_MAP.get(entityClass);
    }

    /**
     * 获取工厂的优先级顺序
     *
     * @return 优先级值，Integer.MAX_VALUE 表示最高优先级
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}