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
package org.miaixz.bus.auth.vendor.twitter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Twitter OAuth 1.0a application values.
 *
 * @param vendor      exact Twitter platform identifier
 * @param variant     exact OAuth 1.0a Twitter variant
 * @param clientId    public consumer key issued by Twitter
 * @param credential  external consumer-secret reference
 * @param redirectUri exact OAuth callback URI registered for the application
 * @param scopes      empty list because OAuth 1.0 does not define OAuth 2.0 scope parameters
 * @author Kimi Liu
 */
public record TwitterSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Validates and freezes one Twitter OAuth 1.0a registration without resolving its consumer secret.
     *
     * @throws IllegalArgumentException if a required component or container is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scope state is invalid
     */
    public TwitterSourceSettings {
        if (!TwitterDefinition.ID.equals(vendor) || !TwitterDefinition.OAUTH1.equals(variant)) {
            throw new ValidateException("Twitter settings must select twitter/oauth1");
        }
        clientId = Assert.notBlank(clientId, "Twitter consumer key must not be blank");
        Assert.notNull(credential, "Twitter consumer-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET && credential.type() != Credential.Type.SHARED_SECRET) {
            throw new ValidateException("Twitter HMAC-SHA1 credential must reference a client or shared secret");
        }
        Assert.notNull(redirectUri, "Twitter callback URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Twitter settings require a registered OAuth callback URI");
        }
        callback(redirectUri.getOrNull());
        Assert.notNull(scopes, "Twitter scopes must not be null");
        if (!scopes.isEmpty()) {
            throw new ValidateException("Twitter OAuth 1.0a does not accept OAuth 2.0 scopes");
        }
        scopes = List.of();
    }

    /**
     * Validates one absolute credential-free fragmentless OAuth callback URI.
     *
     * @param value callback URI registered with Twitter
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void callback(final String value) {
        final String checked = Assert.notBlank(value, "Twitter callback URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Twitter callback URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Twitter callback URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without consumer, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "TwitterSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=[]]";
    }

}
