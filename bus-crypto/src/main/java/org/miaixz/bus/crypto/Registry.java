/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.crypto.metric.*;

/**
 * A registry for cryptographic service providers, mapping algorithm names to their respective {@link Provider}
 * implementations. This class manages the built-in strategy mappings between cryptographic algorithms and their
 * concrete implementations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Registry {

    /**
     * Cache for cryptographic algorithm providers, mapping algorithm names to {@link Provider} instances.
     */
    private static Map<String, Provider> ALGORITHM_CACHE = new ConcurrentHashMap<>();

    static {
        register(Algorithm.AES.getValue(), new AESProvider());
        register(Algorithm.DES.getValue(), new DESProvider());
        register(Algorithm.RC4.getValue(), new RC4Provider());
        register(Algorithm.RSA.getValue(), new RSAProvider());
        register(Algorithm.SM2.getValue(), new SM2Provider());
        register(Algorithm.SM4.getValue(), new SM4Provider());
    }

    /**
     * Registers a cryptographic service provider with a given name. If a provider with the same name or the same class
     * simple name is already registered, an {@link InternalException} is thrown.
     *
     * @param name   The name of the algorithm or component to register.
     * @param object The {@link Provider} instance to register.
     * @throws InternalException if a component with the same name or class simple name is already registered.
     */
    public static void register(String name, Provider object) {
        if (ALGORITHM_CACHE.containsKey(name)) {
            throw new InternalException("Repeat registration of components with the same name：" + name);
        }
        Class<?> clazz = object.getClass();
        if (ALGORITHM_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("Repeat registration of components with the same name：" + clazz);
        }
        ALGORITHM_CACHE.putIfAbsent(name, object);
    }

    /**
     * Retrieves a cryptographic service provider by its name.
     *
     * @param name The name of the algorithm or component to retrieve.
     * @return The {@link Provider} instance associated with the given name.
     * @throws IllegalArgumentException if no provider is found for the specified name.
     */
    public static Provider require(String name) {
        Provider object = ALGORITHM_CACHE.get(name);
        if (ObjectKit.isEmpty(object)) {
            throw new IllegalArgumentException("None provider be found!, type:" + name);
        }
        return object;
    }

    /**
     * Checks if a cryptographic service provider with the specified name is registered.
     *
     * @param name The name of the algorithm or component to check.
     * @return {@code true} if a provider with the given name is registered, {@code false} otherwise.
     */
    public boolean contains(String name) {
        return ALGORITHM_CACHE.containsKey(name);
    }

}
