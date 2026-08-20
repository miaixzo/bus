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
package org.miaixz.bus.auth.vendor.ximalaya;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed values for one Ximalaya OAuth application.
 * <p>
 * Fixed endpoints and signature behavior remain manifest-owned. The record retains the six common Vendor components
 * followed only by the device, operating-system, and package selectors required by Ximalaya's official wire. Secret
 * material remains an external reference and is never retained here.
 * </p>
 *
 * @param vendor       exact Ximalaya platform identifier
 * @param variant      exact {@code default} variant
 * @param clientId     Ximalaya application key
 * @param credential   external application-secret reference
 * @param redirectUri  exact callback registered for the application
 * @param scopes       empty list because the frozen authorization request has no scope parameter
 * @param deviceId     actual device identifier registered for this Source
 * @param clientOsType official client type: {@code 1}, {@code 2}, or {@code 3}
 * @param packageId    package name or bundle identifier sent as {@code pack_id}
 * @author Kimi Liu
 */
public record XimalayaOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, String deviceId,
        String clientOsType, String packageId) implements VendorOptions<XimalayaOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<XimalayaOptions> type() {
        return XimalayaOptions.class;
    }

    /**
     * Closed official client operating-system vocabulary.
     */
    private static final Set<String> CLIENT_OS_TYPES = Set.of(Symbol.ONE, Symbol.TWO, Symbol.THREE);

    /**
     * Maximum accepted length of a device or package wire value.
     */
    private static final int MAXIMUM_SELECTOR_LENGTH = 255;

    /**
     * Validates and freezes one Ximalaya registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component or container is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, scope, or a selector violates the frozen
     *                                  manifest
     */
    public XimalayaOptions {
        if (!XimalayaManifest.ID.equals(vendor) || !XimalayaManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Ximalaya options must select ximalaya/default");
        }
        clientId = Assert.notBlank(clientId, "Ximalaya application key must not be blank");
        Assert.notNull(credential, "Ximalaya credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Ximalaya credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Ximalaya redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        callback(redirectUri);
        Assert.notNull(scopes, "Ximalaya scopes must not be null");
        if (!scopes.isEmpty()) {
            throw new ValidateException("Ximalaya default variant does not register authorization scopes");
        }
        scopes = List.of();
        deviceId = selector(deviceId, "device identifier");
        clientOsType = Assert.notBlank(clientOsType, "Ximalaya client operating-system type must not be blank");
        if (!CLIENT_OS_TYPES.contains(clientOsType)) {
            throw new ValidateException("Ximalaya client operating-system type must be 1, 2, or 3");
        }
        packageId = selector(packageId, "package identifier");
    }

    /**
     * Requires one exact credential-free absolute HTTP(S) callback without fragment.
     *
     * @param redirectUri registered callback container
     * @throws ValidateException if the callback is absent or ineligible for browser authorization
     */
    private static void callback(final Optional<String> redirectUri) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Ximalaya options require a registered redirect URI");
        }
        final String value = Assert.notBlank(redirectUri.getOrNull(), "Ximalaya redirect URI must not be blank");
        try {
            final URI uri = new URI(value);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Ximalaya redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Ximalaya redirect URI is invalid", cause);
        }
    }

    /**
     * Validates one bounded visible-ASCII device or package selector.
     *
     * @param value external selector value
     * @param label non-sensitive selector label for validation errors
     * @return validated selector
     * @throws IllegalArgumentException if the selector is blank
     * @throws ValidateException        if the selector is too long or contains non-visible wire characters
     */
    private static String selector(final String value, final String label) {
        final String checked = Assert.notBlank(value, "Ximalaya " + label + " must not be blank");
        if (checked.length() > MAXIMUM_SELECTOR_LENGTH) {
            throw new ValidateException("Ximalaya " + label + " is too long");
        }
        for (int index = 0; index < checked.length(); index++) {
            final char character = checked.charAt(index);
            if (character < 0x21 || character > 0x7e) {
                throw new ValidateException("Ximalaya " + label + " must contain visible ASCII characters only");
            }
        }
        return checked;
    }

    /**
     * Returns a diagnostic representation without application, credential, callback, device, or package values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "XimalayaOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=[]"
                + ", deviceId=[REDACTED], clientOsType=" + clientOsType + ", packageId=[REDACTED]]";
    }

}
