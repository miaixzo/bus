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
package org.miaixz.bus.auth.protocol.oauth2;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves an OAuth 2.x error code using the open extension syntax defined by RFC 6749.
 * <p>
 * Constants cover error codes emitted or consumed by the implemented OAuth endpoint profiles. Unknown codes remain
 * representable because OAuth extension specifications can register additional values without changing this API.
 * </p>
 *
 * @param value case-sensitive OAuth error wire value
 * @author Kimi Liu
 */
public record OAuth2ErrorCode(String value) {

    /**
     * Malformed or incomplete request defined by RFC 6749.
     */
    public static final OAuth2ErrorCode INVALID_REQUEST = new OAuth2ErrorCode("invalid_request");

    /**
     * Client not permitted to use the requested authorization method, as defined by RFC 6749.
     */
    public static final OAuth2ErrorCode UNAUTHORIZED_CLIENT = new OAuth2ErrorCode("unauthorized_client");

    /**
     * Resource owner or authorization server denial defined by RFC 6749.
     */
    public static final OAuth2ErrorCode ACCESS_DENIED = new OAuth2ErrorCode("access_denied");

    /**
     * Unsupported authorization response type defined by RFC 6749.
     */
    public static final OAuth2ErrorCode UNSUPPORTED_RESPONSE_TYPE = new OAuth2ErrorCode("unsupported_response_type");

    /**
     * Invalid, unknown, malformed, or excessive scope defined by RFC 6749.
     */
    public static final OAuth2ErrorCode INVALID_SCOPE = new OAuth2ErrorCode("invalid_scope");

    /**
     * Authorization server processing failure defined by RFC 6749.
     */
    public static final OAuth2ErrorCode SERVER_ERROR = new OAuth2ErrorCode("server_error");

    /**
     * Temporarily unavailable authorization server defined by RFC 6749.
     */
    public static final OAuth2ErrorCode TEMPORARILY_UNAVAILABLE = new OAuth2ErrorCode("temporarily_unavailable");

    /**
     * Failed or unsupported client authentication defined by RFC 6749.
     */
    public static final OAuth2ErrorCode INVALID_CLIENT = new OAuth2ErrorCode("invalid_client");

    /**
     * Invalid authorization grant or refresh token defined by RFC 6749.
     */
    public static final OAuth2ErrorCode INVALID_GRANT = new OAuth2ErrorCode("invalid_grant");

    /**
     * Unsupported authorization grant type defined by RFC 6749.
     */
    public static final OAuth2ErrorCode UNSUPPORTED_GRANT_TYPE = new OAuth2ErrorCode("unsupported_grant_type");

    /**
     * Unsupported token type hint defined by RFC 7009.
     */
    public static final OAuth2ErrorCode UNSUPPORTED_TOKEN_TYPE = new OAuth2ErrorCode("unsupported_token_type");

    /**
     * Invalid access token defined by RFC 6750.
     */
    public static final OAuth2ErrorCode INVALID_TOKEN = new OAuth2ErrorCode("invalid_token");

    /**
     * Access token with insufficient privileges defined by RFC 6750.
     */
    public static final OAuth2ErrorCode INSUFFICIENT_SCOPE = new OAuth2ErrorCode("insufficient_scope");

    /**
     * Pending device authorization defined by RFC 8628.
     */
    public static final OAuth2ErrorCode AUTHORIZATION_PENDING = new OAuth2ErrorCode("authorization_pending");

    /**
     * Excessive device-token polling rate defined by RFC 8628.
     */
    public static final OAuth2ErrorCode SLOW_DOWN = new OAuth2ErrorCode("slow_down");

    /**
     * Expired device code defined by RFC 8628.
     */
    public static final OAuth2ErrorCode EXPIRED_TOKEN = new OAuth2ErrorCode("expired_token");

    /**
     * Unacceptable requested target defined by RFC 8693 and RFC 8707.
     */
    public static final OAuth2ErrorCode INVALID_TARGET = new OAuth2ErrorCode("invalid_target");

    /**
     * Invalid Demonstrating Proof-of-Possession proof defined by RFC 9449.
     */
    public static final OAuth2ErrorCode INVALID_DPOP_PROOF = new OAuth2ErrorCode("invalid_dpop_proof");

    /**
     * Request for a server-provided DPoP nonce defined by RFC 9449.
     */
    public static final OAuth2ErrorCode USE_DPOP_NONCE = new OAuth2ErrorCode("use_dpop_nonce");

    /**
     * Validates the RFC 6749 Appendix A.7 {@code error} syntax without narrowing the extension space.
     *
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if the value contains a character outside {@code NQSCHAR}
     */
    public OAuth2ErrorCode {
        Assert.notEmpty(value, "OAuth 2.x error code must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e || character == 0x22 || character == 0x5c) {
                throw new ValidateException("OAuth 2.x error code contains a character outside RFC 6749 NQSCHAR");
            }
        }
    }

}
