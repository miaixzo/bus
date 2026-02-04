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
package org.miaixz.bus.extra.template.provider.freemarker;

import java.io.IOException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

/**
 * FreeMarker template engine encapsulation. This class provides an implementation of {@link TemplateProvider} for the
 * FreeMarker template engine, allowing for configuration and retrieval of FreeMarker templates. For more information,
 * see: <a href="https://freemarker.apache.org/">https://freemarker.apache.org/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerProvider implements TemplateProvider {

    /**
     * The underlying FreeMarker configuration object.
     */
    private Configuration cfg;

    /**
     * Default constructor for FreemarkerProvider. Checks if the FreeMarker library is available (via
     * {@link Configuration} class).
     */
    public FreemarkerProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(Configuration.class);
    }

    /**
     * Constructs a new FreemarkerProvider with the given template configuration.
     *
     * @param config The {@link TemplateConfig} for initializing the FreeMarker engine.
     */
    public FreemarkerProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * Constructs a new FreemarkerProvider with an existing FreeMarker {@link Configuration} instance.
     *
     * @param freemarkerCfg The pre-initialized {@link Configuration} instance.
     */
    public FreemarkerProvider(final Configuration freemarkerCfg) {
        init(freemarkerCfg);
    }

    /**
     * Creates a new FreeMarker {@link Configuration} instance based on the provided {@link TemplateConfig}. This method
     * sets up the template loader and default encoding according to the configuration.
     *
     * @param config The {@link TemplateConfig} containing settings for the template engine.
     * @return A new {@link Configuration} instance.
     * @throws InternalException if an {@link IOException} occurs during template loader setup.
     */
    private static Configuration of(TemplateConfig config) {
        if (null == config) {
            config = new TemplateConfig();
        }

        final Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setLocalizedLookup(false);
        cfg.setDefaultEncoding(config.getCharset().toString());

        switch (config.getResourceMode()) {
            case CLASSPATH:
                cfg.setTemplateLoader(new ClassTemplateLoader(ClassKit.getClassLoader(), config.getPath()));
                break;

            case FILE:
                try {
                    cfg.setTemplateLoader(new FileTemplateLoader(FileKit.file(config.getPath())));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
                break;

            case WEB_ROOT:
                try {
                    cfg.setTemplateLoader(new FileTemplateLoader(FileKit.file(FileKit.getWebRoot(), config.getPath())));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
                break;

            case STRING:
                cfg.setTemplateLoader(new SimpleStringTemplateLoader());
                break;

            default:
                break;
        }

        return cfg;
    }

    /**
     * Initializes the template provider with the given configuration. This method is designed to be overridden by
     * subclasses for custom initialization.
     *
     * from the TemplateConfig and initializes the provider. Subclasses may override to add custom configuration
     * settings or validation.
     *
     * @param config The template configuration, or null to use defaults.
     * @return This provider instance for method chaining.
     */
    @Override
    public TemplateProvider init(TemplateConfig config) {
        if (null == config) {
            config = TemplateConfig.DEFAULT;
        }
        init(of(config));
        return this;
    }

    /**
     * Initializes the FreeMarker engine with a pre-configured {@link Configuration}.
     *
     * @param freemarkerCfg The {@link Configuration} instance to use.
     */
    private void init(final Configuration freemarkerCfg) {
        this.cfg = freemarkerCfg;
    }

    /**
     * Gets a template by name. This method is designed to be overridden by subclasses for custom template retrieval.
     *
     * Subclasses may override to add caching, custom loading, or error handling.
     *
     * @param resource The name of the template resource.
     * @return The template object.
     * @throws InternalException if the template cannot be loaded.
     */
    @Override
    public Template getTemplate(final String resource) {
        if (null == this.cfg) {
            init(TemplateConfig.DEFAULT);
        }
        try {
            return FreemarkerTemplate.wrap(this.cfg.getTemplate(resource));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Provides a hook to access the raw FreeMarker {@link Configuration} engine. This can be used for custom
     * configurations or accessing specific FreeMarker functionalities, such as plugins.
     *
     * @return The raw {@link Configuration} instance.
     */
    @Override
    public Configuration getRaw() {
        return this.cfg;
    }

}
