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
package org.miaixz.bus.mapper.binding.basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.mapper.builder.GenericTypeResolver;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * An interface that provides information about an entity class. By implementing this interface, you can easily access
 * the entity's {@link Class} type and its corresponding table metadata ({@link TableMeta}).
 *
 * @param <T> The generic type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ClassMapper<T> {

    /**
     * Gets the entity class type corresponding to the current interface.
     *
     * @return The entity class type.
     */
    default Class<T> entityClass() {
        return (Class<T>) CachingEntityClass.getEntityClass(getClass());
    }

    /**
     * Gets the entity table metadata corresponding to the current interface.
     *
     * @return The entity table metadata.
     */
    default TableMeta entityTable() {
        return MapperFactory.create(entityClass());
    }

    /**
     * A utility class for caching entity class types.
     */
    class CachingEntityClass {

        /**
         * A map to store the mapping between interface classes and their corresponding entity class types.
         */
        static Map<Class<?>, Class<?>> entityClassMap = new ConcurrentHashMap<>();

        /**
         * Gets the entity class type for a given sub-interface.
         *
         * @param clazz The sub-interface that inherits from this mapper.
         * @return The entity class type.
         */
        private static Class<?> getEntityClass(Class<?> clazz) {
            if (!entityClassMap.containsKey(clazz)) {
                entityClassMap.put(
                        clazz,
                        GenericTypeResolver.resolveTypeToClass(
                                GenericTypeResolver.resolveType(
                                        ClassMapper.class.getTypeParameters()[0],
                                        clazz,
                                        ClassMapper.class)));
            }
            return entityClassMap.get(clazz);
        }
    }

}
