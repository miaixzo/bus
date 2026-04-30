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
import org.miaixz.bus.logger.Logger;

/**
 * Beetl template engine encapsulation. This class provides an implementation of {@link TemplateProvider} for the Beetl
 * template engine, allowing for configuration and retrieval of Beetl templates.
 *
 * @author Kimi Liu
 * @since Java 21+
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

        Logger.info(
                true,
                "Extra",
                "component=template, Beetl group template creation started: resourceMode={}, charset={}, pathPresent={}",
                config.getResourceMode(),
                config.getCharsetString(),
                config.getPath() != null);
        final GroupTemplate groupTemplate;
        switch (config.getResourceMode()) {
            case CLASSPATH:
                groupTemplate = createGroupTemplate(
                        new ClasspathResourceLoader(config.getPath(), config.getCharsetString()));
                break;

            case FILE:
                groupTemplate = createGroupTemplate(
                        new FileResourceLoader(config.getPath(), config.getCharsetString()));
                break;

            case WEB_ROOT:
                groupTemplate = createGroupTemplate(
                        new WebAppResourceLoader(config.getPath(), config.getCharsetString()));
                break;

            case STRING:
                groupTemplate = createGroupTemplate(new StringTemplateResourceLoader());
                break;

            case COMPOSITE:
                // TODO Need to define a composite resource loader
                groupTemplate = createGroupTemplate(new CompositeResourceLoader());
                break;

            default:
                groupTemplate = new GroupTemplate();
                break;
        }
        Logger.info(
                false,
                "Extra",
                "component=template, Beetl group template created: resourceMode={}, charset={}, pathPresent={}, groupTemplatePresent={}",
                config.getResourceMode(),
                config.getCharsetString(),
                config.getPath() != null,
                groupTemplate != null);
        return groupTemplate;
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
            Logger.debug(
                    true,
                    "Extra",
                    "component=template, Beetl group template default configuration loading started: loader={}",
                    loader == null ? "null" : loader.getClass().getSimpleName());
            return createGroupTemplate(loader, Configuration.defaultConfiguration());
        } catch (final IOException e) {
            Logger.warn(
                    false,
                    "Extra",
                    e,
                    "component=template, Beetl group template default configuration loading failed: loader={}, exception={}",
                    loader == null ? "null" : loader.getClass().getSimpleName(),
                    e.getClass().getSimpleName());
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
        Logger.info(
                true,
                "Extra",
                "component=template, Beetl provider initialization started: configPresent={}, resourceMode={}, charset={}, pathPresent={}",
                config != null,
                config == null ? null : config.getResourceMode(),
                config == null ? null : config.getCharsetString(),
                config != null && config.getPath() != null);
        init(of(config));
        Logger.info(
                false,
                "Extra",
                "component=template, Beetl provider initialized: rawPresent={}",
                this.engine != null);
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
            Logger.debug(true, "Extra", "component=template, Beetl provider lazy initialization requested");
            init(TemplateConfig.DEFAULT);
        }
        Logger.debug(
                true,
                "Extra",
                "component=template, Beetl template loading started: resourcePresent={}, resourceLength={}",
                resource != null,
                resource == null ? 0 : resource.length());
        final Template template = BeetlTemplate.wrap(engine.getTemplate(resource));
        Logger.debug(
                false,
                "Extra",
                "component=template, Beetl template loaded: resourcePresent={}, resourceLength={}, templatePresent={}",
                resource != null,
                resource == null ? 0 : resource.length(),
                template != null);
        return template;
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
