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
 * @since Java 17+
 */
public class TemplateFactory {

    /**
     * Retrieves a singleton instance of {@link TemplateProvider} based on the available template engine JARs. The first
     * available template engine found via SPI will be used.
     *
     * @return A singleton instance of {@link TemplateProvider}.
     */
    public static TemplateProvider get() {
        final TemplateProvider engine = Instances.get(TemplateProvider.class.getName(), TemplateFactory::of);
        Logger.debug(
                "Use [{}] Template Engine As Default.",
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
        final Class<? extends TemplateProvider> customEngineClass = config.getProvider();
        final TemplateProvider engine;
        if (null != customEngineClass) {
            // Custom template engine
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI engine lookup
            engine = NormalSpiLoader.loadFirstAvailable(TemplateProvider.class);
        }
        if (null != engine) {
            return engine.init(config);
        }

        throw new InternalException("No template found! Please add one of template jar to your project !");
    }

}
