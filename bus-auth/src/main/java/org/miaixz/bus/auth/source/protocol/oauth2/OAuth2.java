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
package org.miaixz.bus.auth.source.protocol.oauth2;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.source.protocol.oauth2.server.OAuth2ServerOptions;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes direction-neutral OAuth 2.x operation keys and explicit server and client driver factories.
 *
 * @author Kimi Liu
 */
public class OAuth2 {

    /**
     * OAuth authorization endpoint operation key.
     */
    public static final Capability.Key AUTHORIZATION = Capability.Key.standard(Protocol.OAUTH2, "authorize");
    /**
     * OAuth token endpoint operation key shared by every supported grant type.
     */
    public static final Capability.Key TOKEN = Capability.Key.standard(Protocol.OAUTH2, "token");
    /**
     * OAuth token introspection endpoint operation key.
     */
    public static final Capability.Key INTROSPECTION = Capability.Key.standard(Protocol.OAUTH2, "introspect");
    /**
     * OAuth token revocation endpoint operation key.
     */
    public static final Capability.Key REVOCATION = Capability.Key.standard(Protocol.OAUTH2, "revoke");
    /**
     * OAuth device authorization endpoint operation key.
     */
    public static final Capability.Key DEVICE_AUTHORIZATION = Capability.Key
            .standard(Protocol.OAUTH2, "device_authorization");
    /**
     * OAuth Authorization Server Metadata operation key.
     */
    public static final Capability.Key AUTHORIZATION_SERVER_METADATA = Capability.Key
            .standard(Protocol.OAUTH2, "authorization_server_metadata");

    /**
     * Creates an OAuth 2.x operation constant holder with no retained state.
     */
    public OAuth2() {
        // No initialization required.
    }

    /**
     * Creates the server-side OAuth 2.x driver.
     *
     * @return new OAuth 2.x Server driver
     */
    public static SourceDriver<OAuth2ServerOptions> server() {
        return new OAuth2ServerDriver();
    }

    /**
     * Creates the client-side OAuth 2.x driver.
     *
     * @return new OAuth 2.x Client driver
     */
    public static SourceDriver<OAuth2ClientOptions> client() {
        return new OAuth2ClientDriver();
    }

    /**
     * Defines standard OAuth request and response member names used on the wire.
     *
     * @author Kimi Liu
     */
    public static class Parameters {

        /**
         * Standard access token member name.
         */
        public static final String ACCESS_TOKEN = "access_token";
        /**
         * Standard actor token parameter name.
         */
        public static final String ACTOR_TOKEN = "actor_token";
        /**
         * Standard actor token type parameter name.
         */
        public static final String ACTOR_TOKEN_TYPE = "actor_token_type";
        /**
         * Standard active introspection member name.
         */
        public static final String ACTIVE = "active";
        /**
         * Standard audience parameter name.
         */
        public static final String AUDIENCE = "audience";
        /**
         * Standard authorization code parameter name.
         */
        public static final String CODE = "code";
        /**
         * Standard PKCE code challenge parameter name.
         */
        public static final String CODE_CHALLENGE = "code_challenge";
        /**
         * Standard PKCE code challenge method parameter name.
         */
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        /**
         * Standard PKCE code verifier parameter name.
         */
        public static final String CODE_VERIFIER = "code_verifier";
        /**
         * Standard client identifier parameter name.
         */
        public static final String CLIENT_ID = "client_id";
        /**
         * Standard JWT client assertion parameter name.
         */
        public static final String CLIENT_ASSERTION = "client_assertion";
        /**
         * Standard JWT client assertion type parameter name.
         */
        public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
        /**
         * Standard RFC 7523 JWT bearer client assertion type value.
         */
        public static final String JWT_BEARER_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        /**
         * Standard client secret parameter name.
         */
        public static final String CLIENT_SECRET = "client_secret";
        /**
         * Standard device code member name.
         */
        public static final String DEVICE_CODE = "device_code";
        /**
         * Standard error code member name.
         */
        public static final String ERROR = "error";
        /**
         * Standard human-readable error description member name.
         */
        public static final String ERROR_DESCRIPTION = "error_description";
        /**
         * Standard error documentation URI member name.
         */
        public static final String ERROR_URI = "error_uri";
        /**
         * Standard token lifetime member name.
         */
        public static final String EXPIRES_IN = "expires_in";
        /**
         * Standard grant type parameter name.
         */
        public static final String GRANT_TYPE = "grant_type";
        /**
         * Standard polling interval member name.
         */
        public static final String INTERVAL = "interval";
        /**
         * RFC 9207 authorization response issuer parameter name.
         */
        public static final String ISS = "iss";
        /**
         * Standard issued token type member name.
         */
        public static final String ISSUED_TOKEN_TYPE = "issued_token_type";
        /**
         * Standard redirect URI parameter name.
         */
        public static final String REDIRECT_URI = "redirect_uri";
        /**
         * Standard refresh token member name.
         */
        public static final String REFRESH_TOKEN = "refresh_token";
        /**
         * Standard requested token type parameter name.
         */
        public static final String REQUESTED_TOKEN_TYPE = "requested_token_type";
        /**
         * Standard resource indicator parameter name.
         */
        public static final String RESOURCE = "resource";
        /**
         * Standard response type parameter name.
         */
        public static final String RESPONSE_TYPE = "response_type";
        /**
         * Standard scope parameter or member name.
         */
        public static final String SCOPE = "scope";
        /**
         * Standard authorization state parameter name.
         */
        public static final String STATE = "state";
        /**
         * Standard subject token parameter name.
         */
        public static final String SUBJECT_TOKEN = "subject_token";
        /**
         * Standard subject token type parameter name.
         */
        public static final String SUBJECT_TOKEN_TYPE = "subject_token_type";
        /**
         * Standard token parameter name used by introspection and revocation.
         */
        public static final String TOKEN = "token";
        /**
         * Standard token type hint parameter name.
         */
        public static final String TOKEN_TYPE_HINT = "token_type_hint";
        /**
         * Standard token type member name.
         */
        public static final String TOKEN_TYPE = "token_type";
        /**
         * Standard resource-owner username parameter name.
         */
        public static final String USERNAME = "username";
        /**
         * Standard device user code member name.
         */
        public static final String USER_CODE = "user_code";
        /**
         * Standard device verification URI member name.
         */
        public static final String VERIFICATION_URI = "verification_uri";
        /**
         * Standard complete device verification URI member name.
         */
        public static final String VERIFICATION_URI_COMPLETE = "verification_uri_complete";

