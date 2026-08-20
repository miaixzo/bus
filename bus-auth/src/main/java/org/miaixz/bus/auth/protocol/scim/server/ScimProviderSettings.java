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
package org.miaixz.bus.auth.protocol.scim.server;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.protocol.scim.ResourceType;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.auth.protocol.scim.ServiceProviderConfig;
import org.miaixz.bus.auth.provider.ProviderSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Associates one SCIM Provider with its typed standard discovery objects and implementation safety limits.
 *
 * @param baseUri               absolute credential-free HTTPS service base URI
 * @param serviceProviderConfig standard ServiceProviderConfig exposed by discovery
 * @param resourceTypes         non-empty registered ResourceType discovery objects
 * @param maximumRequestBytes   positive maximum request body octets
 * @param maximumJsonDepth      positive maximum JSON nesting depth
 * @author Kimi Liu
 */
public record ScimProviderSettings(URI baseUri, ServiceProviderConfig serviceProviderConfig,
        List<ResourceType> resourceTypes, long maximumRequestBytes, int maximumJsonDepth) implements ProviderSettings {

    /**
     * Validates the secure service URI, typed discovery associations, and safety limits.
     *
     * @throws IllegalArgumentException if a component or resource type is {@code null}
     * @throws ValidateException        if URI, discovery, resource, or limit invariants are invalid
     */
    public ScimProviderSettings {
        baseUri = secureBaseUri(baseUri);
        serviceProviderConfig = Assert.notNull(serviceProviderConfig, "SCIM ServiceProviderConfig must not be null");
        Assert.notNull(resourceTypes, "SCIM Provider ResourceTypes must not be null");
        if (resourceTypes.isEmpty()) {
            throw new ValidateException("SCIM Provider ResourceTypes must not be empty");
        }
        final Set<String> names = new HashSet<>();
        final Set<String> endpoints = new HashSet<>();
        final Set<String> schemas = new HashSet<>();
        for (ResourceType resourceType : resourceTypes) {
            final ResourceType value = Assert.notNull(resourceType, "SCIM Provider ResourceType must not be null");
            if (!names.add(value.name()) || !endpoints.add(value.endpoint()) || !schemas.add(value.schema())) {
                throw new ValidateException("SCIM Provider ResourceType names, endpoints, and schemas must be unique");
            }
            if (!Scim.USER_SCHEMA.equals(value.schema()) && !Scim.GROUP_SCHEMA.equals(value.schema())) {
                throw new ValidateException("SCIM Provider currently supports only core User and Group resources");
            }
        }
        resourceTypes = List.copyOf(resourceTypes);
        if (maximumRequestBytes <= 0 || maximumJsonDepth <= 0) {
            throw new ValidateException("SCIM Provider safety limits must be positive");
        }
        final ServiceProviderConfig.Bulk bulk = serviceProviderConfig.bulk();
        if (bulk.supported() && bulk.maxPayloadSize() > maximumRequestBytes) {
            throw new ValidateException("SCIM Bulk payload limit must not exceed the request body limit");
        }
    }

    /**
     * Validates one secure credential-free service base URI.
     *
     * @param value candidate URI
     * @return unchanged valid URI
     */
    private static URI secureBaseUri(final URI value) {
        final URI uri = Assert.notNull(value, "SCIM Provider base URI must not be null");
        if (!uri.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new ValidateException(
                    "SCIM Provider base URI must be absolute credential-free HTTPS without query or fragment");
        }
        return uri;
    }

    /**
     * Tests whether one standard resource type name is registered.
     *
     * @param name exact ResourceType name
     * @return whether the resource type is registered
     */
    public boolean supports(final String name) {
        return resourceTypes.stream().anyMatch(resourceType -> resourceType.name().equals(name));
    }

    /**
     * Tests whether one standard core resource schema is registered.
     *
     * @param schema exact core schema URI
     * @return whether the schema is registered
     */
    public boolean supportsSchema(final String schema) {
        return resourceTypes.stream().anyMatch(resourceType -> resourceType.schema().equals(schema));
    }

}
