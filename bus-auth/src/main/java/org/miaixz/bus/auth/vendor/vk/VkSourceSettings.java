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
package org.miaixz.bus.auth.vendor.vk;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed VK ID OAuth 2.0 application values.
 * <p>
 * Fixed endpoints and mandatory S256 behavior remain definition-owned. The application-secret reference preserves the
 * common externally managed registration contract but is not resolved or transmitted by VK's public PKCE flow.
 * </p>
 *
 * @param vendor      exact VK platform identifier
 * @param variant     exact default VK variant
 * @param clientId    VK ID application identifier
 * @param credential  external VK application-secret reference
 * @param redirectUri exact callback URI registered with VK ID
 * @param scopes      ordered approved VK scopes, or empty to select definition defaults
 * @author Kimi Liu
 */
public record VkSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Complete scope vocabulary retained from the historical VK scope declaration.
     */
    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "vkid.personal_info",
            "email",
            "phone",
            "friends",
            "wall",
            "groups",
            "stories",
            "docs",
            "photos",
            "ads",
            "video",
            "status",
            "market",
            "pages",
            "notifications",
            "stats",
            "notes");

    /**
     * Validates and freezes one VK ID registration without resolving credential material.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope, or PKCE state violates the frozen
     *                                  profile
     */
    public VkSourceSettings {
        if (!VkDefinition.ID.equals(vendor) || !VkDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("VK settings must select vk/default");
        }
        clientId = Assert.notBlank(clientId, "VK client id must not be blank");
        Assert.notNull(credential, "VK application-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("VK credential must reference an application client secret");
        }
        Assert.notNull(redirectUri, "VK redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("VK settings require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "VK scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "VK scope must not be blank");
            Scope.parse(checked);
            if (!ALLOWED_SCOPES.contains(checked) || copy.contains(checked)) {
                throw new ValidateException("VK scopes must be unique values from the frozen VK scope vocabulary");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add("vkid.personal_info");
            copy.add("email");
        }
        if (!copy.contains("vkid.personal_info")) {
            throw new ValidateException("VK scopes must include vkid.personal_info for Source identity mapping");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one absolute credential-free fragmentless HTTP(S) callback URI.
     *
     * @param value exact callback URI registered with VK ID
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback URI is malformed or violates its transport boundary
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "VK redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "VK redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("VK redirect URI is invalid", cause);
        }
    }

    /**
     * Reports the immutable S256 requirement of every VK ID Source.
     *
     * @return {@code true} because VK ID authorization always requires PKCE
     */
    @Override
    public boolean pkce() {
        return true;
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "VkSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes + "]";
    }

}
