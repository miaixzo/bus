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
package org.miaixz.bus.auth.vendor.slack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Slack OAuth application values.
 *
 * @param vendor      exact Slack platform identifier
 * @param variant     exact default Slack variant
 * @param clientId    Slack application client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered with Slack
 * @param scopes      ordered Slack scopes, or empty to use definition defaults
 * @author Kimi Liu
 */
public record SlackSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Scope required by the frozen {@code users.info} identity operation.
     */
    private static final String USERS_READ = "users:read";

    /**
     * Validates and freezes one Slack registration without resolving its client secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope syntax, uniqueness, or identity coverage
     *                                  fails
     */
    public SlackSourceSettings {
        if (!SlackDefinition.ID.equals(vendor) || !SlackDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Slack settings must select slack/default");
        }
        clientId = Assert.notBlank(clientId, "Slack client identifier must not be blank");
        Assert.notNull(credential, "Slack credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Slack credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Slack redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Slack settings require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Slack scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Slack scope must not be blank");
            Scope.parse(checked);
            if (copy.contains(checked)) {
                throw new ValidateException("Slack scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty() && !copy.contains(USERS_READ)) {
            throw new ValidateException("Explicit Slack scopes must contain users:read for identity resolution");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one absolute credential-free fragmentless Slack callback.
     *
     * @param value callback URI registered with Slack
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Slack redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Slack redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Slack redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "SlackSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
