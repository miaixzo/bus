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
 * @since Java 21+
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
        return MapperFactory.of(entityClass());
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
