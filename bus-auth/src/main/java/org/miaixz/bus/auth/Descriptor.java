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
package org.miaixz.bus.auth;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Options;

/**
 * Stable immutable description of an authentication component.
 *
 * @param id           stable provider identifier
 * @param name         human-readable provider name
 * @param protocol     primary wire protocol
 * @param capabilities immutable supported capability set
 * @param endpoints    immutable endpoints indexed by declared capability
 * @param options      immutable provider metadata
 * @author Kimi Liu
 */
public record Descriptor(String id, String name, Protocol protocol, Set<Capability> capabilities,
        Map<Capability, Endpoint> endpoints, Options options) {

    /**
     * Validates identity and protocol fields and snapshots all containers.
     *
     * @throws ValidateException if identity fields are blank, protocol is null, or an endpoint is undeclared
     */
    public Descriptor {
        id = required(id, "Descriptor identifier");
        name = required(name, "Descriptor name");
        if (protocol == null) {
            throw new ValidateException("Descriptor protocol must not be null");
        }
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
        if (!capabilities.containsAll(endpoints.keySet())) {
            throw new ValidateException("Every endpoint must declare its capability");
        }
        options = options == null ? Options.empty() : options;
    }

    /**
     * Validates and trims required descriptor text.
     *
     * @param value text to validate
     * @param label field label used in failures
     * @return trimmed text
     * @throws ValidateException if the text is null or blank
     */
    private static String required(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        return value.trim();
    }

    /**
     * Tests whether this component declares a capability.
     *
     * @param capability capability to test
     * @return true when supported
     */
    public boolean supports(final Capability capability) {
        return capabilities.contains(capability);
    }

    /**
     * Looks up an endpoint for a declared capability.
     *
     * @param capability endpoint capability
     * @return optional endpoint
     */
    public Optional<Endpoint> endpoint(final Capability capability) {
        return Optional.ofNullable(endpoints.get(capability));
    }

    /**
     * Returns the endpoint required for a capability.
     *
     * @param capability endpoint capability
     * @return configured endpoint
     * @throws ValidateException if no endpoint is configured
     */
    public Endpoint requireEndpoint(final Capability capability) {
        return endpoint(capability).orElseThrow(() -> new ValidateException("Missing endpoint: " + capability.name()));
    }

}
