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
 * @since Java 17+
 */
public class NLPFactory {

    /**
     * Automatically creates and retrieves a singleton instance of the corresponding word segmentation engine object.
     * The engine is determined based on the NLP engine JARs introduced by the user via SPI mechanism. The chosen
     * engine's simple name (without "Engine" suffix) is logged for informational purposes.
     *
     * @return A singleton instance of {@link NLPProvider}.
     */
    public static NLPProvider getEngine() {
        final NLPProvider engine = Instances.get(NLPProvider.class.getName(), NLPFactory::createEngine);
        Logger.debug(
                "Use [{}] Tokenizer Engine As Default.",
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
     * optionally include the "Engine" suffix. Supported engine names include, but are not limited to: `Analysis`,
     * `Ansj`, `HanLP`, `IKAnalyzer`, `Jcseg`, `Jieba`, `Mmseg`, `Mynlp`, `Word`.
     *
     * @param engineName The name of the engine to create (e.g., `Analysis`, `Ansj`).
     * @return An {@link NLPProvider} instance corresponding to the given engine name.
     * @throws InternalException if no engine with the corresponding name is found via SPI.
     */
    public static NLPProvider createEngine(String engineName) throws InternalException {
        if (!StringKit.endWithIgnoreCase(engineName, "Engine")) {
            engineName = engineName + "Engine";
        }
        final ServiceLoader<NLPProvider> list = NormalSpiLoader.loadList(NLPProvider.class);
        for (final String serviceName : list.getServiceNames()) {
            if (StringKit.endWithIgnoreCase(serviceName, engineName)) {
                return list.getService(serviceName);
            }
        }
        throw new InternalException("No such provider named: " + engineName);
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
        final NLPProvider engine = NormalSpiLoader.loadFirstAvailable(NLPProvider.class);
        if (null != engine) {
            return engine;
        }

        throw new InternalException("No tokenizer found !Please add some tokenizer jar to your project !");
    }

}
