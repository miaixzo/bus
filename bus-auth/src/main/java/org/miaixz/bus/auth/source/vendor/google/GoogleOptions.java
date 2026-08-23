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
package org.miaixz.bus.auth.source.vendor.google;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.protocol.oauth2.Scope;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Carries externally managed Google OpenID Connect login or Workspace service-account values.
 * <p>
 * The default Variant retains the public OAuth Client ID, external Client Secret reference, registered callback, and
 * exact OIDC scopes. The Workspace Variant interprets {@code clientId} as the service-account {@code client_email},
 * keeps the private key external, prohibits a callback, and binds domain-wide delegation to one customer and delegated
 * administrator. All official endpoints remain manifest-owned.
 * </p>
 *
 * @param vendor         exact Google platform identifier
 * @param variant        exact default login or Workspace enterprise Variant
 * @param clientId       registered OAuth Client ID or Workspace service-account {@code client_email}
 * @param credential     external Client Secret or Private Key reference selected by the Variant
 * @param redirectUri    exact authorized login callback, empty for Workspace
 * @param scopes         exact ordered OIDC or Admin SDK scope set selected by the Variant
 * @param customer       exact Workspace customer identifier, empty for login
 * @param delegatedAdmin exact delegated administrator identity, empty for login
 * @author Kimi Liu
 */
public record GoogleOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String customer, String delegatedAdmin)
        implements VendorOptions<GoogleOptions> {

    /**
     * Validates and freezes one Google login or Workspace registration without resolving credential material.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scopes, or Workspace delegation values differ
     *                                  from the frozen Variant contract
     */
    public GoogleOptions {
        if (!GoogleManifest.ID.equals(vendor)
                || !GoogleManifest.DEFAULT.equals(variant) && !GoogleManifest.WORKSPACE.equals(variant)) {
            throw new ValidateException("Google options must select google/default or google/workspace");
        }
        clientId = exactText(clientId, "Google client id");
        Assert.notNull(credential, "Google credential reference must not be null");
        Assert.notNull(redirectUri, "Google redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Google scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = exactText(scope, "Google scope");
            Scope.parse(checked);
            if (copy.contains(checked)) {
                throw new ValidateException("Google scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        customer = exactOptionalText(customer, "Google Workspace customer");
        delegatedAdmin = exactOptionalText(delegatedAdmin, "Google delegated administrator");

        if (GoogleManifest.DEFAULT.equals(variant)) {
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("Google login credential must reference a Client Secret");
            }
            if (redirectUri.isEmpty()) {
                throw new ValidateException("Google login options require an authorized redirect URI");
            }
            redirect(redirectUri.getOrNull());
            if (copy.isEmpty()) {
                copy.addAll(List.of("openid", "profile", "email"));
            }
            if (!copy.equals(List.of("openid", "profile", "email"))) {
                throw new ValidateException("Google login scopes must be exactly openid, profile, and email in order");
            }
            if (!customer.isEmpty() || !delegatedAdmin.isEmpty()) {
                throw new ValidateException("Google login options prohibit Workspace delegation values");
            }
        } else {
            if (credential.type() != Credential.Type.PRIVATE_KEY) {
                throw new ValidateException("Google Workspace credential must reference a Private Key");
            }
            if (redirectUri.isPresent()) {
                throw new ValidateException("Google Workspace options prohibit a redirect URI");
            }
            if (!copy.equals(GoogleManifest.WORKSPACE_SCOPES)) {
                throw new ValidateException("Google Workspace scopes must match the frozen Admin SDK scope order");
            }
            if (customer.isEmpty() || delegatedAdmin.isEmpty()) {
                throw new ValidateException("Google Workspace customer and delegated administrator are required");
            }
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one required value without normalizing or trimming its lexical representation.
     *
     * @param value externally supplied value
     * @param label semantic validation label
     * @return the unchanged validated value
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if surrounding whitespace would require normalization
     */
    private static String exactText(final String value, final String label) {
        final String checked = Assert.notBlank(value, label + " must not be blank");
        if (!checked.equals(StringKit.trim(checked))) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return checked;
    }

    /**
     * Validates an optional lexical value while preserving the empty-string sentinel used by the record contract.
     *
     * @param value optional text represented by an empty string when absent
     * @param label semantic validation label
     * @return the unchanged empty or validated non-blank value
     * @throws IllegalArgumentException if the value is {@code null}
     * @throws ValidateException        if the value is whitespace-only or has surrounding whitespace
     */
    private static String exactOptionalText(final String value, final String label) {
        Assert.notNull(value, label + " must not be null");
        if (value.isEmpty()) {
            return Normal.EMPTY;
        }
        return exactText(value, label);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Google redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Google redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Google redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<GoogleOptions> type() {
        return GoogleOptions.class;
    }

    /**
     * Returns a diagnostic representation without client, credential, callback, scope, or delegation values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "GoogleOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS
                + Builder.REDACTED_VALUE + Symbol.C_BRACKET_RIGHT;
    }

}
