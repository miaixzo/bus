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
package org.miaixz.bus.spring.boot.environment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

/**
 * An immutable startup property source with isolated binding for compound values.
 * <p>
 * A consumer that binds several related fields can choose the whole snapshot and preserve literal credentials.
 *
 * @author Kimi Liu
 */
public class EnvironmentSnapshotPropertySource extends MapPropertySource {

    /**
     * Canonical Spring Boot names supplied by the snapshot.
     */
    private final Set<ConfigurationPropertyName> names;

    /**
     * Creates an immutable copy of the startup properties and their canonical names.
     *
     * @param name   property source name
     * @param values flattened startup properties
     * @param names  canonical names corresponding to the properties
     */
    protected EnvironmentSnapshotPropertySource(String name, Map<String, Object> values,
            Set<ConfigurationPropertyName> names) {
        super(name, Map.copyOf(values));
        this.names = Set.copyOf(names);
    }

    /**
     * Returns whether the snapshot declares this exact property under Boot's relaxed naming rules.
     *
     * @param name canonical property name
     * @return {@code true} when the snapshot declares the property
     */
    public final boolean declares(ConfigurationPropertyName name) {
        return names.contains(name);
    }

    /**
     * Returns whether the snapshot supplies the binding root or one of its descendants.
     *
     * @param root canonical binding root
     * @return {@code true} when the nonempty root or a descendant is present
     */
    public final boolean owns(ConfigurationPropertyName root) {
        return !root.isEmpty() && names.stream().anyMatch(name -> name.equals(root) || root.isAncestorOf(name));
    }

    /**
     * Selects the first snapshot in environment order that supplies any of the binding roots.
     * <p>
     * The returned binder reads only that snapshot and preserves placeholder syntax as literal text.
     *
     * @param environment Spring environment that contains the startup property sources
     * @param roots       canonical property roots used to bind the object
     * @return an isolated binder, or {@code null} when no snapshot matches
     */
    public static Binder isolatedBinder(Environment environment, List<String> roots) {
        if (environment instanceof ConfigurableEnvironment configurable) {
            var names = roots.stream().map(ConfigurationPropertyName::of).toList();
            for (var source : configurable.getPropertySources()) {
                if (source instanceof EnvironmentSnapshotPropertySource snapshot
                        && names.stream().anyMatch(snapshot::owns)) {
                    return new Binder(List.of(new MapConfigurationPropertySource(snapshot.getSource())),
                            PlaceholdersResolver.NONE);
                }
            }
        }
        return null;
    }

}
