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
package org.miaixz.bus.auth.vendor.pinterest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Pinterest OAuth application values.
 * <p>
 * Fixed endpoints, default scopes, and platform wire deviations remain definition-owned. This immutable record retains
 * only the public client identifier, external secret reference, exact registered callback, and selected historical
 * Pinterest scopes.
 * </p>
 *
 * @param vendor      exact Pinterest platform identifier
 * @param variant     exact default Pinterest variant
 * @param clientId    public client identifier issued by Pinterest
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered for the application
 * @param scopes      ordered requested Pinterest scopes, or empty to use the definition default
 * @author Kimi Liu
 */
public record PinterestSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Validates and freezes one Pinterest registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, scope vocabulary, or uniqueness is
     *                                  invalid
     */
    public PinterestSourceSettings {
        if (!PinterestDefinition.ID.equals(vendor) || !PinterestDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Pinterest settings must select pinterest/default");
        }
        clientId = Assert.notBlank(clientId, "Pinterest client identifier must not be blank");
        Assert.notNull(credential, "Pinterest credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Pinterest credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Pinterest redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Pinterest settings require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Pinterest scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Pinterest scope must not be blank");
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Pinterest scopes must be unique registered Pinterest values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Determines whether one requested value belongs to Pinterest's preserved scope vocabulary.
     *
     * @param value validated non-blank scope value
     * @return {@code true} when Pinterest registers the scope
     */
    private static boolean approvedScope(final String value) {
        return switch (value) {
            case "read_public", "write_public", "read_relationships", "write_relationships" -> true;
            default -> false;
        };
    }

    /**
     * Validates one absolute credential-free fragmentless application callback.
     *
     * @param value callback URI registered with Pinterest
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Pinterest redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Pinterest redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Pinterest redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "PinterestSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
