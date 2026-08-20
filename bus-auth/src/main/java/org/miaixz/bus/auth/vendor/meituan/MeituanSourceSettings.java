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
package org.miaixz.bus.auth.vendor.meituan;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Meituan Waimai application registration values.
 * <p>
 * Official endpoints, empty-scope behavior, and platform form mappings remain definition- and adapter-owned. This
 * record retains only the canonical public {@code app_id}, an external {@code secret} reference, and the exact
 * registered callback value.
 * </p>
 *
 * @param vendor      exact Meituan platform identifier
 * @param variant     exact default Meituan variant
 * @param clientId    canonical non-negative decimal {@code app_id}
 * @param credential  external application-secret reference
 * @param redirectUri exact callback URI registered by the external project
 * @param scopes      immutable empty list because Meituan requires an empty scope parameter
 * @author Kimi Liu
 */
public record MeituanSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Validates and freezes one Meituan registration without resolving its application secret.
     *
     * @throws IllegalArgumentException if a required component or container is {@code null} or blank
     * @throws ValidateException        if routing, app id, credential, callback, or scopes differ from the definition
     */
    public MeituanSourceSettings {
        if (!MeituanDefinition.ID.equals(vendor) || !MeituanDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Meituan settings must select meituan/default");
        }
        clientId = appId(clientId);
        Assert.notNull(credential, "Meituan application-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Meituan credential must reference an application secret");
        }
        Assert.notNull(redirectUri, "Meituan redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Meituan settings require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Meituan scopes must not be null");
        if (!scopes.isEmpty()) {
            throw new ValidateException("Meituan settings require an empty scope list");
        }
        scopes = List.of();
    }

    /**
     * Validates a canonical non-negative decimal application identifier without changing its lexical value.
     *
     * @param value externally loaded {@code app_id}
     * @return unchanged canonical decimal text
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if it is negative, contains non-decimal syntax, or has a non-canonical form
     */
    private static String appId(final String value) {
        final String checked = Assert.notBlank(value, "Meituan app_id must not be blank");
        try {
            final BigInteger number = new BigInteger(checked);
            if (number.signum() < 0 || !number.toString().equals(checked)) {
                throw new ValidateException("Meituan app_id must be canonical non-negative decimal text");
            }
            return checked;
        } catch (NumberFormatException cause) {
            throw new ValidateException("Meituan app_id must contain only canonical decimal digits", cause);
        }
    }

    /**
     * Validates one credential-free absolute registered callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if it is not absolute, lacks a host, or contains credentials or a fragment
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Meituan redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Meituan redirect URI must be absolute, credential-free, hosted, and fragmentless");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Meituan redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic summary without app id, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "MeituanSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=[]]";
    }

}
