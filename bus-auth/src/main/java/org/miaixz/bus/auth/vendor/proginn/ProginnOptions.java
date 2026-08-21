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
package org.miaixz.bus.auth.vendor.proginn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Proginn OAuth application values.
 *
 * @param vendor      exact Proginn platform identifier
 * @param variant     exact default Proginn variant
 * @param clientId    public client identifier issued by Proginn
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered for the application
 * @param scopes      ordered requested Proginn scopes, or empty to use the manifest default
 * @author Kimi Liu
 */
public record ProginnOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<ProginnOptions> {

    /**
     * Validates and freezes one Proginn registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope vocabulary, or uniqueness is invalid
     */
    public ProginnOptions {
        if (!ProginnManifest.ID.equals(vendor) || !ProginnManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Proginn options must select proginn/default");
        }
        clientId = Assert.notBlank(clientId, "Proginn client identifier must not be blank");
        Assert.notNull(credential, "Proginn credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Proginn credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Proginn redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Proginn options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Proginn scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Proginn scope must not be blank");
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Proginn scopes must be unique registered Proginn values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Determines whether one requested value belongs to Proginn's preserved scope vocabulary.
     *
     * @param value validated non-blank scope value
     * @return {@code true} when Proginn registers the scope
     */
    private static boolean approvedScope(final String value) {
        return switch (value) {
            case "basic", "email", "realname", "cellphone" -> true;
            default -> false;
        };
    }

    /**
     * Validates one absolute credential-free fragmentless application callback.
     *
     * @param value callback URI registered with Proginn
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Proginn redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Proginn redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Proginn redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<ProginnOptions> type() {
        return ProginnOptions.class;
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "ProginnOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
