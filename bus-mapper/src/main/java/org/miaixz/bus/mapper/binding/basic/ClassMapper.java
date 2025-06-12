/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.binding.basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.mapper.builder.GenericTypeResolver;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 实体类信息接口，实现此接口可便捷获取当前接口对应的实体类类型 {@link Class} 和实体表信息 {@link TableMeta}
 *
 * @param <T> 实体类泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ClassMapper<T> {

    /**
     * 获取当前接口对应的实体类类型
     *
     * @return 实体类类型
     */
    default Class<T> entityClass() {
        return (Class<T>) CachingEntityClass.getEntityClass(getClass());
    }

    /**
     * 获取当前接口对应的实体表信息
     *
     * @return 实体表信息
     */
    default TableMeta entityTable() {
        return MapperFactory.create(entityClass());
    }

    /**
     * 缓存实体类类型的工具类
     */
    class CachingEntityClass {
        /**
         * 存储接口与实体类类型的映射
         */
        static Map<Class<?>, Class<?>> entityClassMap = new ConcurrentHashMap<>();

        /**
         * 获取接口对应的实体类类型
         *
         * @param clazz 继承的子接口
         * @return 实体类类型
         */
        private static Class<?> getEntityClass(Class<?> clazz) {
            if (!entityClassMap.containsKey(clazz)) {
                entityClassMap.put(clazz, GenericTypeResolver.resolveTypeToClass(GenericTypeResolver
                        .resolveType(ClassMapper.class.getTypeParameters()[0], clazz, ClassMapper.class)));
            }
            return entityClassMap.get(clazz);
        }
    }

}