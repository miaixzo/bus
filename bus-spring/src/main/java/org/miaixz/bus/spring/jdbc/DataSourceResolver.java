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
package org.miaixz.bus.spring.jdbc;

import java.util.*;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.boot.environment.EnvironmentSnapshotPropertySource;

/**
 * Resolves the first configured datasource namespace from an ordered prefix list without creating connection pools.
 * <p>
 * The selected root entry becomes the primary route, while its {@code multi} list contributes additional uniquely named
 * routes. Prefix ordering and default pool selection remain responsibilities of the consuming integration.
 *
 * @author Kimi Liu
 */
public class DataSourceResolver {

    /**
     * Spring environment containing compatible datasource namespaces.
     */
    private final Environment environment;

    /**
     * Datasource prefixes in descending priority order.
     */
    private final List<String> prefixes;

    /**
     * Creates a resolver for ordered compatible datasource prefixes.
     *
     * @param environment current Spring environment
     * @param prefixes    datasource prefixes in descending priority order
     * @throws IllegalArgumentException if the environment is absent or the prefixes are empty, blank, or repeated
     */
    public DataSourceResolver(Environment environment, List<String> prefixes) {
        if (environment == null) {
            throw new IllegalArgumentException("Spring environment is required");
        }
        if (prefixes == null || prefixes.isEmpty()) {
            throw new IllegalArgumentException("At least one datasource prefix is required");
        }
        List<String> normalized = new ArrayList<>(prefixes.size());
        for (String prefix : prefixes) {
            String value = StringKit.trim(prefix);
            if (StringKit.isEmpty(value) || normalized.contains(value)) {
                throw new IllegalArgumentException("Datasource prefixes must be nonblank and unique");
            }
            normalized.add(value);
        }
        this.environment = environment;
        this.prefixes = List.copyOf(normalized);
    }

    /**
     * Resolves the root and additional entries under one selected prefix.
     *
     * @param binder             binder for the selected property source or environment
     * @param prefix             selected datasource prefix
     * @param literalCredentials whether credentials from the selected source must retain surrounding whitespace
     * @return validated datasource mapping
     * @throws IllegalStateException if multiple entries resolve to the same datasource name
     */
    private static DataSourceMapping resolve(Binder binder, String prefix, boolean literalCredentials) {
        String primary = bindString(binder, property(prefix, "name"));
        if (StringKit.isEmpty(primary)) {
            primary = Normal.DEFAULT;
        }
        Map<String, Object> hikari = binder.bind(property(prefix, "hikari"), Bindable.mapOf(String.class, Object.class))
                .map(HashMap::new).orElseGet(HashMap::new);
        String username = credential(binder, property(prefix, "username"), literalCredentials);
        String password = credential(binder, property(prefix, "password"), literalCredentials);
        DataSourceDefinition root = new DataSourceDefinition(primary, bindString(binder, property(prefix, "url")),
                username, password, bindString(binder, property(prefix, "driver-class-name")),
                bindString(binder, property(prefix, "type")), hikari);

        LinkedHashMap<String, DataSourceDefinition> sources = new LinkedHashMap<>();
        sources.put(root.getName(), root);
        List<DataSourceDefinition> additional = binder
                .bind(property(prefix, "multi"), Bindable.listOf(DataSourceDefinition.class)).orElse(List.of());
        for (DataSourceDefinition definition : additional) {
            definition.validate();
            if (sources.putIfAbsent(definition.getName(), definition) != null) {
                throw new IllegalStateException(
                        "Duplicate datasource name under " + prefix + ": " + definition.getName());
            }
        }
        DataSourceMapping mapping = new DataSourceMapping(primary, sources);
        Logger.info(
                true,
                "Spring",
                "JDBC configuration resolved: source={}, primary={}, datasourceCount={}",
                prefix,
                mapping.getPrimary(),
                mapping.getSources().size());
        return mapping;
    }

    /**
     * Returns whether a datasource namespace declares a root JDBC URL.
     *
     * @param binder property binder for the current environment
     * @param prefix datasource configuration prefix
     * @return {@code true} when a standard or Hikari-specific URL is present
     */
    private static boolean hasUrl(Binder binder, String prefix) {
        return StringKit.isNotEmpty(bindString(binder, property(prefix, "url")))
                || StringKit.isNotEmpty(bindString(binder, property(prefix, "hikari.jdbc-url")));
    }

    /**
     * Binds and trims one optional string property.
     *
     * @param binder       Spring Boot property binder
     * @param propertyName complete property name
     * @return trimmed property value, or {@code null} when absent
     */
    private static String bindString(Binder binder, String propertyName) {
        return StringKit.trim(binder.bind(propertyName, String.class).orElse(null));
    }

    /**
     * Binds credentials while preserving whitespace for literal and programmatic values.
     * <p>
     * Installation sources can use a non-{@link String} value to bypass placeholder expansion.
     *
     * @param binder   binder for the selected source or environment
     * @param property complete credential property name
     * @param literal  whether to preserve whitespace in string values
     * @return bound credential, or {@code null} when absent
     */
    private static String credential(Binder binder, String property, boolean literal) {
        CharSequence value = binder.bind(property, CharSequence.class).orElse(null);
        if (value == null) {
            return null;
        }
        return literal || !(value instanceof String) ? value.toString() : StringKit.trim(value.toString());
    }

    /**
     * Builds one complete property name.
     *
     * @param prefix selected datasource prefix
     * @param name   relative property name
     * @return complete property name
     */
    private static String property(String prefix, String name) {
        return prefix + Symbol.DOT + name;
    }

    /**
     * Resolves the first configured complete datasource mapping.
     * <p>
     * If a startup snapshot supplies a datasource prefix, the mapping is bound from that snapshot alone.
     *
     * @return validated datasource mapping
     * @throws IllegalStateException if no datasource URL is present or a datasource name is repeated
     */
    public DataSourceMapping resolve() {
        Binder snapshot = EnvironmentSnapshotPropertySource.isolatedBinder(environment, prefixes);
        return snapshot == null ? resolve(Binder.get(environment), false) : resolve(snapshot, true);
    }

    /**
     * Selects the first configured datasource namespace in prefix order.
     *
     * @param binder             binder to use for the entire mapping
     * @param literalCredentials whether credentials should retain surrounding whitespace
     * @return validated datasource mapping
     * @throws IllegalStateException if no datasource URL is present or a datasource name is repeated
     */
    private DataSourceMapping resolve(Binder binder, boolean literalCredentials) {
        for (String prefix : this.prefixes) {
            if (hasUrl(binder, prefix)) {
                return resolve(binder, prefix, literalCredentials);
            }
        }
        throw new IllegalStateException("JDBC requires a datasource URL under one of: " + this.prefixes);
    }

}
