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
package org.miaixz.bus.auth.source.protocol.oidc;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientOptions;
import org.miaixz.bus.auth.source.protocol.oidc.server.OpenIdServerOptions;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes direction-neutral OpenID Connect operation keys and explicit server and client driver factories.
 *
 * @author Kimi Liu
 */
public class OpenIdConnect {

    /**
     * Complete OpenID Connect authentication operation composed over OAuth 2.x authorization and token exchange.
     */
    public static final Capability.Key AUTHENTICATION = Capability.Key.standard(Protocol.OIDC, "authentication");
    /**
     * OpenID Connect UserInfo operation.
     */
    public static final Capability.Key USERINFO = Capability.Key.standard(Protocol.OIDC, "userinfo");
    /**
     * OpenID Provider Discovery operation.
     */
    public static final Capability.Key DISCOVERY = Capability.Key.standard(Protocol.OIDC, "discovery");
    /**
     * OpenID Provider public JWK Set retrieval operation.
     */
    public static final Capability.Key JWK_SET = Capability.Key.standard(Protocol.OIDC, "jwks");
    /**
     * OpenID Connect RP-Initiated Logout operation.
     */
    public static final Capability.Key END_SESSION = Capability.Key.standard(Protocol.OIDC, "end_session");

    /**
     * Creates an OpenID Connect operation constant holder with no retained state.
     */
    public OpenIdConnect() {
        // No initialization required.
    }

    /**
     * Creates the server-side OpenID Connect driver.
     *
     * @return new OpenID Server driver
     */
    public static SourceDriver<OpenIdServerOptions> server() {
        return new OpenIdServerDriver();
    }

    /**
     * Creates the client-side OpenID Connect driver.
     *
     * @return new OpenID Client driver
     */
    public static SourceDriver<OpenIdClientOptions> client() {
        return new OpenIdClientDriver();
    }

    /**
     * Defines OpenID Connect authentication and logout request parameter names.
     *
     * @author Kimi Liu
     */
    public static class Parameters {

        /**
         * Requested authentication context class reference parameter name.
         */
        public static final String ACR_VALUES = "acr_values";
        /**
         * Requested claims parameter name.
         */
        public static final String CLAIMS = "claims";
        /**
         * Authentication display mode parameter name.
         */
        public static final String DISPLAY = "display";
        /**
         * ID Token hint parameter name.
         */
        public static final String ID_TOKEN_HINT = "id_token_hint";
        /**
         * ID Token member name used by token and authentication responses.
         */
        public static final String ID_TOKEN = "id_token";
        /**
         * Login hint parameter name.
         */
        public static final String LOGIN_HINT = "login_hint";
        /**
         * Logout hint parameter name.
         */
        public static final String LOGOUT_HINT = "logout_hint";
        /**
         * Maximum authentication age parameter name.
         */
        public static final String MAX_AGE = "max_age";
        /**
         * Authentication nonce parameter name.
         */
        public static final String NONCE = "nonce";
        /**
         * Post-logout redirect URI parameter name.
         */
        public static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
        /**
         * Authentication prompt parameter name.
         */
        public static final String PROMPT = "prompt";
        /**
         * Request Object parameter name.
         */
        public static final String REQUEST = "request";
        /**
         * Request Object URI parameter name.
         */
        public static final String REQUEST_URI = "request_uri";
        /**
         * Authorization response mode parameter name.
         */
        public static final String RESPONSE_MODE = "response_mode";
        /**
         * OpenID Provider session-state response parameter name.
         */
        public static final String SESSION_STATE = "session_state";
        /**
         * User-interface locale parameter name.
         */
        public static final String UI_LOCALES = "ui_locales";

        /**
         * Creates an OpenID Connect request-parameter constant holder.
         */
        public Parameters() {
            // No initialization required.
        }

    }

    /**
     * Defines OpenID Connect ID Token and UserInfo claim names beyond the registered JWT claims.
     *
     * @author Kimi Liu
     */
    public static class Claims {

