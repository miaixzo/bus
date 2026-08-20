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
package org.miaixz.bus.auth.protocol.oauth1;

import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1ClientSettings;
import org.miaixz.bus.auth.protocol.oauth1.internal.OAuth1SourceDriver;
import org.miaixz.bus.auth.source.SourceDriver;

/**
 * Exposes RFC 5849 protocol constants and the explicit Source-driver factory for client-only OAuth 1.0 support.
 *
 * @author Kimi Liu
 */
public final class OAuth1 {

    /**
     * OAuth protocol version emitted in the optional {@code oauth_version} parameter.
     */
    public static final String VERSION = "1.0";
    /**
     * HTTP Authorization authentication scheme registered by RFC 5849.
     */
    public static final String AUTHORIZATION_SCHEME = "OAuth";
    /**
     * Prefix identifying OAuth protocol parameters in RFC 5849 request locations.
     */
    public static final String PARAMETER_PREFIX = "oauth_";
    /**
     * Optional protection-space parameter accepted only in the HTTP Authorization header.
     */
    public static final String REALM = "realm";
    /**
     * RFC 5849 HMAC-SHA1 signature method wire token.
     */
    public static final String HMAC_SHA1 = "HMAC-SHA1";
    /**
     * RFC 5849 RSA-SHA1 signature method wire token.
     */
    public static final String RSA_SHA1 = "RSA-SHA1";

    /**
     * Prevents instantiation of the protocol entry class.
     */
    private OAuth1() {
        // No initialization required.
    }

    /**
     * Creates the client-side OAuth 1.0 driver.
     *
     * @return new OAuth 1.0 Source driver
     */
    public static SourceDriver<OAuth1ClientSettings> source() {
        return new OAuth1SourceDriver();
    }

    /**
     * Defines the RFC 5849 parameter names shared by OAuth 1.0 encoders, decoders, clients, and vendor adapters.
     */
    public static final class Parameters {

        /**
         * Consumer identifier parameter name.
         */
        public static final String CONSUMER_KEY = "oauth_consumer_key";

        /**
         * Temporary or token credential identifier parameter name.
         */
        public static final String TOKEN = "oauth_token";

        /**
         * Signature method parameter name.
         */
        public static final String SIGNATURE_METHOD = "oauth_signature_method";

        /**
         * Computed request signature parameter name.
         */
        public static final String SIGNATURE = "oauth_signature";

        /**
         * Unix timestamp parameter name.
         */
        public static final String TIMESTAMP = "oauth_timestamp";

        /**
         * Unique request nonce parameter name.
         */
        public static final String NONCE = "oauth_nonce";

        /**
         * Optional protocol version parameter name.
         */
        public static final String VERSION = "oauth_version";

        /**
         * Temporary credential callback URI parameter name.
         */
        public static final String CALLBACK = "oauth_callback";

        /**
         * Resource owner verification code parameter name.
         */
        public static final String VERIFIER = "oauth_verifier";

        /**
         * Temporary or token credential shared-secret response parameter name.
         */
        public static final String TOKEN_SECRET = "oauth_token_secret";

        /**
         * Temporary credential callback-confirmation response parameter name.
         */
        public static final String CALLBACK_CONFIRMED = "oauth_callback_confirmed";

        /**
         * Prevents instantiation of the OAuth 1.0 parameter namespace.
         */
        private Parameters() {
            // No initialization required.
        }

    }

}
