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

import java.lang.reflect.Method;
import java.util.*;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.Order;

/**
 * Resolves the entity class type based on information such as the mapper type and method. The default implementation
 * can be replaced via SPI (Service Provider Interface).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ClassMetaResolver extends Order {

    /**
     * A cache to avoid repeated lookups during method execution. The key is a {@link MapperTypeMethod} and the value is
     * the corresponding entity class.
     */
    Map<MapperTypeMethod, Optional<Class<?>>> ENTITY_CLASS_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Finds the entity class corresponding to the current method.
     *
     * @param mapperType   The Mapper interface, which cannot be null.
     * @param mapperMethod The Mapper interface method, which can be null.
     * @return An {@link Optional} containing the entity class type.
     */
    static Optional<Class<?>> find(Class<?> mapperType, Method mapperMethod) {
        Objects.requireNonNull(mapperType);
        MapperTypeMethod key = new MapperTypeMethod(mapperType, mapperMethod);
        Optional<Class<?>> optionalClass = ENTITY_CLASS_MAP.get(key);
        if (optionalClass == null) {
            synchronized (ENTITY_CLASS_MAP) {
                optionalClass = ENTITY_CLASS_MAP.get(key);
                if (optionalClass == null) {
                    Optional<Class<?>> foundClass = Optional.empty();
                    for (ClassMetaResolver instance : ClassFinderInstance.getInstances()) {
                        Optional<Class<?>> result = instance.findClass(mapperType, mapperMethod);
                        if (result.isPresent()) {
                            foundClass = result;
                            break;
                        }
                    }
                    optionalClass = foundClass;
                    ENTITY_CLASS_MAP.put(key, optionalClass);
                }
            }
        }
        return optionalClass;
    }

    /**
     * Finds the entity class corresponding to the current method.
     *
     * @param mapperType   The Mapper interface, which cannot be null.
     * @param mapperMethod The Mapper interface method, which can be null.
     * @return An {@link Optional} containing the entity class type.
     */
    Optional<Class<?>> findClass(Class<?> mapperType, Method mapperMethod);

    /**
     * Determines whether the specified type is a defined entity class type.
     *
     * @param clazz The type to check.
     * @return {@code true} if it is an entity class type, {@code false} otherwise.
     */
    boolean isClass(Class<?> clazz);

    /**
     * Represents a combination of a Mapper interface and a method, used as a cache key.
     */
    class MapperTypeMethod {

        /**
         * The Mapper interface class.
         */
        private final Class<?> mapperType;

        /**
         * The Mapper interface method.
         */
        private final Method mapperMethod;

        /**
         * Constructs a new MapperTypeMethod.
         *
         * @param mapperType   The Mapper interface class.
         * @param mapperMethod The Mapper interface method.
         */
        public MapperTypeMethod(Class<?> mapperType, Method mapperMethod) {
            this.mapperType = mapperType;
            this.mapperMethod = mapperMethod;
        }

        /**
         * Compares this MapperTypeMethod with another object for equality.
         *
         * @param o The object to compare with.
         * @return {@code true} if the objects are equal, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MapperTypeMethod that = (MapperTypeMethod) o;
            return Objects.equals(mapperType, that.mapperType) && Objects.equals(mapperMethod, that.mapperMethod);
        }

        /**
         * Computes the hash code for this object.
         *
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            return Objects.hash(mapperType, mapperMethod);
        }

        /**
         * Returns a string representation of the object.
         *
         * @return A string in the format "mapperTypeSimpleName.methodName" or just "mapperTypeSimpleName.".
         */
        @Override
        public String toString() {
            return (mapperType != null ? mapperType.getSimpleName() + "." : "")
                    + (mapperMethod != null ? mapperMethod.getName() : "");
        }
    }

    /**
     * Manages instances of entity class finders.
     */
    class ClassFinderInstance {

        /**
         * Gets extended implementations via SPI or uses the default implementation.
         *
         * @return A list of {@link ClassMetaResolver} instances.
         */
        public static List<ClassMetaResolver> getInstances() {
            return ClassMetaResolverHolder.INSTANCES;
        }

        /**
         * Initialization-on-demand holder idiom.
         */
        private static class ClassMetaResolverHolder {

            private static final List<ClassMetaResolver> INSTANCES = NormalSpiLoader
                    .loadList(false, ClassMetaResolver.class);
        }
    }

}
