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
package org.miaixz.bus.auth.vendor.apple;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Sign in with Apple registration and signing-key references.
 * <p>
 * The record contains no private key material. The adapter resolves the referenced key for each dynamic client-secret
 * operation and never caches it in this settings object.
 * </p>
 *
 * @param vendor      exact Apple platform identifier
 * @param variant     exact default variant
 * @param clientId    registered Apple Services identifier
 * @param credential  external EC private-key reference
 * @param redirectUri exact registered Sign in with Apple callback URI
 * @param scopes      ordered requested name and email scopes, or empty to use defaults
 * @param teamId      Apple Developer team identifier used as the client-secret JWT issuer
 * @param keyId       Apple signing key identifier used in the client-secret protected header
 * @author Kimi Liu
 */
public record AppleSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, String teamId, String keyId)
        implements VendorSettings {

    /**
     * Validates and freezes one Sign in with Apple registration without resolving its private key.
     *
     * @throws IllegalArgumentException if a required component, container, scope, or Apple identifier is null or blank
     * @throws ValidateException        if routing, credential type, scope vocabulary, or scope uniqueness differs from
     *                                  the frozen definition
     */
    public AppleSourceSettings {
        if (!AppleDefinition.ID.equals(vendor) || !AppleDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Sign in with Apple settings must select apple/default");
        }
        Assert.notBlank(clientId, "Sign in with Apple client id must not be blank");
        Assert.notNull(credential, "Sign in with Apple signing key reference must not be null");
        if (credential.type() != Credential.Type.PRIVATE_KEY) {
            throw new ValidateException("Sign in with Apple credential must reference a private key");
        }
        Assert.notNull(redirectUri, "Sign in with Apple redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isPresent()) {
            Assert.notBlank(redirectUri.getOrNull(), "Sign in with Apple redirect URI must not be blank");
        }
        Assert.notNull(scopes, "Sign in with Apple scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Sign in with Apple scope must not be blank");
            if (!scope(checked) || copy.contains(checked)) {
                throw new ValidateException("Sign in with Apple scopes must be unique name or email values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        Assert.notBlank(teamId, "Sign in with Apple team id must not be blank");
        Assert.notBlank(keyId, "Sign in with Apple key id must not be blank");
    }

    /**
     * Tests one value against Apple's exact authorization scope vocabulary.
     *
     * @param value scope value
     * @return whether Sign in with Apple accepts the scope
     */
    private static boolean scope(final String value) {
        return switch (value) {
            case "name", "email" -> true;
            default -> false;
        };
    }

    /**
     * Returns a diagnostic representation without client, credential, callback, team, or key identifiers.
     *
     * @return redacted settings description
     */
    @Override
    public String toString() {
        return "AppleSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + ", teamId=[REDACTED], keyId=[REDACTED]]";
    }

}
