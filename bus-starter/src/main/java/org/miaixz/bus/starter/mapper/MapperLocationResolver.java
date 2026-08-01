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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

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
final class MapperLocationResolver {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final String ALL_CLASSPATH_PREFIX = "classpath*:";

    private MapperLocationResolver() {
    }

    /**
     * Resolves all configured mapper locations and normalizes their native-image resource patterns.
     *
     * @param properties mapper configuration properties
     * @param resolver   Spring resource pattern resolver
     * @return resolved resources and normalized classpath patterns
     * @throws IllegalStateException when a location is unsupported, invalid or resolves to no resources
     */
    static Result resolve(MapperProperties properties, ResourcePatternResolver resolver) {
        String[] locations = properties.getMapperLocations();
        if (locations == null || locations.length == 0) {
            return new Result(new Resource[0], Set.of());
        }

        List<Resource> resources = new ArrayList<>();
        Set<String> patterns = new LinkedHashSet<>();
        for (String location : locations) {
            String pattern = normalize(location);
            Resource[] resolved;
            try {
                resolved = resolver.getResources(location);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to resolve mapper XML resources: " + location, e);
            }
            if (resolved.length == 0) {
                throw new IllegalStateException("No mapper XML resources found: " + location);
            }
            patterns.add(pattern);
            resources.addAll(Arrays.asList(resolved));
        }

        Resource[] distinctResources = new LinkedHashSet<>(resources).toArray(Resource[]::new);
        return new Result(distinctResources, Set.copyOf(patterns));
    }

    private static String normalize(String location) {
        if (StringKit.isBlank(location)) {
            throw new IllegalStateException("Mapper XML location must not be blank");
        }

        String pattern;
        if (location.startsWith(ALL_CLASSPATH_PREFIX)) {
            pattern = location.substring(ALL_CLASSPATH_PREFIX.length());
        } else if (location.startsWith(CLASSPATH_PREFIX)) {
            pattern = location.substring(CLASSPATH_PREFIX.length());
        } else {
            throw new IllegalStateException("Native Image only supports classpath mapper locations: " + location);
        }

        while (pattern.startsWith("/")) {
            pattern = pattern.substring(1);
        }
        if (StringKit.isBlank(pattern)) {
            throw new IllegalStateException("Mapper XML location must contain a classpath pattern: " + location);
        }
        return pattern;
    }

    /**
     * Unified mapper location resolution result.
     *
     * @param resources resources loaded by MyBatis at JVM or native runtime
     * @param patterns  resource patterns registered during AOT processing
     */
    record Result(Resource[] resources, Set<String> patterns) {
    }

}