        /**
         * Authentication context class reference claim name.
         */
        public static final String ACR = "acr";
        /**
         * Authentication methods references claim name.
         */
        public static final String AMR = "amr";
        /**
         * Authentication time claim name.
         */
        public static final String AUTH_TIME = "auth_time";
        /**
         * Access token hash claim name.
         */
        public static final String ACCESS_TOKEN_HASH = "at_hash";
        /**
         * Authorized party claim name.
         */
        public static final String AUTHORIZED_PARTY = "azp";
        /**
         * Authorization code hash claim name.
         */
        public static final String CODE_HASH = "c_hash";
        /**
         * Authentication nonce claim name.
         */
        public static final String NONCE = "nonce";
        /**
         * Session identifier claim name.
         */
        public static final String SESSION_ID = "sid";
        /**
         * Authorization state hash claim name.
         */
        public static final String STATE_HASH = "s_hash";
        /**
         * Full display name claim name.
         */
        public static final String NAME = "name";
        /**
         * Given name claim name.
         */
        public static final String GIVEN_NAME = "given_name";
        /**
         * Family name claim name.
         */
        public static final String FAMILY_NAME = "family_name";
        /**
         * Middle name claim name.
         */
        public static final String MIDDLE_NAME = "middle_name";
        /**
         * Nickname claim name.
         */
        public static final String NICKNAME = "nickname";
        /**
         * Preferred username claim name.
         */
        public static final String PREFERRED_USERNAME = "preferred_username";
        /**
         * Profile page URL claim name.
         */
        public static final String PROFILE = "profile";
        /**
         * Profile picture URL claim name.
         */
        public static final String PICTURE = "picture";
        /**
         * Website URL claim name.
         */
        public static final String WEBSITE = "website";
        /**
         * Email address claim name.
         */
        public static final String EMAIL = "email";
        /**
         * Email verification claim name.
         */
        public static final String EMAIL_VERIFIED = "email_verified";
        /**
         * Gender claim name.
         */
        public static final String GENDER = "gender";
        /**
         * Birthdate claim name.
         */
        public static final String BIRTHDATE = "birthdate";
        /**
         * Time-zone database name claim.
         */
        public static final String ZONE_INFO = "zoneinfo";
        /**
         * Locale claim name.
         */
        public static final String LOCALE = "locale";
        /**
         * Telephone number claim name.
         */
        public static final String PHONE_NUMBER = "phone_number";
        /**
         * Telephone verification claim name.
         */
        public static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";
        /**
         * Structured address claim name.
         */
        public static final String ADDRESS = "address";
        /**
         * Profile update NumericDate claim name.
         */
        public static final String UPDATED_AT = "updated_at";
        /**
         * Formatted postal address member name.
         */
        public static final String FORMATTED = "formatted";
        /**
         * Street address member name.
         */
        public static final String STREET_ADDRESS = "street_address";
        /**
         * Address locality member name.
         */
        public static final String LOCALITY = "locality";
        /**
         * Address region member name.
         */
        public static final String REGION = "region";
        /**
         * Postal code member name.
         */
        public static final String POSTAL_CODE = "postal_code";
        /**
         * Address country member name.
         */
        public static final String COUNTRY = "country";
        /**
         * UserInfo target member used inside an OpenID Connect claims request object.
         */
        public static final String USERINFO = "userinfo";
        /**
         * Essential individual-claim request member name.
         */
        public static final String ESSENTIAL = "essential";
        /**
         * Exact individual-claim request value member name.
         */
        public static final String VALUE = "value";
        /**
         * Accepted individual-claim request values member name.
         */
        public static final String VALUES = "values";

        /**
         * Creates an OpenID Connect claim-name constant holder.
         */
        public Claims() {
            // No initialization required.
        }

    }

    /**
     * Defines OpenID Connect scope values introduced beyond OAuth 2.x.
     *
     * @author Kimi Liu
     */
    public static class Scopes {

        /**
         * Mandatory OpenID Connect authentication scope value.
         */
        public static final String OPENID = "openid";
        /**
         * Standard profile claim-release scope value.
         */
        public static final String PROFILE = "profile";
        /**
         * Standard email claim-release scope value.
         */
        public static final String EMAIL = "email";
        /**
         * Standard address claim-release scope value.
         */
        public static final String ADDRESS = "address";
        /**
         * Standard telephone claim-release scope value.
         */
        public static final String PHONE = "phone";

