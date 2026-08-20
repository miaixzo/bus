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
package org.miaixz.bus.auth.vendor.amazon;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Login with Amazon OAuth 2.0 client values.
 * <p>
 * The record retains only public registration values, an external secret reference, and the optional S256 selector.
 * Fixed Amazon endpoints remain owned by {@link AmazonManifest}.
 * </p>
 *
 * @param vendor      exact Amazon platform identifier
 * @param variant     exact default variant
 * @param clientId    registered Login with Amazon client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact registered redirect URI
 * @param scopes      ordered requested scopes, or empty to use manifest defaults
 * @param pkce        whether this registration uses optional S256 PKCE
 * @author Kimi Liu
 */
public record AmazonOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, boolean pkce) implements VendorOptions<AmazonOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<AmazonOptions> type() {
        return AmazonOptions.class;
    }

    /**
     * Validates and freezes one Login with Amazon registration without resolving its client secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is null or blank
     * @throws ValidateException        if routing, credential type, scope vocabulary, uniqueness, or identity scope
     *                                  coverage differs from the frozen manifest
     */
    public AmazonOptions {
        if (!AmazonManifest.ID.equals(vendor) || !AmazonManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Login with Amazon options must select amazon/default");
        }
        Assert.notBlank(clientId, "Login with Amazon client id must not be blank");
        Assert.notNull(credential, "Login with Amazon credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Login with Amazon credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Login with Amazon redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isPresent()) {
            Assert.notBlank(redirectUri.getOrNull(), "Login with Amazon redirect URI must not be blank");
        }
        Assert.notNull(scopes, "Login with Amazon scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Login with Amazon scope must not be blank");
            if (!scope(checked) || copy.contains(checked)) {
                throw new ValidateException(
                        "Login with Amazon scopes must be unique profile, profile:user_id, or postal_code values");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty() && !copy.contains("profile") && !copy.contains("profile:user_id")) {
            throw new ValidateException("Explicit Login with Amazon scopes must authorize a profile identifier");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one scope against the exact Login with Amazon registration vocabulary.
     *
     * @param value scope value
     * @return whether Login with Amazon accepts the value
     */
    private static boolean scope(final String value) {
        return switch (value) {
            case "profile", "profile:user_id", "postal_code" -> true;
            default -> false;
        };
    }

    /**
     * Returns a diagnostic representation without client, credential, or callback data.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "AmazonOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes + ", pkce="
                + pkce + Symbol.BRACKET_RIGHT;
    }

}
