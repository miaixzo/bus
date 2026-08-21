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
package org.miaixz.bus.extra.pinyin;

import java.util.ServiceConfigurationError;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Simple Pinyin engine factory that automatically creates the corresponding Pinyin engine object based on the Pinyin
 * library JARs introduced by the user. It uses the Simple Factory pattern.
 *
 * @author Kimi Liu
 */
public class PinyinFactory {

    /**
     * Constructs a new PinyinFactory instance.
     */
    public PinyinFactory() {
        // No initialization required.
    }

    /**
     * Retrieves a singleton instance of {@link PinyinProvider}. The first available Pinyin provider found via SPI will
     * be used.
     *
     * @return A singleton instance of {@link PinyinProvider}.
     */
    public static PinyinProvider get() {
        Logger.debug(true, "Extra", "Default pinyin provider lookup started");
        final PinyinProvider engine = Instances.get(PinyinProvider.class.getName(), PinyinFactory::of);
        Logger.debug(
                false,
                "Extra",
                "Default pinyin provider selected: provider={}",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * Creates a new {@link PinyinProvider} instance based on the available Pinyin engine JARs. This method returns a
     * new engine instance each time it is called.
     *
     * @return A new {@link PinyinProvider} instance.
     * @throws InternalException if no Pinyin library is found or available.
     */
    private static PinyinProvider of() {
        Logger.debug(true, "Extra", "SPI pinyin provider lookup started");
        final PinyinProvider engine = NormalSpiLoader.loadFirstAvailable(PinyinProvider.class);
        if (null != engine) {
            Logger.debug(
                    false,
                    "Extra",
                    "SPI pinyin provider selected: provider={}",
                    engine.getClass().getSimpleName());
            return engine;
        }

        Logger.warn(false, "Extra", "SPI pinyin provider lookup failed: providerPresent={}", false);
        throw new InternalException("No pinyin jar found !Please add one of it to your project !");
    }

    /**
     * Creates a custom Pinyin engine by name.
     *
     * @param name The name of the engine (case-insensitive), e.g., `Bopomofo4j`, `Houbb`, `JPinyin`, `Pinyin4j`,
     *             `TinyPinyin`.
     * @return The {@link PinyinProvider} instance corresponding to the given name.
     * @throws IllegalArgumentException if the provider name is null or blank
     * @throws InternalException        if no provider with the specified name is available via SPI, a provider declares
     *                                  an invalid type, or duplicate types are discovered
     */
    public static PinyinProvider of(String name) throws InternalException {
        Logger.debug(true, "Extra", "Named pinyin provider lookup started: requestedProvider={}", name);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pinyin provider name must not be null or blank");
        }
        final String candidate = name.trim();
        final String requested = StringKit.endWithIgnoreCase(candidate, "Provider")
                ? candidate.substring(0, candidate.length() - "Provider".length())
                : candidate;
        final ServiceLoader<PinyinProvider> list = NormalSpiLoader.loadList(PinyinProvider.class);
        Logger.debug(
                true,
                "Extra",
                "Named pinyin provider candidates loaded: normalizedProvider={}, candidateCount={}",
                requested,
                list.getServiceNames().size());
        PinyinProvider match = null;
        for (final String serviceName : list.getServiceNames()) {
            final PinyinProvider provider;
            try {
                provider = list.getService(serviceName);
            } catch (RuntimeException | ServiceConfigurationError | LinkageError unavailable) {
                continue;
            }
            if (provider != null) {
                final String type = provider.type();
                if (type == null || type.isBlank()) {
                    throw new InternalException(
                            "Pinyin provider type must not be null or blank: " + provider.getClass().getName());
                }
                if (!type.equalsIgnoreCase(requested)) {
                    continue;
                }
                if (match != null) {
                    throw new InternalException("Duplicate Pinyin provider type '" + requested + "': "
                            + match.getClass().getName() + " and " + provider.getClass().getName());
                }
                match = provider;
                Logger.debug(
                        false,
                        "Extra",
                        "Named pinyin provider selected: normalizedProvider={}, serviceName={}, provider={}",
                        requested,
                        serviceName,
                        provider.getClass().getSimpleName());
            }
        }
        if (match != null) {
            return match;
        }
        Logger.warn(
                false,
                "Extra",
                "Named pinyin provider lookup failed: normalizedProvider={}, candidateCount={}",
                requested,
                list.getServiceNames().size());
        throw new InternalException("No such provider named: " + requested);
    }

}