        /**
         * Creates an OpenID Connect scope constant holder.
         */
        public Scopes() {
            // No initialization required.
        }

    }

    /**
     * Defines response mode values used by OpenID Connect authorization responses.
     *
     * @author Kimi Liu
     */
    public static class ResponseModes {

        /**
         * Query-encoded authorization response mode.
         */
        public static final String QUERY = "query";
        /**
         * Fragment-encoded authorization response mode.
         */
        public static final String FRAGMENT = "fragment";
        /**
         * Auto-submitted HTML form authorization response mode.
         */
        public static final String FORM_POST = "form_post";

        /**
         * Creates an OpenID Connect response-mode constant holder.
         */
        public ResponseModes() {
            // No initialization required.
        }

    }

    /**
     * Defines OpenID Provider metadata member names introduced by OpenID Connect Discovery.
     *
     * @author Kimi Liu
     */
    public static class Metadata {

        /**
         * Authentication context support metadata member name.
         */
        public static final String ACR_VALUES_SUPPORTED = "acr_values_supported";
        /**
         * Claim type support metadata member name.
         */
        public static final String CLAIM_TYPES_SUPPORTED = "claim_types_supported";
        /**
         * Claim locale support metadata member name.
         */
        public static final String CLAIMS_LOCALES_SUPPORTED = "claims_locales_supported";
        /**
         * Claims parameter support metadata member name.
         */
        public static final String CLAIMS_PARAMETER_SUPPORTED = "claims_parameter_supported";
        /**
         * Supported claim names metadata member name.
         */
        public static final String CLAIMS_SUPPORTED = "claims_supported";
        /**
         * Display mode support metadata member name.
         */
        public static final String DISPLAY_VALUES_SUPPORTED = "display_values_supported";
        /**
         * End-session endpoint metadata member name.
         */
        public static final String END_SESSION_ENDPOINT = "end_session_endpoint";
        /**
         * ID Token encryption algorithm metadata member name.
         */
        public static final String ID_TOKEN_ENCRYPTION_ALGORITHMS_SUPPORTED = "id_token_encryption_alg_values_supported";
        /**
         * ID Token encryption method metadata member name.
         */
        public static final String ID_TOKEN_ENCRYPTION_METHODS_SUPPORTED = "id_token_encryption_enc_values_supported";
        /**
         * ID Token signing algorithm metadata member name.
         */
        public static final String ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED = "id_token_signing_alg_values_supported";
        /**
         * Request Object parameter support metadata member name.
         */
        public static final String REQUEST_PARAMETER_SUPPORTED = "request_parameter_supported";
        /**
         * Request URI registration requirement metadata member name.
         */
        public static final String REQUIRE_REQUEST_URI_REGISTRATION = "require_request_uri_registration";
        /**
         * Request URI parameter support metadata member name.
         */
        public static final String REQUEST_URI_PARAMETER_SUPPORTED = "request_uri_parameter_supported";
        /**
         * Subject identifier type metadata member name.
         */
        public static final String SUBJECT_TYPES_SUPPORTED = "subject_types_supported";
        /**
         * UserInfo endpoint metadata member name.
         */
        public static final String USERINFO_ENDPOINT = "userinfo_endpoint";
        /**
         * UserInfo encryption algorithm metadata member name.
         */
        public static final String USERINFO_ENCRYPTION_ALGORITHMS_SUPPORTED = "userinfo_encryption_alg_values_supported";
        /**
         * UserInfo encryption method metadata member name.
         */
        public static final String USERINFO_ENCRYPTION_METHODS_SUPPORTED = "userinfo_encryption_enc_values_supported";
        /**
         * UserInfo signing algorithm metadata member name.
         */
        public static final String USERINFO_SIGNING_ALGORITHMS_SUPPORTED = "userinfo_signing_alg_values_supported";

        /**
         * Creates an OpenID Provider metadata-name constant holder.
         */
        public Metadata() {
            // No initialization required.
        }

    }

}
