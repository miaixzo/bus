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

import java.time.Duration;

import org.miaixz.bus.fabric.Options;

/**
 * Catalog of shared authentication constants and typed option keys.
 *
 * @author Kimi Liu
 */
public final class Builder {

    /**
     * Directory that stores protocol-neutral providers.
     */
    public static final String DIRECTORY_PROVIDERS = "providers";

    /**
     * Directory that stores server protocol handlers.
     */
    public static final String DIRECTORY_PROTOCOLS = "protocols";

    /**
     * Directory that stores third-party authentication clients.
     */
    public static final String DIRECTORY_VENDORS = "vendors";

    /**
     * Directory that stores authentication policies.
     */
    public static final String DIRECTORY_POLICIES = "policies";

    /**
     * Capability for initiating an authorization flow.
     */
    public static final Capability CAPABILITY_AUTHORIZE = Capability.of("authorize");

    /**
     * Capability for consuming an authorization callback.
     */
    public static final Capability CAPABILITY_CALLBACK = Capability.of("callback");

    /**
     * Capability for authenticating credentials.
     */
    public static final Capability CAPABILITY_AUTHENTICATE = Capability.of("authenticate");

    /**
     * Capability for issuing or exchanging a token.
     */
    public static final Capability CAPABILITY_TOKEN = Capability.of("token");

    /**
     * Capability for refreshing a token.
     */
    public static final Capability CAPABILITY_REFRESH = Capability.of("refresh");

    /**
     * Capability for revoking a token.
     */
    public static final Capability CAPABILITY_REVOKE = Capability.of("revoke");

    /**
     * Capability for introspecting a token.
     */
    public static final Capability CAPABILITY_INTROSPECT = Capability.of("introspect");

    /**
     * Capability for resolving user information.
     */
    public static final Capability CAPABILITY_USERINFO = Capability.of("userinfo");

    /**
     * Capability for publishing or reading discovery metadata.
     */
    public static final Capability CAPABILITY_DISCOVERY = Capability.of("discovery");

    /**
     * Capability for provisioning an identity resource.
     */
    public static final Capability CAPABILITY_PROVISION = Capability.of("provision");

    /**
     * Typed option controlling accepted clock skew.
     */
    public static final Options.Key<Duration> OPTION_CLOCK_SKEW = Options
            .key("auth.security.clock_skew", Duration.class);

    /**
     * Typed option controlling one-time authorization state lifetime.
     */
    public static final Options.Key<Duration> OPTION_STATE_TTL = Options.key("auth.state.ttl", Duration.class);

    /**
     * Typed option requiring callback state validation.
     */
    public static final Options.Key<Boolean> OPTION_STATE_REQUIRED = Options.key("auth.state.required", Boolean.class);

    /**
     * Typed option limiting callback parameter count.
     */
    public static final Options.Key<Integer> OPTION_CALLBACK_MAX_PARAMETERS = Options
            .key("auth.callback.max_parameters", Integer.class);

    /**
     * Typed option limiting each callback parameter in UTF-8 bytes.
     */
    public static final Options.Key<Integer> OPTION_CALLBACK_MAX_PARAMETER_BYTES = Options
            .key("auth.callback.max_parameter_bytes", Integer.class);

    /**
     * Typed option limiting decoded authentication header bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_HEADER_BYTES = Options
            .key("auth.limit.max_header_bytes", Integer.class);

    /**
     * Typed option limiting decoded callback or form parameter count.
     */
    public static final Options.Key<Integer> OPTION_MAX_PARAMETERS = Options
            .key("auth.limit.max_parameters", Integer.class);

    /**
     * Typed option limiting one decoded callback or form parameter in UTF-8 bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_PARAMETER_BYTES = Options
            .key("auth.limit.max_parameter_bytes", Integer.class);

    /**
     * Typed option limiting decoded authentication JSON bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_JSON_BYTES = Options
            .key("auth.limit.max_json_bytes", Integer.class);

    /**
     * Typed option limiting JSON object or array nesting depth.
     */
    public static final Options.Key<Integer> OPTION_MAX_JSON_DEPTH = Options
            .key("auth.limit.max_json_depth", Integer.class);

    /**
     * Typed option limiting encoded token bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_TOKEN_BYTES = Options
            .key("auth.limit.max_token_bytes", Integer.class);

    /**
     * Typed option limiting one LDAP message in bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_LDAP_MESSAGE_BYTES = Options
            .key("auth.limit.max_ldap_message_bytes", Integer.class);

    /**
     * Typed option limiting LDAP BER nesting depth.
     */
    public static final Options.Key<Integer> OPTION_MAX_LDAP_DEPTH = Options
            .key("auth.limit.max_ldap_depth", Integer.class);

    /**
     * Typed option limiting one SCIM bulk document in bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_SCIM_BULK_BYTES = Options
            .key("auth.limit.max_scim_bulk_bytes", Integer.class);

    /**
     * Typed option limiting operations in one SCIM bulk request.
     */
    public static final Options.Key<Integer> OPTION_MAX_SCIM_BULK_OPERATIONS = Options
            .key("auth.limit.max_scim_bulk_operations", Integer.class);

    /**
     * Typed option limiting one RADIUS datagram in bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_RADIUS_PACKET_BYTES = Options
            .key("auth.limit.max_radius_packet_bytes", Integer.class);

    /**
     * Typed option limiting one SSF security event token in bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_SSF_SET_BYTES = Options
            .key("auth.limit.max_ssf_set_bytes", Integer.class);

    /**
     * Typed option limiting buffered remote response bytes.
     */
    public static final Options.Key<Integer> OPTION_MAX_RESPONSE_BYTES = Options
            .key("auth.limit.max_response_bytes", Integer.class);

    /**
     * Typed option limiting redirects followed by an authentication client.
     */
    public static final Options.Key<Integer> OPTION_REDIRECT_LIMIT = Options
            .key("auth.transport.redirect_limit", Integer.class);

    /**
     * Prevents construction of this constant catalog.
     */
    private Builder() {
        // No initialization required.
    }

}
