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
package org.miaixz.bus.auth.vendor.coding;

import java.net.IDN;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed CODING OAuth client values and one constrained team label.
 * <p>
 * The team value is not an arbitrary URL or host. It supplies only the first DNS label of manifest-owned endpoint
 * templates; all endpoint scheme, suffix, paths, methods, and authentication remain immutable manifest data.
 * </p>
 *
 * @param vendor      exact CODING platform identifier
 * @param variant     exact default variant
 * @param clientId    registered CODING client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      ordered requested scopes, or empty to use the manifest default
 * @param team        canonical single DNS label used by the constrained endpoint templates
 * @author Kimi Liu
 */
public record CodingOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String team) implements VendorOptions<CodingOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<CodingOptions> type() {
        return CodingOptions.class;
    }

    /**
     * Validates and freezes one CODING registration without resolving its client secret.
     *
     * @throws IllegalArgumentException if a required component, container, scope, or team is null or blank
     * @throws ValidateException        if routing, credential type, scope, or team syntax differs from the frozen
     *                                  profile
     */
    public CodingOptions {
        if (!CodingManifest.ID.equals(vendor) || !CodingManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("CODING options must select coding/default");
        }
        Assert.notBlank(clientId, "CODING client id must not be blank");
        Assert.notNull(credential, "CODING credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("CODING credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "CODING redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("CODING browser options require a registered redirect URI");
        }
        Assert.notBlank(redirectUri.getOrNull(), "CODING redirect URI must not be blank");
        Assert.notNull(scopes, "CODING scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "CODING scope must not be blank");
            if (!"user:profile:ro".equals(checked) || copy.contains(checked)) {
                throw new ValidateException("CODING scopes may contain only one user:profile:ro value");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        team = dnsLabel(team);
    }

    /**
     * Normalizes one team value to a canonical single-label ASCII DNS form.
     *
     * @param value externally loaded team label
     * @return lowercase ASCII DNS label
     * @throws ValidateException if the value is not one complete DNS label
     */
    private static String dnsLabel(final String value) {
        final String ascii;
        try {
            ascii = IDN.toASCII(Assert.notBlank(value, "CODING team must not be blank"));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("CODING team is not a valid DNS label", cause);
        }
        if (ascii.length() > 63 || ascii.indexOf(Symbol.C_DOT) >= 0 || ascii.startsWith(Symbol.MINUS)
                || ascii.endsWith(Symbol.MINUS) || !ascii.chars()
                        .allMatch(character -> Character.isLetterOrDigit(character) || character == Symbol.C_MINUS)) {
            throw new ValidateException("CODING team must be one canonical DNS label");
        }
        return ascii.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the constrained team label consumed by manifest-owned endpoint templates.
     *
     * @return present canonical CODING team label
     */
    @Override
    public Optional<String> templateInstance() {
        return Optional.of(team);
    }

    /**
     * Returns a diagnostic representation without client, credential, callback, or tenant data.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "CodingOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + ", team=[REDACTED]]";
    }

}
