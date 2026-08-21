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
package org.miaixz.bus.extra.nlp;

import java.util.ServiceConfigurationError;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Factory class for creating and managing Natural Language Processing (NLP) word segmentation engines. This factory
 * automatically detects and instantiates appropriate {@link NLPProvider} implementations based on available JARs or
 * explicit engine names, providing a unified access point for NLP services.
 *
 * @author Kimi Liu
 */
public class NLPFactory {

    /**
     * Constructs a new NLPFactory instance.
     */
    public NLPFactory() {
        // No initialization required.
    }

    /**
     * Automatically creates and retrieves a singleton instance of the corresponding word segmentation engine object.
     * The engine is determined based on the NLP engine JARs introduced by the user via SPI mechanism. The chosen
     * engine's simple name (without "Engine" suffix) is logged for informational purposes.
     *
     * @return A singleton instance of {@link NLPProvider}.
     */
    public static NLPProvider getEngine() {
        Logger.debug(true, "Extra", "Default segmentation engine lookup started");
        final NLPProvider engine = Instances.get(NLPProvider.class.getName(), NLPFactory::createEngine);
        Logger.debug(
                false,
                "Extra",
                "Default segmentation engine selected: engine={}",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * Automatically creates a new instance of the corresponding word segmentation engine object. The engine is
     * determined based on the NLP engine JARs introduced by the user via SPI mechanism.
     *
     * @return A new {@link NLPProvider} instance.
     * @throws InternalException if no tokenizer implementation is found on the classpath.
     */
    public static NLPProvider createEngine() {
        return doCreateEngine();
    }

    /**
     * Creates a custom word segmentation engine object by its name. The engine name is case-insensitive and can
     * optionally include the "Engine" suffix. Built-in engine names are {@code ansj}, {@code hanlp}, {@code jcseg},
     * {@code jieba}, {@code mmseg}, {@code mynlp}, {@code smartcn}, and {@code word}.
     *
     * @param engineName The name of the engine to create, optionally ending in {@code Engine}.
     * @return An {@link NLPProvider} instance corresponding to the given engine name.
     * @throws IllegalArgumentException if the engine name is null or blank
     * @throws InternalException        if no engine with the corresponding name is available via SPI, a provider
     *                                  declares an invalid type, or duplicate types are discovered
     */
    public static NLPProvider createEngine(String engineName) throws InternalException {
        Logger.debug(true, "Extra", "Named segmentation engine lookup started: requestedEngine={}", engineName);
        if (engineName == null || engineName.isBlank()) {
            throw new IllegalArgumentException("NLP engine name must not be null or blank");
        }
        final String candidate = engineName.trim();
        final String requested = StringKit.endWithIgnoreCase(candidate, "Engine")
                ? candidate.substring(0, candidate.length() - "Engine".length())
                : candidate;
        final ServiceLoader<NLPProvider> list = NormalSpiLoader.loadList(NLPProvider.class);
        Logger.debug(
                true,
                "Extra",
                "Named segmentation engine candidates loaded: normalizedEngine={}, candidateCount={}",
                requested,
                list.getServiceNames().size());
        NLPProvider match = null;
        for (final String serviceName : list.getServiceNames()) {
            final NLPProvider provider;
            try {
                provider = list.getService(serviceName);
            } catch (RuntimeException | ServiceConfigurationError | LinkageError unavailable) {
                continue;
            }
            if (provider != null) {
                final String type = provider.type();
                if (type == null || type.isBlank()) {
                    throw new InternalException(
                            "NLP provider type must not be null or blank: " + provider.getClass().getName());
                }
                if (!type.equalsIgnoreCase(requested)) {
                    continue;
                }
                if (match != null) {
                    throw new InternalException("Duplicate NLP provider type '" + requested + "': "
                            + match.getClass().getName() + " and " + provider.getClass().getName());
                }
                match = provider;
                Logger.debug(
                        false,
                        "Extra",
                        "Named segmentation engine selected: normalizedEngine={}, serviceName={}, provider={}",
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
                "Named segmentation engine lookup failed: normalizedEngine={}, candidateCount={}",
                requested,
                list.getServiceNames().size());
        throw new InternalException("No such provider named: " + requested);
    }

    /**
     * Internal method to automatically create the corresponding word segmentation engine object. It uses
     * {@link NormalSpiLoader} to find the first available {@link NLPProvider} implementation via Java's Service
     * Provider Interface (SPI) mechanism.
     *
     * @return An {@link NLPProvider} instance.
     * @throws InternalException if no tokenizer implementation is found on the classpath.
     */
    private static NLPProvider doCreateEngine() {
        Logger.debug(true, "Extra", "SPI segmentation engine lookup started");
        final NLPProvider engine = NormalSpiLoader.loadFirstAvailable(NLPProvider.class);
        if (null != engine) {
            Logger.debug(
                    false,
                    "Extra",
                    "SPI segmentation engine selected: provider={}",
                    engine.getClass().getSimpleName());
            return engine;
        }

        Logger.warn(false, "Extra", "SPI segmentation engine lookup failed: providerPresent={}", false);
        throw new InternalException("No tokenizer found !Please add some tokenizer jar to your project !");
    }

}
