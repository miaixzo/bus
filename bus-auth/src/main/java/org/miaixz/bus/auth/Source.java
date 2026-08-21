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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tracer;

/**
 * Represents one registered authentication protocol source owned by a Provider.
 * <p>
 * Every source belongs to exactly one {@link Provider} through {@link #provider_id}. A Provider may own multiple
 * Sources, each independently selecting its adapter type, actual protocol, and typed protocol options.
 * </p>
 * <p>
 * A Source does not own a persistent {@code namespace_id}. Its resource namespace is resolved only through
 * {@code provider_id -> Provider.library_id -> Library.namespace_id}; inherited Tracer fields are transient request
 * context and are not registration ownership.
 * </p>
 * <p>
 * This mutable registration model exposes no runtime operation, protocol implementation, vendor-specific field, or
 * reverse collection. The external project remains responsible for persistence mapping and materializes the typed
 * options value before loading the registration. Runtime access is available only through the Registry.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Source extends Tracer {

    /**
     * Required non-blank identifier of the owning Provider. The referenced Provider must exist; when this Source is
     * enabled, the Provider and its Library must also be enabled. Multiple Sources may reference the same Provider.
     */
    private String provider_id;

    /**
     * Required non-blank stable Source code. The value is used by external management interfaces and must be unique
     * among Sources with the same {@link #provider_id}.
     */
    private String code;

    /**
     * Required non-blank human-readable Source name shown at management and authentication entry points.
     */
    private String name;

    /**
     * Required exact {@link Scheme} identifier used to select a registered Driver. Built-in values include
     * {@code oauth2}, {@code oidc}, {@code saml}, {@code ldap}, {@code vendor}, and server-role values such as
     * {@code oauth2-server}, {@code oidc-server}, {@code saml-server}, {@code scim-server}, {@code ldap-server}, and
     * {@code radius-server}. Custom Drivers may contribute additional stable identifiers.
     */
    private String type;

    /**
     * Optional external media reference for the Source icon. {@code null} or blank means no Source-specific icon; a
     * value may be a relative path, storage identifier, or HTTP(S) URL as interpreted by the external project.
     */
    private String icon;

    /**
     * Optional presentation order within the owning Provider. {@code null} is compared as {@code 0}; otherwise the
     * value must be zero or greater. Lower values are ordered first. Sources with the same effective sort value are
     * ordered by inherited {@code created} ascending, then by {@code id} ascending, so the earliest created Source is
     * the default.
     */
    private Integer sort;

    /**
     * Required actual protocol accepted by the selected Driver. Values are compared case-insensitively with Bus
     * protocol names such as {@code OAUTH2}, {@code OIDC}, {@code SAML}, {@code SCIM}, {@code LDAP}, {@code RADIUS}, or
     * {@code VENDOR_AUTH}; lower-case database values are recommended. The selected Driver performs the final
     * type-to-protocol compatibility check.
     */
    private String protocol;

    /**
     * Required immutable options value already materialized by the external project integration boundary. The Driver
     * selected by {@link #type} validates and consumes the concrete value. Secret material must be represented by
     * external {@link Credential.Reference} values and must never be embedded as plaintext.
     */
    private Options<?> options;

    /**
     * Optional JSON object encoded as text for external management extensions. {@code null} or blank means no metadata;
     * its members must not override {@link #type}, {@link #protocol}, options, authorization, or security decisions.
     */
    private String metadata;

    /**
     * Optional human-readable Source description. {@code null} means that no description is supplied; the value is
     * presentation-only and must not be interpreted as protocol configuration.
     */
    private String description;

    /**
     * Creates an empty registration model for an external project integration.
     */
    public Source() {
        // No initialization required.
    }

}
