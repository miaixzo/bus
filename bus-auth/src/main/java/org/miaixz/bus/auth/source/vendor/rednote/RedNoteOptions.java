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
package org.miaixz.bus.auth.source.vendor.rednote;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Xiaohongshu marketing application values.
 *
 * @param vendor      exact RedNote platform identifier
 * @param variant     exact marketing variant
 * @param clientId    marketing application identifier
 * @param credential  external application-secret reference
 * @param redirectUri exact callback URI registered for authorization
 * @param scopes      ordered official marketing scopes, or empty to use the manifest default
 * @author Kimi Liu
 */
public record RedNoteOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<RedNoteOptions> {

    /**
     * Validates and freezes one RedNote marketing registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope vocabulary, or identity coverage is
     *                                  invalid
     */
    public RedNoteOptions {
        if (!RedNoteManifest.ID.equals(vendor) || !RedNoteManifest.MARKETING.equals(variant)) {
            throw new ValidateException("RedNote options must select rednote/marketing");
        }
        clientId = Assert.notBlank(clientId, "RedNote marketing application id must not be blank");
        Assert.notNull(credential, "RedNote marketing credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("RedNote marketing credential must reference an application secret");
        }
        Assert.notNull(redirectUri, "RedNote marketing redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("RedNote marketing options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "RedNote marketing scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "RedNote marketing scope must not be blank");
            if (!marketingScope(checked) || copy.contains(checked)) {
                throw new ValidateException("RedNote marketing scopes must be unique registered values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Determines whether one requested value belongs to the preserved Xiaohongshu marketing scope vocabulary.
     *
     * @param value validated non-blank scope value
     * @return {@code true} when the marketing platform registers the scope
     */
    private static boolean marketingScope(final String value) {
        return switch (value) {
            case "report_service", "ad_query", "ad_manage", "account_manage" -> true;
            default -> false;
        };
    }

    /**
     * Validates one absolute credential-free fragmentless authorization callback.
     *
     * @param value callback URI registered with RedNote
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "RedNote marketing redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "RedNote marketing redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("RedNote marketing redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<RedNoteOptions> type() {
        return RedNoteOptions.class;
    }

    /**
     * Returns a diagnostic representation without application, credential, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "RedNoteOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
