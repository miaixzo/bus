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
package org.miaixz.bus.auth.protocol.oauth1.client;

import java.time.Duration;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth1.SignatureMethod;
import org.miaixz.bus.auth.source.SourceSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Holds immutable deployment settings for one RFC 5849 client Source.
 *
 * @param temporaryCredentialsEndpoint       endpoint used to obtain temporary credentials
 * @param resourceOwnerAuthorizationEndpoint user-agent resource owner authorization endpoint
 * @param tokenCredentialsEndpoint           endpoint used to obtain token credentials
 * @param consumerKey                        registered OAuth consumer key
 * @param signingCredential                  external consumer secret or private-key reference
 * @param signatureMethod                    enabled RFC 5849 signature method
 * @param temporaryCredentialLifetime        maximum temporary credential retention interval
 * @param realm                              optional Authorization header protection-space realm
 * @author Kimi Liu
 */
public record OAuth1ClientSettings(Endpoint temporaryCredentialsEndpoint, Endpoint resourceOwnerAuthorizationEndpoint,
        Endpoint tokenCredentialsEndpoint, String consumerKey, Credential.Reference signingCredential,
        SignatureMethod signatureMethod, Duration temporaryCredentialLifetime, Optional<String> realm)
        implements SourceSettings {

    /**
     * Validates transport, method, credential type, algorithm, lifetime, and realm invariants.
     */
    public OAuth1ClientSettings {
        requireEndpoint(temporaryCredentialsEndpoint, Http.Method.POST, "Temporary credentials endpoint");
        requireEndpoint(resourceOwnerAuthorizationEndpoint, Http.Method.GET, "Resource owner authorization endpoint");
        requireEndpoint(tokenCredentialsEndpoint, Http.Method.POST, "Token credentials endpoint");
        Assert.notBlank(consumerKey, "OAuth 1.0 consumer key must not be blank");
        Assert.notNull(signingCredential, "OAuth 1.0 signing credential must not be null");
        Assert.notNull(signatureMethod, "OAuth 1.0 signature method must not be null");
        if (SignatureMethod.HMAC_SHA1.equals(signatureMethod)) {
            if (signingCredential.type() != Credential.Type.CLIENT_SECRET
                    && signingCredential.type() != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("HMAC-SHA1 requires a client-secret or shared-secret reference");
            }
        } else if (SignatureMethod.RSA_SHA1.equals(signatureMethod)) {
            if (signingCredential.type() != Credential.Type.PRIVATE_KEY) {
                throw new ValidateException("RSA-SHA1 requires a private-key reference");
            }
        } else {
            throw new ValidateException("Unsupported OAuth 1.0 signature method: " + signatureMethod.value());
        }
        Assert.notNull(temporaryCredentialLifetime, "Temporary credential lifetime must not be null");
        if (temporaryCredentialLifetime.isZero() || temporaryCredentialLifetime.isNegative()) {
            throw new ValidateException("Temporary credential lifetime must be positive");
        }
        Assert.notNull(realm, "OAuth 1.0 realm container must not be null");
        final String value = realm.getOrNull();
        if (value != null && (value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)) {
            throw new ValidateException("OAuth 1.0 realm must be non-blank and single-line when present");
        }
        realm = Optional.ofNullable(value);
    }

    /**
     * Requires one HTTPS endpoint with the exact operation method.
     *
     * @param endpoint endpoint to validate
     * @param method   required HTTP method
     * @param label    safe diagnostic label
     */
    private static void requireEndpoint(final Endpoint endpoint, final Http.Method method, final String label) {
        Assert.notNull(endpoint, label + " must not be null");
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != method) {
            throw new ValidateException(label + " must use HTTPS " + method.name());
        }
    }

    /**
     * Returns a diagnostic representation without consumer or credential identifiers.
     *
     * @return redacted settings summary
     */
    @Override
    public String toString() {
        return "OAuth1ClientSettings[temporaryCredentialsEndpoint=" + temporaryCredentialsEndpoint.url().redact()
                + ", resourceOwnerAuthorizationEndpoint=" + resourceOwnerAuthorizationEndpoint.url().redact()
                + ", tokenCredentialsEndpoint=" + tokenCredentialsEndpoint.url().redact()
                + ", consumerKey=[REDACTED], signingCredential=[REDACTED], signatureMethod=" + signatureMethod.value()
                + ", temporaryCredentialLifetime=" + temporaryCredentialLifetime + ", realm=[REDACTED]]";
    }

}
