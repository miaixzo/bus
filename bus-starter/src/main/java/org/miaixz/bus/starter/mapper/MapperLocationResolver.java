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
package org.miaixz.bus.starter.mapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Resolves configured MyBatis mapper XML locations for both JVM startup and AOT resource hint generation.
 * <p>
 * {@link MapperProperties#getMapperLocations()} is the single source of truth. Runtime consumers use the resolved
 * resources, while AOT consumers register the normalized classpath patterns in the native image.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MapperLocationResolver {

    /**
     * Prefix for a single-classpath mapper resource.
     */
    private static final String CLASSPATH_PREFIX = Normal.CLASSPATH;

    /**
     * Prefix for an all-classpath mapper resource pattern.
     */
    private static final String ALL_CLASSPATH_PREFIX = "classpath" + Symbol.STAR + Symbol.COLON;

    /**
     * Prevents instantiation of this mapper resource resolver.
     */
    private MapperLocationResolver() {
        // No initialization required.
    }

    /**
     * Returns a Mapper resource-pattern resolver backed by the supplied resource loader.
     *
     * @param resourceLoader resource loader to adapt, or {@code null} for the default resolver
     * @return Mapper resource-pattern resolver
     */
    public static ResourcePatternResolver getPatternResolver(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof ResourcePatternResolver resolver) {
            return resolver;
        }
        if (resourceLoader != null) {
            return new PathMatchingResourcePatternResolver(resourceLoader);
        }
        return new PathMatchingResourcePatternResolver();
    }

    /**
     * Resolves all configured mapper locations and normalizes their native-image resource patterns.
     *
     * @param properties mapper configuration properties
     * @param resolver   Spring resource pattern resolver
     * @return resolved resources and normalized classpath patterns
     * @throws IllegalStateException when a location is unsupported, invalid, or cannot be resolved
     */
    public static Result resolve(MapperProperties properties, ResourcePatternResolver resolver) {
        Objects.requireNonNull(properties, "MapperProperties must not be null");
        Objects.requireNonNull(resolver, "ResourcePatternResolver must not be null");
        String[] locations = properties.getMapperLocations();
        if (locations == null || locations.length == 0) {
            return new Result(new Resource[0], Set.of());
        }

        List<Resource> resources = new ArrayList<>();
        Set<String> patterns = new LinkedHashSet<>();
        for (int index = 0; index < locations.length; index++) {
            String location = locations[index];
            String pattern = normalize(location, index);
            Resource[] resolved;
            try {
                resolved = resolver.getResources(location);
            } catch (FileNotFoundException e) {
                if (!isPattern(location)) {
                    throw new IllegalStateException(
                            "Failed to resolve mapper XML resource at configured location index " + index, e);
                }
                resolved = new Resource[0];
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to resolve mapper XML resources at configured location index " + index, e);
            }
            patterns.add(pattern);
            resources.addAll(Arrays.asList(resolved));
        }

        Resource[] distinctResources = new LinkedHashSet<>(resources).toArray(Resource[]::new);
        return new Result(distinctResources, Set.copyOf(patterns));
    }

    /**
     * Normalizes one mapper resource location.
     *
     * @param location mapper resource location
     * @param index    location index used in validation messages
     * @return normalized mapper resource location
     */
    private static String normalize(String location, int index) {
        if (StringKit.isBlank(location)) {
            throw new IllegalStateException("Mapper XML location at configured index " + index + " must not be blank");
        }

        String pattern;
        if (location.startsWith(ALL_CLASSPATH_PREFIX)) {
            pattern = location.substring(ALL_CLASSPATH_PREFIX.length());
        } else if (location.startsWith(CLASSPATH_PREFIX)) {
            pattern = location.substring(CLASSPATH_PREFIX.length());
        } else {
            throw new IllegalStateException(
                    "Mapper XML location at configured index " + index + " must use classpath: or classpath*: syntax");
        }

        while (pattern.startsWith(Symbol.SLASH)) {
            pattern = pattern.substring(1);
        }
        if (StringKit.isBlank(pattern)) {
            throw new IllegalStateException(
                    "Mapper XML location at configured index " + index + " must contain a classpath pattern");
        }
        return pattern;
    }

    /**
     * Returns whether a mapper location is a classpath pattern that may legally match no resources.
     *
     * @param location mapper resource location
     * @return {@code true} when the location contains a wildcard
     */
    private static boolean isPattern(String location) {
        return location.contains(Symbol.STAR) || location.contains(Symbol.QUESTION_MARK);
    }

    /**
     * Unified mapper location resolution result.
     *
     * @param resources resources loaded by MyBatis at JVM or native runtime
     * @param patterns  resource patterns registered during AOT processing
     */
    public record Result(Resource[] resources, Set<String> patterns) {
    }

}
