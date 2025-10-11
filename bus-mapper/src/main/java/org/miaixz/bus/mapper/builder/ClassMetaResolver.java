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
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.ORDER;

/**
 * Resolves the entity class type based on information such as the mapper type and method. The default implementation
 * can be replaced via SPI (Service Provider Interface).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ClassMetaResolver extends ORDER {

    /**
     * A cache to avoid repeated lookups during method execution. The key is a {@link MapperTypeMethod} and the value is
     * the corresponding entity class.
     */
    Map<MapperTypeMethod, Optional<Class<?>>> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * Finds the entity class corresponding to the current method.
     *
     * @param mapperType   The Mapper interface, which cannot be null.
     * @param mapperMethod The Mapper interface method, which can be null.
     * @return An {@link Optional} containing the entity class type.
     */
    static Optional<Class<?>> find(Class<?> mapperType, Method mapperMethod) {
        Objects.requireNonNull(mapperType);
        return ENTITY_CLASS_MAP.computeIfAbsent(new MapperTypeMethod(mapperType, mapperMethod), mapperTypeMethod -> {
            for (ClassMetaResolver instance : ClassFinderInstance.getInstances()) {
                Optional<Class<?>> optionalClass = instance.findClass(mapperType, mapperMethod);
                if (optionalClass.isPresent()) {
                    return optionalClass;
                }
            }
            return Optional.empty();
        });
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
         * A cached list of {@link ClassMetaResolver} instances.
         */
        private static volatile List<ClassMetaResolver> INSTANCES;

        /**
         * Gets extended implementations via SPI or uses the default implementation.
         *
         * @return A list of {@link ClassMetaResolver} instances.
         */
        public static List<ClassMetaResolver> getInstances() {
            if (INSTANCES == null) {
                synchronized (ClassMetaResolver.class) {
                    if (INSTANCES == null) {
                        INSTANCES = NormalSpiLoader.loadList(false, ClassMetaResolver.class);
                    }
                }
            }
            return INSTANCES;
        }
    }

}
