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
package org.miaixz.bus.auth.vendor;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Endpoint;

/**
 * Immutable static registration for one third-party authentication client.
 *
 * <p>
 * The registration contains only public identifiers and configuration. {@code secretId} is an opaque lookup key; secret
 * material is resolved by {@link VendorConfiguration#secrets()} for the active root authentication context and is never
 * stored in this value.
 * </p>
 *
 * @param clientId          public vendor client identifier
 * @param secretId          opaque SecretResolver lookup identifier for kind {@code vendor-client}
 * @param unionId           optional provider-specific organization or application identifier
 * @param extId             optional provider-specific extended identifier
 * @param deviceId          optional device identifier
 * @param type              optional provider-specific client type
 * @param flag              provider-specific feature flag
 * @param pkce              whether the vendor authorization flow uses PKCE
 * @param prefix            optional provider domain prefix
 * @param redirectUri       registered authorization callback URI
 * @param scopes            immutable requested vendor scope names
 * @param ignoreState       whether legacy state verification is disabled
 * @param ignoreRedirectUri whether legacy redirect URI verification is disabled
 * @param kid               optional signing key identifier
 * @param teamId            optional vendor team identifier
 * @param loginType         optional vendor login type, defaulting to {@code CorpApp}
 * @param lang              optional language code, defaulting to {@code zh}
 * @param extension         optional provider-defined textual extension
 * @param attributes        immutable provider-defined non-secret attributes
 * @param endpoints         immutable endpoint overrides indexed by vendor operation
 * @author Kimi Liu
 */
public record VendorRegistration(String clientId, String secretId, String unionId, String extId, String deviceId,
        String type, boolean flag, boolean pkce, String prefix, String redirectUri, List<String> scopes,
        boolean ignoreState, boolean ignoreRedirectUri, String kid, String teamId, String loginType, String lang,
        String extension, Map<String, Object> attributes, Map<VendorEndpoint, Endpoint> endpoints) {

    /**
     * Normalizes optional defaults and snapshots every mutable collection supplied by the caller.
     *
     * @throws NullPointerException if a collection contains a null key, value, or element
     */
    public VendorRegistration {
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
        loginType = loginType == null ? "CorpApp" : loginType;
        lang = lang == null ? "zh" : lang;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
    }

}
