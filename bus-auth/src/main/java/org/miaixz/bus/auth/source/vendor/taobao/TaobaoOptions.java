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
package org.miaixz.bus.auth.source.vendor.taobao;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Taobao OAuth application values.
 *
 * @param vendor      exact Taobao platform identifier
 * @param variant     exact default Taobao variant
 * @param clientId    public client identifier issued by Taobao
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered for the application
 * @param scopes      empty list because the historical Taobao flow does not send a scope parameter
 * @author Kimi Liu
 */
public record TaobaoOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<TaobaoOptions> {

    /**
     * Validates and freezes one Taobao registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component or container is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scope state is invalid
     */
    public TaobaoOptions {
        if (!TaobaoManifest.ID.equals(vendor) || !TaobaoManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Taobao options must select taobao/default");
        }
        clientId = Assert.notBlank(clientId, "Taobao client identifier must not be blank");
        Assert.notNull(credential, "Taobao credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Taobao credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Taobao redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Taobao options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "Taobao scopes must not be null");
        if (!scopes.isEmpty()) {
            throw new ValidateException("Taobao default variant does not register a scope parameter");
        }
        scopes = List.of();
    }

    /**
     * Validates one absolute credential-free fragmentless application callback.
     *
     * @param value callback URI registered with Taobao
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not a permitted absolute URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Taobao redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Taobao redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Taobao redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<TaobaoOptions> type() {
        return TaobaoOptions.class;
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "TaobaoOptions[vendor=" + vendor + Builder.VARIANT + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=[]]";
    }

}
