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
package org.miaixz.bus.spring.env;

import org.miaixz.bus.core.xyz.SetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An {@link EnvironmentPostProcessor} implementation that loads configuration properties based on defined scenes.
 * <p>
 * This post-processor allows for dynamic loading of configuration files (e.g., {@code application-dev.yml}) based on a
 * {@code bus.scenes} property in the environment. It searches for scene-specific configuration files in the
 * {@code classpath:/bus-scenes/} directory.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ScenesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Post-processes the environment to load scene-specific configuration properties.
     * <p>
     * It retrieves the {@code bus.scenes} property, and if present, loads corresponding configuration files (e.g.,
     * {@code classpath:/bus-scenes/sceneName.yml}) and adds them to the environment's property sources.
     * </p>
     *
     * @param environment The configurable environment.
     * @param application The Spring application instance.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        ResourceLoader resourceLoader = application.getResourceLoader();
        resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
        List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader
                .loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
        String scenesValue = environment.getProperty(GeniusBuilder.BUS_SCENES);
        if (!StringKit.hasText(scenesValue)) {
            return;
        }
        Set<String> scenes = SetKit.of(scenesValue);
        List<SceneConfigDataReference> sceneConfigDataReferences = scenesResources(
                resourceLoader,
                propertySourceLoaders,
                scenes);

        Logger.info("Configs for scenes {} enable", scenes);
        processAndApply(sceneConfigDataReferences, environment);

    }

    /**
     * Returns the order value for this post-processor.
     * <p>
     * This ensures that scene-specific configurations are loaded with a higher precedence than default configurations
     * but after other core environment post-processors.
     * </p>
     *
     * @return The order value.
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    /**
     * Locates scene-specific configuration resources.
     *
     * @param resourceLoader        The resource loader to use.
     * @param propertySourceLoaders A list of property source loaders.
     * @param scenes                A set of scene names.
     * @return A list of {@link SceneConfigDataReference} objects for found resources.
     */
    private List<SceneConfigDataReference> scenesResources(
            ResourceLoader resourceLoader,
            List<PropertySourceLoader> propertySourceLoaders,
            Set<String> scenes) {
        List<SceneConfigDataReference> resources = new ArrayList<>();
        if (scenes != null && !scenes.isEmpty()) {
            scenes.forEach(scene -> propertySourceLoaders.forEach(psl -> {
                for (String extension : psl.getFileExtensions()) {
                    String location = "classpath:/" + GeniusBuilder.BUS_SCENES_PATH + File.separator + scene + "."
                            + extension;
                    Resource resource = resourceLoader.getResource(location);
                    if (resource.exists()) {
                        resources.add(new SceneConfigDataReference(location, resource, psl));
                    }
                }
            }));
        }
        return resources;
    }

    /**
     * Processes and applies all scene configuration property sources to the {@link ConfigurableEnvironment}.
     *
     * @param sceneConfigDataReferences A list of scene configuration data references.
     * @param environment               The configurable environment to which properties will be added.
     * @throws IllegalStateException if an I/O error occurs while loading scene config data.
     */
    private void processAndApply(
            List<SceneConfigDataReference> sceneConfigDataReferences,
            ConfigurableEnvironment environment) {
        for (SceneConfigDataReference sceneConfigDataReference : sceneConfigDataReferences) {
            try {
                List<PropertySource<?>> propertySources = sceneConfigDataReference.propertySourceLoader
                        .load(sceneConfigDataReference.getName(), sceneConfigDataReference.getResource());
                if (propertySources != null) {
                    propertySources.forEach(environment.getPropertySources()::addLast);
                }
            } catch (IOException e) {
                throw new IllegalStateException(
                        "IO error on loading scene config data from " + sceneConfigDataReference.name, e);
            }
        }
    }

    /**
     * A simple data class to hold a reference to a scene-specific configuration resource along with its name and the
     * {@link PropertySourceLoader} capable of loading it.
     */
    private static class SceneConfigDataReference {

        private String name;
        private Resource resource;
        private PropertySourceLoader propertySourceLoader;

        /**
         * Constructs a new {@code SceneConfigDataReference}.
         *
         * @param name                 The name of the configuration resource.
         * @param resource             The {@link Resource} object pointing to the configuration file.
         * @param propertySourceLoader The {@link PropertySourceLoader} to use for this resource.
         */
        public SceneConfigDataReference(String name, Resource resource, PropertySourceLoader propertySourceLoader) {
            this.name = name;
            this.resource = resource;
            this.propertySourceLoader = propertySourceLoader;
        }

        /**
         * Gets the resource object.
         *
         * @return The {@link Resource} object.
         */
        public Resource getResource() {
            return resource;
        }

        /**
         * Sets the resource object.
         *
         * @param resource The {@link Resource} object to set.
         */
        public void setResource(Resource resource) {
            this.resource = resource;
        }

        /**
         * Gets the property source loader.
         *
         * @return The {@link PropertySourceLoader} instance.
         */
        public PropertySourceLoader getPropertySourceLoader() {
            return propertySourceLoader;
        }

        /**
         * Sets the property source loader.
         *
         * @param propertySourceLoader The {@link PropertySourceLoader} instance to set.
         */
        public void setPropertySourceLoader(PropertySourceLoader propertySourceLoader) {
            this.propertySourceLoader = propertySourceLoader;
        }

        /**
         * Gets the name of the configuration resource.
         *
         * @return The name of the resource.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the configuration resource.
         *
         * @param name The name to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

}
