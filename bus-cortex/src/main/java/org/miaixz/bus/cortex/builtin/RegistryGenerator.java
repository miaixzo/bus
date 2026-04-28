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
package org.miaixz.bus.cortex.builtin;

import java.util.LinkedHashSet;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.RegistrySpec;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.RegistryIdentity;

/**
 * Built-in registry-side {@link Keying} implementation.
 * <p>
 * This implementation owns all default registry/runtime key semantics:
 * </p>
 * <ul>
 * <li>{@link RegistrySpec#ENTRY}: durable registry entry keys and scan prefixes</li>
 * <li>{@link RegistrySpec#INSTANCE}: runtime instance keys and scan prefixes</li>
 * <li>{@link RegistrySpec#ROUTE}: strongest runtime route key plus the ordered 8-level fallback chain</li>
 * </ul>
 * <p>
 * Route generation keeps {@code namespace}, {@code type}, and {@code appId} optional and never auto-fills them from
 * persistence defaults.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegistryGenerator implements Keying<RegistrySpec> {

    /**
     * Shared default instance.
     */
    public static final RegistryGenerator INSTANCE = new RegistryGenerator();

    /**
     * Creates the default registry key generator.
     */
    public RegistryGenerator() {

    }

    /**
     * Builds the strongest key represented by the supplied registry specification.
     *
     * @param spec registry key specification
     * @return primary registry key or {@code null}
     */
    @Override
    public String key(RegistrySpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case RegistrySpec.ENTRY -> {
                String prefix = prefix(spec);
                String id = spec.idToken();
                yield prefix == null || id == null ? null : prefix + id;
            }
            case RegistrySpec.INSTANCE -> {
                String prefix = prefix(spec);
                String fingerprint = spec.fingerprintToken();
                yield prefix == null || fingerprint == null ? null : prefix + fingerprint;
            }
            case RegistrySpec.ROUTE -> {
                List<String> keys = keys(spec);
                yield keys.isEmpty() ? null : keys.getFirst();
            }
            default -> null;
        };
    }

    /**
     * Builds the ordered candidate-key list for one route specification.
     *
     * @param spec registry key specification
     * @return ordered route candidate keys, or a singleton list for non-route specifications
     */
    @Override
    public List<String> keys(RegistrySpec spec) {
        if (spec == null) {
            return List.of();
        }
        if (spec.mode() != RegistrySpec.ROUTE) {
            return Keying.super.keys(spec);
        }
        if (!spec.routable()) {
            return List.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        String namespace = spec.namespaceToken();
        String type = spec.typeKeyToken();
        String appId = spec.appIdToken();
        String method = spec.methodToken();
        String version = spec.versionToken();
        Integer verb = spec.verbToken();
        this.appendKey(keys, namespace, type, appId, method, version, verb, true, true, true);
        this.appendKey(keys, namespace, type, appId, method, version, verb, true, true, false);
        this.appendKey(keys, namespace, type, appId, method, version, verb, true, false, true);
        this.appendKey(keys, namespace, type, appId, method, version, verb, true, false, false);
        this.appendKey(keys, namespace, type, appId, method, version, verb, false, true, true);
        this.appendKey(keys, namespace, type, appId, method, version, verb, false, true, false);
        this.appendKey(keys, namespace, type, appId, method, version, verb, false, false, true);
        this.appendKey(keys, namespace, type, appId, method, version, verb, false, false, false);
        return ListKit.of(keys);
    }

    /**
     * Builds the scan prefix for registry entry and runtime instance specifications.
     *
     * @param spec registry key specification
     * @return scan prefix or {@code null} when the mode has no prefix form
     */
    @Override
    public String prefix(RegistrySpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case RegistrySpec.ENTRY -> {
                String namespace = RegistryIdentity.namespace(spec.namespace());
                Type type = spec.typeToken();
                yield type == null ? null
                        : Builder.REG_PREFIX + namespace + Symbol.COLON + type.segment() + Symbol.COLON;
            }
            case RegistrySpec.INSTANCE -> {
                StringBuilder builder = new StringBuilder();
                builder.append(Builder.REG_PREFIX).append(RegistryIdentity.namespace(spec.namespace()))
                        .append(Symbol.COLON).append("instance").append(Symbol.COLON);
                String appId = spec.appIdToken();
                if (appId != null) {
                    builder.append(appId).append(Symbol.COLON);
                    String method = spec.methodToken();
                    if (method != null) {
                        builder.append(method).append(Symbol.COLON);
                        String version = spec.versionToken();
                        if (version != null) {
                            builder.append(version).append(Symbol.COLON);
                        }
                    }
                }
                yield builder.toString();
            }
            default -> null;
        };
    }

    /**
     * Appends one route-key candidate for a specific inclusion level.
     *
     * @param keys             route-key accumulator
     * @param namespace        normalized namespace token
     * @param type             normalized numeric type token
     * @param appId            normalized application identifier token
     * @param method           route method token
     * @param version          route version token
     * @param verb             numeric verb code
     * @param includeNamespace whether the namespace segment should be emitted
     * @param includeType      whether the type segment should be emitted
     * @param includeAppId     whether the application segment should be emitted
     */
    private void appendKey(
            LinkedHashSet<String> keys,
            String namespace,
            String type,
            String appId,
            String method,
            String version,
            Integer verb,
            boolean includeNamespace,
            boolean includeType,
            boolean includeAppId) {
        if (method == null || version == null || verb == null) {
            return;
        }
        if (includeNamespace && namespace == null) {
            return;
        }
        if (includeType && type == null) {
            return;
        }
        if (includeAppId && appId == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        if (includeNamespace) {
            builder.append(namespace).append(Symbol.COLON);
        }
        if (includeType) {
            builder.append(type).append(Symbol.COLON);
        }
        if (includeAppId) {
            builder.append(appId).append(Symbol.COLON);
        }
        builder.append(method).append(Symbol.COLON).append(version).append(Symbol.COLON).append(verb);
        keys.add(builder.toString());
    }

}
