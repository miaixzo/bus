/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.template.provider.thymeleaf;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.*;

/**
 * Thymeleaf template engine implementation. This class provides an implementation of {@link TemplateProvider} for the
 * Thymeleaf template engine, allowing for configuration and retrieval of Thymeleaf templates. For more information,
 * see: https://www.thymeleaf.org/
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThymeleafProvider implements TemplateProvider {

    /**
     * The underlying Thymeleaf template engine object.
     */
    TemplateEngine engine;
    /**
     * The template configuration.
     */
    TemplateConfig config;

    /**
     * Default constructor for ThymeleafProvider. Checks if the Thymeleaf library is available (via
     * {@link org.thymeleaf.TemplateEngine} class).
     */
    public ThymeleafProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(org.thymeleaf.TemplateEngine.class);
    }

    /**
     * Constructs a new ThymeleafProvider with the given template configuration.
     *
     * @param config The {@link TemplateConfig} for initializing the Thymeleaf engine.
     */
    public ThymeleafProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * Constructs a new ThymeleafProvider with an existing Thymeleaf {@link TemplateEngine} instance.
     *
     * @param engine The pre-initialized {@link TemplateEngine} instance.
     */
    public ThymeleafProvider(final TemplateEngine engine) {
        init(engine);
    }

    /**
     * Creates a new Thymeleaf {@link TemplateEngine} instance based on the provided {@link TemplateConfig}. This method
     * sets up the appropriate {@link ITemplateResolver} based on the resource mode in the config.
     *
     * @param config The {@link TemplateConfig} containing settings for the template engine.
     * @return A new {@link TemplateEngine} instance.
     */
    private static TemplateEngine of(TemplateConfig config) {
        if (null == config) {
            config = new TemplateConfig();
        }

        final ITemplateResolver resolver;
        switch (config.getResourceMode()) {
            case CLASSPATH:
                final ClassLoaderTemplateResolver classLoaderResolver = new ClassLoaderTemplateResolver();
                classLoaderResolver.setCharacterEncoding(config.getCharsetString());
                classLoaderResolver.setTemplateMode(TemplateMode.HTML);
                classLoaderResolver.setPrefix(StringKit.addSuffixIfNot(config.getPath(), "/"));
                classLoaderResolver.setCacheable(config.isUseCache());
                resolver = classLoaderResolver;
                break;

            case FILE:
                final FileTemplateResolver fileResolver = new FileTemplateResolver();
                fileResolver.setCharacterEncoding(config.getCharsetString());
                fileResolver.setTemplateMode(TemplateMode.HTML);
                fileResolver.setPrefix(StringKit.addSuffixIfNot(config.getPath(), "/"));
                fileResolver.setCacheable(config.isUseCache());
                resolver = fileResolver;
                break;

            case WEB_ROOT:
                final FileTemplateResolver webRootResolver = new FileTemplateResolver();
                webRootResolver.setCharacterEncoding(config.getCharsetString());
                webRootResolver.setTemplateMode(TemplateMode.HTML);
                webRootResolver.setPrefix(
                        StringKit.addSuffixIfNot(
                                FileKit.getAbsolutePath(FileKit.file(FileKit.getWebRoot(), config.getPath())),
                                "/"));
                webRootResolver.setCacheable(config.isUseCache());
                resolver = webRootResolver;
                break;

            case STRING:
                resolver = new StringTemplateResolver();
                break;

            default:
                resolver = new DefaultTemplateResolver();
                break;
        }

        final TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
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
        this.config = config;
        init(of(config));
        return this;
    }

    /**
     * Initializes the Thymeleaf engine with a pre-configured {@link TemplateEngine}.
     *
     * @param engine The {@link TemplateEngine} instance to use.
     */
    private void init(final TemplateEngine engine) {
        this.engine = engine;
    }

    /**
     * Gets a template by name. This method is designed to be overridden by subclasses for custom template retrieval.
     *
     * into a ThymeleafTemplate. Subclasses may override to add caching or custom loading.
     *
     * @param resource The name of the template resource.
     * @return The template object.
     */
    @Override
    public Template getTemplate(final String resource) {
        if (null == this.engine) {
            init(TemplateConfig.DEFAULT);
        }
        return ThymeleafTemplate.wrap(this.engine, resource, (null == this.config) ? null : this.config.getCharset());
    }

    /**
     * Provides a hook to access the raw Thymeleaf {@link TemplateEngine} engine. This can be used for custom
     * configurations or accessing specific Thymeleaf functionalities, such as plugins.
     *
     * @return The raw {@link TemplateEngine} instance.
     */
    @Override
    public TemplateEngine getRaw() {
        return this.engine;
    }

}
