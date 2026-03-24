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
package org.miaixz.bus.spring;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.Nexus;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds Spring context information and provides utility methods.
 * <p>
 * This class is responsible for scanning packages for class objects and acts as a central holder for various
 * Spring-related configurations. It is conditionally activated by {@link Nexus} and has a high order to ensure early
 * initialization.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ComponentScan("org.miaixz.**")
@Conditional(Nexus.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringHolder {

    /**
     * A static flag indicating whether the Spring application context is alive.
     */
    public static boolean alive = false;

    /**
     * Scans a given package and returns a set of all class objects found within it.
     * <p>
     * This method uses Spring's {@link PathMatchingResourcePatternResolver} to find class files and
     * {@link CachingMetadataReaderFactory} to read their metadata without loading the classes.
     * </p>
     *
     * @param packageName The package path to scan (e.g., "org.miaixz.bus.example").
     * @return A {@link Set} of {@link Class} objects found in the specified package.
     */
    public static Set<Class<?>> scan(String packageName) {
        Set<Class<?>> handlerSet = new HashSet<>();
        try {
            String pattern = "classpath*:" + packageName.replace(Symbol.C_DOT, Symbol.C_SLASH) + "/**/*.class";
            PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (Resource resource : resources) {
                try {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(className);
                    handlerSet.add(clazz);
                } catch (Exception e) {
                    // Log the exception but continue scanning other resources
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Log the exception if resource pattern resolution fails
            e.printStackTrace();
        }
        return handlerSet;
    }

}
