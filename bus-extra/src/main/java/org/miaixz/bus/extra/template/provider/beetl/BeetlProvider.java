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
package org.miaixz.bus.extra.template.provider.beetl;

import java.io.IOException;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.resource.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;

/**
 * Beetl template engine encapsulation. This class provides an implementation of {@link TemplateProvider} for the Beetl
 * template engine, allowing for configuration and retrieval of Beetl templates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeetlProvider implements TemplateProvider {

    /**
     * The underlying Beetl GroupTemplate engine.
     */
    private GroupTemplate engine;

    /**
     * Default constructor for BeetlProvider. Checks if the Beetl library is available (via {@link GroupTemplate}
     * class).
     */
    public BeetlProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(GroupTemplate.class);
    }

    /**
     * Constructs a new BeetlProvider with the given template configuration.
     *
     * @param config The {@link TemplateConfig} for initializing the Beetl engine.
     */
    public BeetlProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * Constructs a new BeetlProvider with an existing Beetl {@link GroupTemplate} instance.
     *
     * @param engine The pre-initialized {@link GroupTemplate} instance.
     */
    public BeetlProvider(final GroupTemplate engine) {
        init(engine);
    }

    /**
     * Creates a new {@link GroupTemplate} instance based on the provided {@link TemplateConfig}. This method determines
     * the appropriate {@link ResourceLoader} based on the resource mode in the config.
     *
     * @param config The {@link TemplateConfig} containing settings for the template engine.
     * @return A new {@link GroupTemplate} instance.
     */
    private static GroupTemplate of(TemplateConfig config) {
        if (null == config) {
            config = TemplateConfig.DEFAULT;
        }

        switch (config.getResourceMode()) {
            case CLASSPATH:
                return createGroupTemplate(new ClasspathResourceLoader(config.getPath(), config.getCharsetString()));

            case FILE:
                return createGroupTemplate(new FileResourceLoader(config.getPath(), config.getCharsetString()));

            case WEB_ROOT:
                return createGroupTemplate(new WebAppResourceLoader(config.getPath(), config.getCharsetString()));

            case STRING:
                return createGroupTemplate(new StringTemplateResourceLoader());

            case COMPOSITE:
                // TODO Need to define a composite resource loader
                return createGroupTemplate(new CompositeResourceLoader());

            default:
                return new GroupTemplate();
        }
    }

    /**
     * Creates a custom {@link GroupTemplate} with the given {@link ResourceLoader} and default configuration. Custom
     * Beetl configuration can be provided via a `beetl.properties` file in the ClassPath.
     *
     * @param loader The {@link ResourceLoader} to use for loading template resources.
     * @return A new {@link GroupTemplate} instance.
     * @throws InternalException if an {@link IOException} occurs during configuration.
     */
    private static GroupTemplate createGroupTemplate(final ResourceLoader<?> loader) {
        try {
            return createGroupTemplate(loader, Configuration.defaultConfiguration());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a custom {@link GroupTemplate} with the given {@link ResourceLoader} and {@link Configuration}.
     *
     * @param loader The {@link ResourceLoader} to use for loading template resources.
     * @param conf   The {@link Configuration} for the Beetl engine.
     * @return A new {@link GroupTemplate} instance.
     */
    private static GroupTemplate createGroupTemplate(final ResourceLoader<?> loader, final Configuration conf) {
        return new GroupTemplate(loader, conf);
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
    public TemplateProvider init(final TemplateConfig config) {
        init(of(config));
        return this;
    }

    /**
     * Initializes the Beetl engine with a pre-configured {@link GroupTemplate}.
     *
     * @param engine The {@link GroupTemplate} instance to use.
     */
    private void init(final GroupTemplate engine) {
        this.engine = engine;
    }

    /**
     * Gets a template by name. This method is designed to be overridden by subclasses for custom template retrieval.
     *
     * Subclasses may override to add caching, custom loading, or error handling.
     *
     * @param resource The name of the template resource.
     * @return The template object.
     */
    @Override
    public Template getTemplate(final String resource) {
        if (null == this.engine) {
            init(TemplateConfig.DEFAULT);
        }
        return BeetlTemplate.wrap(engine.getTemplate(resource));
    }

    /**
     * Provides a hook to access the raw Beetl {@link GroupTemplate} engine. This can be used for custom configurations
     * or accessing specific Beetl functionalities, such as plugins.
     *
     * @return The raw {@link GroupTemplate} instance.
     */
    @Override
    public GroupTemplate getRaw() {
        return this.engine;
    }

}
