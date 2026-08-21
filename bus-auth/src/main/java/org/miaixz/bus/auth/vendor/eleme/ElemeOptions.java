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
package org.miaixz.bus.auth.vendor.eleme;

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
 * Carries externally managed Eleme service-provider OAuth client values.
 * <p>
 * Fixed endpoints and merchant RPC behavior remain manifest-owned. The record retains only the public application key,
 * an external application-secret reference, the exact registered callback, and the sole supported scope.
 * </p>
 *
 * @param vendor      exact Eleme platform identifier
 * @param variant     exact default service-provider variant
 * @param clientId    registered application key
 * @param credential  external application-secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      ordered requested scope, or empty to use the manifest default
 * @author Kimi Liu
 */
public record ElemeOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<ElemeOptions> {

    /**
     * Validates and freezes one Eleme registration without resolving its application secret.
     *
     * @throws IllegalArgumentException if a required component or collection member is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scope data violate the frozen manifest
     */
    public ElemeOptions {
        if (!ElemeManifest.ID.equals(vendor) || !ElemeManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Eleme options must select eleme/default");
        }
        Assert.notBlank(clientId, "Eleme client id must not be blank");
        Assert.notNull(credential, "Eleme credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Eleme credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Eleme redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Eleme options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Eleme scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Eleme scope must not be blank");
            if (!"all".equals(checked) || copy.contains(checked)) {
                throw new ValidateException("Eleme scopes may contain only one all value");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Eleme redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Eleme redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Eleme redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<ElemeOptions> type() {
        return ElemeOptions.class;
    }

    /**
     * Returns a diagnostic description without client, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "ElemeOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
