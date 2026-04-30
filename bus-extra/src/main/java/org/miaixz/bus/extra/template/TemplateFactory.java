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
package org.miaixz.bus.extra.template;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Simple template engine factory that automatically creates the corresponding template engine object based on the
 * template engine JARs introduced by the user. It uses the Simple Factory pattern.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TemplateFactory {

    /**
     * Retrieves a singleton instance of {@link TemplateProvider} based on the available template engine JARs. The first
     * available template engine found via SPI will be used.
     *
     * @return A singleton instance of {@link TemplateProvider}.
     */
    public static TemplateProvider get() {
        Logger.debug(true, "Extra", "Default template provider lookup started");
        final TemplateProvider engine = Instances.get(TemplateProvider.class.getName(), TemplateFactory::of);
        Logger.debug(
                false,
                "Extra",
                "Default template engine selected: engine={}",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * Creates a new {@link TemplateProvider} instance using the default configuration. This method returns a new engine
     * instance each time it is called.
     *
     * @return A new {@link TemplateProvider} instance.
     */
    public static TemplateProvider of() {
        Logger.debug(true, "Extra", "Template provider creation requested with default config");
        return of(TemplateConfig.DEFAULT);
    }

    /**
     * Creates a new {@link TemplateProvider} instance with the specified configuration. This method returns a new
     * engine instance each time it is called.
     *
     * @param config The template configuration, including encoding, template file path, etc.
     * @return A new {@link TemplateProvider} instance.
     */
    public static TemplateProvider of(final TemplateConfig config) {
        Logger.debug(
                true,
                "Extra",
                "Template provider creation requested: configPresent={}, resourceMode={}, charset={}, useCache={}, customProviderPresent={}",
                config != null,
                config == null ? null : config.getResourceMode(),
                config == null ? null : config.getCharsetString(),
                config != null && config.isUseCache(),
                config != null && config.getProvider() != null);
        return get(config);
    }

    /**
     * Internal method to create a {@link TemplateProvider} instance based on the given configuration. It first checks
     * for a custom provider specified in the config, then falls back to SPI loading.
     *
     * @param config The template configuration, including encoding, template file path, etc.
     * @return A new {@link TemplateProvider} instance.
     * @throws InternalException if no template engine is found or available.
     */
    private static TemplateProvider get(final TemplateConfig config) {
        final long startedAt = System.nanoTime();
        final Class<? extends TemplateProvider> customEngineClass = config.getProvider();
        final TemplateProvider engine;
        if (null != customEngineClass) {
            // Custom template engine
            Logger.debug(
                    true,
                    "Extra",
                    "Custom template provider creation started: provider={}",
                    customEngineClass.getName());
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI engine lookup
            Logger.debug(
                    true,
                    "Extra",
                    "SPI template provider lookup started: resourceMode={}",
                    config.getResourceMode());
            engine = NormalSpiLoader.loadFirstAvailable(TemplateProvider.class);
        }
        if (null != engine) {
            Logger.debug(
                    false,
                    "Extra",
                    "Template provider discovered: provider={}, elapsedMs={}",
                    engine.getClass().getName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            final TemplateProvider provider = engine.init(config);
            Logger.info(
                    false,
                    "Extra",
                    "Template provider initialized by factory: provider={}, resourceMode={}, charset={}, useCache={}, pathPresent={}, elapsedMs={}",
                    provider == null ? "null" : provider.getClass().getName(),
                    config.getResourceMode(),
                    config.getCharsetString(),
                    config.isUseCache(),
                    StringKit.isNotEmpty(config.getPath()),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            return provider;
        }

        Logger.warn(
                false,
                "Extra",
                "Template provider lookup failed: customProviderPresent={}, elapsedMs={}",
                customEngineClass != null,
                (System.nanoTime() - startedAt) / 1_000_000L);
        throw new InternalException("No template found! Please add one of template jar to your project !");
    }

}
