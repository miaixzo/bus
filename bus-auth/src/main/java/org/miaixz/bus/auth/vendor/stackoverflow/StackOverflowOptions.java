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
package org.miaixz.bus.auth.vendor.stackoverflow;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Stack Overflow OAuth application values.
 *
 * @param vendor      exact Stack Overflow platform identifier
 * @param variant     exact default Stack Overflow variant
 * @param clientId    public client identifier issued by Stack Overflow
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered for the application
 * @param scopes      ordered requested Stack Overflow scopes, or empty to use the manifest default
 * @param key         public Stack Apps API key required with an access token
 * @param siteId      Stack Exchange API site identifier used by the identity request
 * @author Kimi Liu
 */
public record StackOverflowOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, String key, String siteId)
        implements VendorOptions<StackOverflowOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<StackOverflowOptions> type() {
        return StackOverflowOptions.class;
    }

    /**
     * Validates and freezes one Stack Overflow registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, scope, or site is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope vocabulary, uniqueness, or site is
     *                                  invalid
     */
    public StackOverflowOptions {
        if (!StackOverflowManifest.ID.equals(vendor) || !StackOverflowManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Stack Overflow options must select stackoverflow/default");
        }
        clientId = Assert.notBlank(clientId, "Stack Overflow client identifier must not be blank");
        Assert.notNull(credential, "Stack Overflow credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Stack Overflow credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Stack Overflow redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Stack Overflow options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Stack Overflow scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Stack Overflow scope must not be blank");
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Stack Overflow scopes must be unique registered Stack Overflow values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        key = Assert.notBlank(key, "Stack Apps API key must not be blank");
        siteId = site(siteId);
    }

    /**
     * Determines whether one requested value belongs to Stack Overflow's preserved scope vocabulary.
     *
     * @param value validated non-blank scope value
     * @return {@code true} when Stack Overflow registers the scope
     */
    private static boolean approvedScope(final String value) {
        return switch (value) {
            case "read_inbox", "no_expiry", "write_access", "private_info" -> true;
            default -> false;
        };
    }

    /**
     * Validates one absolute credential-free fragmentless application callback.
     *
     * @param value callback URI registered with Stack Overflow
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Stack Overflow redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Stack Overflow redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Stack Overflow redirect URI is invalid", cause);
        }
    }

    /**
     * Validates one Stack Exchange API site identifier without allowing URI or query injection.
     *
     * @param value externally managed Stack Exchange site identifier
     * @return validated site identifier
     * @throws IllegalArgumentException if the site identifier is blank
     * @throws ValidateException        if the site identifier contains unsupported characters
     */
    private static String site(final String value) {
        final String checked = Assert.notBlank(value, "Stack Exchange site identifier must not be blank");
        if (!checked.matches("[A-Za-z0-9][A-Za-z0-9.-]{0,127}")) {
            throw new ValidateException("Stack Exchange site identifier contains unsupported characters");
        }
        return checked;
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, callback, API key, or site values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "StackOverflowOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + ", key=[REDACTED], siteId=[REDACTED]]";
    }

}
