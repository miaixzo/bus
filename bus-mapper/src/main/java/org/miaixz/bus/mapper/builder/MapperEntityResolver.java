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

import java.lang.reflect.Type;

import org.miaixz.bus.mapper.binding.basic.ClassMapper;

/**
 * Resolves mapper entity classes from mapper interfaces.
 * <p>
 * This resolver is intentionally free of Spring {@code BeanDefinition} or classpath scanning concerns. Callers pass in
 * the mapper interface class they have already discovered, and the resolver only inspects the generic
 * {@link ClassMapper} contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MapperEntityResolver {

    /**
     * Keeps mapper entity resolution on the static API.
     */
    private MapperEntityResolver() {
        // No initialization required.
    }

    /**
     * Resolves the entity class declared by a {@link ClassMapper} interface.
     * <p>
     * Returns {@code null} when the supplied type is not a mapper, when the generic argument cannot be resolved, or
     * when the resolved argument is still {@link Object}.
     *
     * @param mapperInterface mapper interface class
     * @return resolved entity class, or {@code null} when it cannot be resolved
     */
    public static Class<?> resolve(Class<?> mapperInterface) {
        if (mapperInterface == null || !ClassMapper.class.isAssignableFrom(mapperInterface)) {
            return null;
        }
        Type entityType = GenericTypeResolver
                .resolveType(ClassMapper.class.getTypeParameters()[0], mapperInterface, ClassMapper.class);
        Class<?> entityClass = GenericTypeResolver.resolveTypeToClass(entityType);
        return Object.class.equals(entityClass) ? null : entityClass;
    }

}
