/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 实现{@link EnvironmentPostProcessor}按场景加载配置。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ScenesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        ResourceLoader resourceLoader = application.getResourceLoader();
        resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
        List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader.loadFactories(
                PropertySourceLoader.class, getClass().getClassLoader());
        String scenesValue = environment.getProperty(GeniusBuilder.BUS_SCENES);
        if (!StringKit.hasText(scenesValue)) {
            return;
        }
        Set<String> scenes = SetKit.of(scenesValue);
        List<SceneConfigDataReference> sceneConfigDataReferences = scenesResources(resourceLoader,
                propertySourceLoaders, scenes);

        Logger.info("Configs for scenes {} enable", scenes);
        processAndApply(sceneConfigDataReferences, environment);

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    private List<SceneConfigDataReference> scenesResources(ResourceLoader resourceLoader, List<PropertySourceLoader> propertySourceLoaders,
                                                           Set<String> scenes) {
        List<SceneConfigDataReference> resources = new ArrayList<>();
        if (scenes != null && !scenes.isEmpty()) {
            scenes.forEach(scene -> propertySourceLoaders.forEach(psl -> {
                for (String extension : psl.getFileExtensions()) {
                    String location =
                            "classpath:/" + GeniusBuilder.BUS_SCENES_PATH + File.separator + scene + "." + extension;
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
     * 将所有场景配置属性源处理到{@link org.springframework.core.env.Environment}。
     *
     * @param sceneConfigDataReferences 场景配置数据
     * @param environment               环境资源信息
     */
    private void processAndApply(List<SceneConfigDataReference> sceneConfigDataReferences, ConfigurableEnvironment environment) {
        for (SceneConfigDataReference sceneConfigDataReference :
                sceneConfigDataReferences) {
            try {
                List<PropertySource<?>> propertySources = sceneConfigDataReference.propertySourceLoader.load(
                        sceneConfigDataReference.getName(),
                        sceneConfigDataReference.getResource());
                if (propertySources != null) {
                    propertySources.forEach(environment.getPropertySources()::addLast);
                }
            } catch (IOException e) {
                throw new IllegalStateException("IO error on loading scene config data from " + sceneConfigDataReference.name, e);
            }
        }
    }

    private static class SceneConfigDataReference {

        private String name;
        private Resource resource;
        private PropertySourceLoader propertySourceLoader;

        public SceneConfigDataReference(String name, Resource resource,
                                        PropertySourceLoader propertySourceLoader) {
            this.name = name;
            this.resource = resource;
            this.propertySourceLoader = propertySourceLoader;
        }

        /**
         * 属性的获取方法
         *
         * @return 资源属性值
         */
        public Resource getResource() {
            return resource;
        }

        /**
         * 属性的设置方法
         *
         * @param resource 要分配给属性资源的值
         */
        public void setResource(Resource resource) {
            this.resource = resource;
        }

        /**
         * 属性propertySourceLoader的getter方法
         *
         * @return roperty的属性值
         */
        public PropertySourceLoader getPropertySourceLoader() {
            return propertySourceLoader;
        }

        /**
         * 属性propertySourceLoader的setter方法
         *
         * @param propertySourceLoader 属性propertySourceLoader的值
         */
        public void setPropertySourceLoader(PropertySourceLoader propertySourceLoader) {
            this.propertySourceLoader = propertySourceLoader;
        }

        /**
         * 属性名的getter方法
         *
         * @return 名称的属性值
         */
        public String getName() {
            return name;
        }

        /**
         * 属性名的setter方法
         *
         * @param name 分配给属性名称的值
         */
        public void setName(String name) {
            this.name = name;
        }
    }

}
