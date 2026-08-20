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
package org.miaixz.bus.auth.protocol.scim;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the RFC 7643 ServiceProviderConfig discovery resource.
 *
 * @param schemas               singleton standard ServiceProviderConfig schema URI
 * @param documentationUri      absolute service-provider documentation URI when published
 * @param patch                 PATCH feature declaration
 * @param bulk                  Bulk feature declaration and limits
 * @param filter                filter feature declaration and result limit
 * @param changePassword        password-change feature declaration
 * @param sort                  sorting feature declaration
 * @param etag                  entity-tag feature declaration
 * @param authenticationSchemes supported authentication schemes
 * @param meta                  service-provider-maintained discovery resource metadata
 * @author Kimi Liu
 */
public record ServiceProviderConfig(List<String> schemas, Optional<String> documentationUri, Supported patch, Bulk bulk,
        FilterSupport filter, Supported changePassword, Supported sort, Supported etag,
        List<AuthenticationScheme> authenticationSchemes, Optional<Resource.Meta> meta) implements Resource {

    /**
     * Enforces the discovery schema, absolute documentation URI, and unique authentication scheme types.
     *
     * @throws IllegalArgumentException if a required value, container, or scheme is {@code null}
     * @throws ValidateException        if a URI, schema, or duplicate authentication type is invalid
     */
    public ServiceProviderConfig {
        Assert.notNull(schemas, "SCIM ServiceProviderConfig schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.SERVICE_PROVIDER_CONFIG_SCHEMA))) {
            throw new ValidateException(
                    "SCIM ServiceProviderConfig must use only the standard ServiceProviderConfig schema URI");
        }
        documentationUri = optionalAbsoluteUri(documentationUri, "SCIM documentationUri");
        patch = Assert.notNull(patch, "SCIM patch support must not be null");
        bulk = Assert.notNull(bulk, "SCIM bulk support must not be null");
        filter = Assert.notNull(filter, "SCIM filter support must not be null");
        changePassword = Assert.notNull(changePassword, "SCIM changePassword support must not be null");
        sort = Assert.notNull(sort, "SCIM sort support must not be null");
        etag = Assert.notNull(etag, "SCIM etag support must not be null");
        Assert.notNull(authenticationSchemes, "SCIM authenticationSchemes must not be null");
        final Set<String> schemeTypes = new HashSet<>(authenticationSchemes.size());
        for (AuthenticationScheme scheme : authenticationSchemes) {
            final AuthenticationScheme item = Assert.notNull(scheme, "SCIM authenticationScheme must not be null");
            if (!schemeTypes.add(item.type().toLowerCase(Locale.ROOT))) {
                throw new ValidateException("SCIM authenticationScheme types must be unique ignoring case");
            }
        }
        authenticationSchemes = List.copyOf(authenticationSchemes);
        Assert.notNull(meta, "SCIM ServiceProviderConfig meta container must not be null");
        meta = Optional.ofNullable(meta.getOrNull());
    }

    /**
     * Normalizes an optional absolute URI.
     *
     * @param value required optional URI container
     * @param label validation label
     * @return independent optional containing the valid URI text
     */
    private static Optional<String> optionalAbsoluteUri(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (value.isEmpty()) {
            return Optional.empty();
        }
        final String text = Assert.notBlank(value.getOrThrow(), label + " must not be blank");
        try {
            if (!URI.create(text).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
        return Optional.of(text);
    }

    /**
     * Declares whether one boolean SCIM feature is supported.
     *
     * @param supported whether the feature is supported
     * @author Kimi Liu
     */
    public record Supported(boolean supported) {

    }

    /**
     * Declares SCIM Bulk support and its advertised request limits.
     *
     * @param supported      whether Bulk is supported
     * @param maxOperations  maximum operations accepted in one request, or zero when unsupported
     * @param maxPayloadSize maximum request payload octets, or zero when unsupported
     * @author Kimi Liu
     */
    public record Bulk(boolean supported, int maxOperations, int maxPayloadSize) {

        /**
         * Requires positive limits for enabled Bulk and zero limits when Bulk is disabled.
         *
         * @throws ValidateException if the limits do not match the support flag
         */
        public Bulk {
            if (supported ? maxOperations <= 0 || maxPayloadSize <= 0 : maxOperations != 0 || maxPayloadSize != 0) {
                throw new ValidateException("SCIM Bulk limits must be positive when supported and zero otherwise");
            }
        }

    }

    /**
     * Declares SCIM filtering support and its advertised result limit.
     *
     * @param supported  whether filtering is supported
     * @param maxResults maximum results returned for a filtered query, or zero when unsupported
     * @author Kimi Liu
     */
    public record FilterSupport(boolean supported, int maxResults) {

        /**
         * Requires a positive limit for enabled filtering and zero when filtering is disabled.
         *
         * @throws ValidateException if the limit does not match the support flag
         */
        public FilterSupport {
            if (supported ? maxResults <= 0 : maxResults != 0) {
                throw new ValidateException(
                        "SCIM filter maxResults must be positive when supported and zero otherwise");
            }
        }

    }

    /**
     * Describes one RFC 7643 service-provider authentication scheme.
     *
     * @param type             authentication scheme type
     * @param name             human-readable authentication scheme name
     * @param description      human-readable authentication scheme description
     * @param specUri          absolute governing specification URI when supplied
     * @param documentationUri absolute implementation documentation URI when supplied
     * @author Kimi Liu
     */
    public record AuthenticationScheme(String type, String name, String description, Optional<String> specUri,
            Optional<String> documentationUri) {

        /**
         * Validates required text and optional absolute URIs.
         *
         * @throws IllegalArgumentException if a required value or optional container is {@code null}
         * @throws ValidateException        if an optional URI is invalid
         */
        public AuthenticationScheme {
            type = Assert.notBlank(type, "SCIM authenticationScheme type must not be blank");
            name = Assert.notBlank(name, "SCIM authenticationScheme name must not be blank");
            description = Assert.notBlank(description, "SCIM authenticationScheme description must not be blank");
            specUri = optionalAbsoluteUri(specUri, "SCIM authenticationScheme specUri");
            documentationUri = optionalAbsoluteUri(documentationUri, "SCIM authenticationScheme documentationUri");
        }

    }

}
