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
package org.miaixz.bus.crypto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.crypto.nimble.*;
import org.miaixz.bus.logger.Logger;

/**
 * A registry for cryptographic service providers, mapping algorithm names to their respective {@link Provider}
 * implementations. This class manages the built-in strategy mappings between cryptographic algorithms and their
 * concrete implementations.
 *
 * @author Kimi Liu
 */
public final class Registry {

    /**
     * Constructs a new Registry instance.
     */
    public Registry() {
        // No initialization required.
    }

    /**
     * Cache for cryptographic algorithm providers, mapping algorithm names to {@link Provider} instances.
     */
    private static final Map<String, Provider> ALGORITHM_CACHE = new ConcurrentHashMap<>();

    static {
        register(new AESProvider());
        register(new DESProvider());
        register(new RC4Provider());
        register(new RSAProvider());
        register(new SM2Provider());
        register(new SM4Provider());
    }

    /**
     * Registers a cryptographic service provider under the stable algorithm name returned by {@link Provider#type()}.
     *
     * @param provider The {@link Provider} instance to register.
     * @throws IllegalArgumentException if the provider or its algorithm name is null or blank
     * @throws InternalException        if another provider is already registered under the same algorithm name
     */
    public static void register(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Crypto provider must not be null");
        }
        final String name = provider.type();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Crypto provider and type must not be null or blank");
        }
        Logger.debug(
                true,
                "Crypto",
                "Crypto provider registration started: name={}, provider={}",
                name,
                provider.getClass().getSimpleName());
        final Provider previous = ALGORITHM_CACHE.putIfAbsent(name, provider);
        if (previous != null) {
            Logger.warn(false, "Crypto", "Crypto provider registration rejected: name={}, reason=duplicateName", name);
            throw new InternalException("A crypto provider is already registered for algorithm: " + name);
        }
        Logger.debug(
                false,
                "Crypto",
                "Crypto provider registered: name={}, provider={}, registeredCount={}",
                name,
                provider.getClass().getSimpleName(),
                ALGORITHM_CACHE.size());
    }

    /**
     * Retrieves a cryptographic service provider by its name.
     *
     * @param name The name of the algorithm or component to retrieve.
     * @return The {@link Provider} instance associated with the given name.
     * @throws IllegalArgumentException if the name is null or blank, or no provider is found for it
     */
    public static Provider require(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Crypto algorithm name must not be null or blank");
        }
        Logger.debug(true, "Crypto", "Crypto provider lookup started: name={}", name);
        Provider provider = ALGORITHM_CACHE.get(name);
        if (ObjectKit.isEmpty(provider)) {
            Logger.warn(false, "Crypto", "Crypto provider lookup failed: name={}", name);
            throw new IllegalArgumentException("None provider be found!, type:" + name);
        }
        Logger.debug(
                false,
                "Crypto",
                "Crypto provider resolved: name={}, provider={}",
                name,
                provider.getClass().getSimpleName());
        return provider;
    }

    /**
     * Checks if a cryptographic service provider with the specified name is registered.
     *
     * @param name The name of the algorithm or component to check.
     * @return {@code true} if a provider with the given name is registered, {@code false} otherwise.
     */
    public static boolean contains(String name) {
        return name != null && !name.isBlank() && ALGORITHM_CACHE.containsKey(name);
    }

}