        /**
         * Creates an OAuth request-parameter constant holder.
         */
        public Parameters() {
            // No initialization required.
        }

    }

    /**
     * Defines OAuth authorization response-mode values published by authorization-server metadata.
     *
     * @author Kimi Liu
     */
    public static class ResponseModes {

        /**
         * Query-encoded authorization response mode.
         */
        public static final String QUERY = "query";

        /**
         * Creates an OAuth response-mode constant holder.
         */
        public ResponseModes() {
            // No initialization required.
        }

    }

    /**
     * Defines standard RFC 8414 authorization-server metadata member names.
     *
     * @author Kimi Liu
     */
    public static class Metadata {

        /**
         * Authorization endpoint metadata member name.
         */
        public static final String AUTHORIZATION_ENDPOINT = "authorization_endpoint";
        /**
         * Authorization response issuer support metadata member name.
         */
        public static final String AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED = "authorization_response_iss_parameter_supported";
        /**
         * PKCE method metadata member name.
         */
        public static final String CODE_CHALLENGE_METHODS_SUPPORTED = "code_challenge_methods_supported";
        /**
         * Device authorization endpoint metadata member name.
         */
        public static final String DEVICE_AUTHORIZATION_ENDPOINT = "device_authorization_endpoint";
        /**
         * DPoP algorithm metadata member name.
         */
        public static final String DPOP_SIGNING_ALGORITHMS_SUPPORTED = "dpop_signing_alg_values_supported";
        /**
         * Grant type metadata member name.
         */
        public static final String GRANT_TYPES_SUPPORTED = "grant_types_supported";
        /**
         * Introspection endpoint metadata member name.
         */
        public static final String INTROSPECTION_ENDPOINT = "introspection_endpoint";
        /**
         * Introspection authentication method metadata member name.
         */
        public static final String INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED = "introspection_endpoint_auth_methods_supported";
        /**
         * Introspection signing algorithm metadata member name.
         */
        public static final String INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED = "introspection_endpoint_auth_signing_alg_values_supported";
        /**
         * Issuer metadata member name.
         */
        public static final String ISSUER = "issuer";
        /**
         * JSON Web Key Set URI metadata member name.
         */
        public static final String JWKS_URI = "jwks_uri";
        /**
         * Operator policy URI metadata member name.
         */
        public static final String OP_POLICY_URI = "op_policy_uri";
        /**
         * Operator terms-of-service URI metadata member name.
         */
        public static final String OP_TOS_URI = "op_tos_uri";
        /**
         * Dynamic client registration endpoint metadata member name.
         */
        public static final String REGISTRATION_ENDPOINT = "registration_endpoint";
        /**
         * Response mode metadata member name.
         */
        public static final String RESPONSE_MODES_SUPPORTED = "response_modes_supported";
        /**
         * Response type metadata member name.
         */
        public static final String RESPONSE_TYPES_SUPPORTED = "response_types_supported";
        /**
         * Revocation endpoint metadata member name.
         */
        public static final String REVOCATION_ENDPOINT = "revocation_endpoint";
        /**
         * Revocation authentication method metadata member name.
         */
        public static final String REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED = "revocation_endpoint_auth_methods_supported";
        /**
         * Revocation signing algorithm metadata member name.
         */
        public static final String REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED = "revocation_endpoint_auth_signing_alg_values_supported";
        /**
         * Scope metadata member name.
         */
        public static final String SCOPES_SUPPORTED = "scopes_supported";
        /**
         * Service documentation URI metadata member name.
         */
        public static final String SERVICE_DOCUMENTATION = "service_documentation";
        /**
         * Signed metadata member name.
         */
        public static final String SIGNED_METADATA = "signed_metadata";
        /**
         * Token endpoint metadata member name.
         */
        public static final String TOKEN_ENDPOINT = "token_endpoint";
        /**
         * Token endpoint authentication method metadata member name.
         */
        public static final String TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED = "token_endpoint_auth_methods_supported";
        /**
         * Token endpoint signing algorithm metadata member name.
         */
        public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED = "token_endpoint_auth_signing_alg_values_supported";
        /**
         * User-interface locale metadata member name.
         */
        public static final String UI_LOCALES_SUPPORTED = "ui_locales_supported";

        /**
         * Creates an OAuth metadata-name constant holder.
         */
        public Metadata() {
            // No initialization required.
        }

    }

}
