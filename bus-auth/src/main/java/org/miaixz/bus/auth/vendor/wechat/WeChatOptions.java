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
package org.miaixz.bus.auth.vendor.wechat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed client values for the frozen WeChat login and WeCom enterprise variants.
 * <p>
 * The common components retain only public registration data and an external client-secret reference. The four platform
 * selectors are a closed union: each variant must leave selectors owned by another variant empty. Direct Mini Program
 * authentication and enterprise directory access prohibit a redirect URI, while every browser Variant requires exact
 * callback text. The enterprise Variant interprets the common client identifier as Corp ID and retains only an external
 * application Corp Secret reference.
 * </p>
 *
 * @param vendor      exact WeChat platform identifier
 * @param variant     selected WeChat product or login flow
 * @param clientId    App ID or Corp ID registered for the selected flow
 * @param credential  external App Secret, Corp Secret, or provider-secret reference
 * @param redirectUri exact registered browser callback, empty for Mini Program and enterprise
 * @param scopes      ordered login scopes, empty for Mini Program and enterprise
 * @param loginType   WeCom QR {@code login_type}, empty for every other variant
 * @param agentId     WeCom {@code agentid}, empty when the selected flow does not use it
 * @param language    WeCom QR {@code lang}, empty for every other variant
 * @param userType    service-provider QR {@code usertype}, empty for every other variant
 * @author Kimi Liu
 */
public record WeChatOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String loginType, String agentId, String language,
        String userType) implements VendorOptions<WeChatOptions> {

    /**
     * Sole Open Platform scope retained from the historical provider.
     */
    private static final String OPEN_SCOPE = "snsapi_login";
    /**
     * Official Account scope that permits profile retrieval.
     */
    private static final String OFFICIAL_PROFILE_SCOPE = "snsapi_userinfo";
    /**
     * Official Account and WeCom scope that returns only the platform subject.
     */
    private static final String BASE_SCOPE = "snsapi_base";
    /**
     * Supported WeCom corporate application login type.
     */
    private static final String CORPORATE_APPLICATION = "CorpApp";
    /**
     * Supported WeCom service application login type.
     */
    private static final String SERVICE_APPLICATION = "ServiceApp";
    /**
     * Historical default language sent to the WeCom QR login page.
     */
    private static final String DEFAULT_LANGUAGE = "zh";

    /**
     * Validates and freezes one WeChat registration without resolving credential material.
     *
     * @throws IllegalArgumentException if a required component or collection member is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope, or variant selector values are invalid
     */
    public WeChatOptions {
        if (!WeChatManifest.ID.equals(vendor) || !supported(variant)) {
            throw new ValidateException("WeChat options must select one frozen wechat variant");
        }
        clientId = Assert.notBlank(clientId, "WeChat client identifier must not be blank");
        Assert.notNull(credential, "WeChat credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("WeChat credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "WeChat redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "WeChat scopes must not be null");
        loginType = Assert.notNull(loginType, "WeChat login type must not be null");
        agentId = Assert.notNull(agentId, "WeChat agent id must not be null");
        language = Assert.notNull(language, "WeChat language must not be null");
        userType = Assert.notNull(userType, "WeChat user type must not be null");

        scopes = scopes(scopes);
        if (WeChatManifest.OPEN.equals(variant)) {
            browser(redirectUri);
            scopes = singleScope(scopes, OPEN_SCOPE, "WeChat Open Platform");
            prohibit(loginType, agentId, language, userType, "WeChat Open Platform");
        } else if (WeChatManifest.MP.equals(variant)) {
            browser(redirectUri);
            scopes = officialScopes(scopes);
            prohibit(loginType, agentId, language, userType, "WeChat Official Account");
        } else if (WeChatManifest.MINI.equals(variant)) {
            direct(redirectUri, scopes);
            prohibit(loginType, agentId, language, userType, "WeChat Mini Program");
        } else if (WeChatManifest.EE.equals(variant)) {
            browser(redirectUri);
            requireNoScopes(scopes, "WeCom QR");
            loginType = loginType.isEmpty() ? CORPORATE_APPLICATION : loginType;
            language = language.isEmpty() ? DEFAULT_LANGUAGE : language;
            workQr(loginType, agentId, language, userType);
        } else if (WeChatManifest.EE_QRCODE.equals(variant)) {
            browser(redirectUri);
            requireNoScopes(scopes, "WeCom service-provider QR");
            thirdParty(loginType, agentId, language, userType);
        } else if (WeChatManifest.EE_WEB.equals(variant)) {
            browser(redirectUri);
            scopes = singleScope(scopes, BASE_SCOPE, "WeCom web");
            workWeb(loginType, agentId, language, userType);
        } else {
            enterprise(redirectUri, scopes, loginType, agentId, language, userType);
        }
    }

    /**
     * Reports whether a variant belongs to the closed WeChat profile set.
     *
     * @param variant candidate variant
     * @return {@code true} only for one of the seven frozen variants
     */
    private static boolean supported(final Vendor.Variant variant) {
        return WeChatManifest.OPEN.equals(variant) || WeChatManifest.MP.equals(variant)
                || WeChatManifest.MINI.equals(variant) || WeChatManifest.EE.equals(variant)
                || WeChatManifest.EE_QRCODE.equals(variant) || WeChatManifest.EE_WEB.equals(variant)
                || WeChatManifest.EE_ENTERPRISE.equals(variant);
    }

    /**
     * Copies and validates caller-supplied scope strings without changing their order.
     *
     * @param values caller-supplied scope values
     * @return immutable unique scope values
     * @throws ValidateException if a scope is blank or duplicated
     */
    private static List<String> scopes(final List<String> values) {
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values) {
            final String scope = Assert.notBlank(value, "WeChat scope must not be blank");
            if (copy.contains(scope)) {
                throw new ValidateException("WeChat scopes must be unique");
            }
            copy.add(scope);
        }
        return List.copyOf(copy);
    }

    /**
     * Requires and validates an exact credential-free HTTP(S) browser callback.
     *
     * @param redirectUri registered callback container
     * @throws ValidateException if the callback is absent or not an eligible exact browser callback
     */
    private static void browser(final Optional<String> redirectUri) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("WeChat browser options require a registered redirect URI");
        }
        final String value = Assert.notBlank(redirectUri.getOrNull(), "WeChat redirect URI must not be blank");
        try {
            final URI uri = new URI(value);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "WeChat redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("WeChat redirect URI is invalid", cause);
        }
    }

    /**
     * Enforces the callback and scope prohibition of Mini Program direct authentication.
     *
     * @param redirectUri callback container that must be empty
     * @param scopes      scope list that must be empty
     * @throws ValidateException if browser-only values are supplied
     */
    private static void direct(final Optional<String> redirectUri, final List<String> scopes) {
        if (redirectUri.isPresent() || !scopes.isEmpty()) {
            throw new ValidateException("WeChat Mini Program options prohibit callback and scope values");
        }
    }

    /**
     * Enforces the exact empty browser and login-selector surface of enterprise directory access.
     *
     * @param redirectUri forbidden browser callback
     * @param scopes      forbidden login scopes
     * @param loginType   forbidden QR login type
     * @param agentId     forbidden login agent identifier
     * @param language    forbidden login language
     * @param userType    forbidden service-provider user type
     * @throws ValidateException if any browser or login-only value is supplied
     */
    private static void enterprise(
            final Optional<String> redirectUri,
            final List<String> scopes,
            final String loginType,
            final String agentId,
            final String language,
            final String userType) {
        if (redirectUri.isPresent() || !scopes.isEmpty() || !loginType.isEmpty() || !agentId.isEmpty()
                || !language.isEmpty() || !userType.isEmpty()) {
            throw new ValidateException("WeCom enterprise options prohibit browser login selectors");
        }
    }

    /**
     * Normalizes and validates a variant that accepts exactly one scope value.
     *
     * @param scopes   immutable explicit scopes
     * @param required sole allowed and default scope
     * @param label    variant validation label
     * @return immutable singleton scope list
     * @throws ValidateException if explicit scopes differ from the required value
     */
    private static List<String> singleScope(final List<String> scopes, final String required, final String label) {
        if (scopes.isEmpty()) {
            return List.of(required);
        }
        if (scopes.size() != 1 || !required.equals(scopes.get(0))) {
            throw new ValidateException(label + " scope must be exactly " + required);
        }
        return scopes;
    }

    /**
     * Normalizes Official Account scope to exactly one documented identity mode.
     *
     * @param scopes immutable explicit scopes
     * @return immutable singleton Official Account scope
     * @throws ValidateException if more than one or an unknown scope is supplied
     */
    private static List<String> officialScopes(final List<String> scopes) {
        if (scopes.isEmpty()) {
            return List.of(OFFICIAL_PROFILE_SCOPE);
        }
        if (scopes.size() != 1 || !Set.of(OFFICIAL_PROFILE_SCOPE, BASE_SCOPE).contains(scopes.get(0))) {
            throw new ValidateException("WeChat Official Account scope must be snsapi_userinfo or snsapi_base");
        }
        return scopes;
    }

    /**
     * Requires a proprietary variant to carry no protocol scope.
     *
     * @param scopes selected variant scopes
     * @param label  variant validation label
     * @throws ValidateException if a scope is present
     */
    private static void requireNoScopes(final List<String> scopes, final String label) {
        if (!scopes.isEmpty()) {
            throw new ValidateException(label + " options prohibit scope values");
        }
    }

    /**
     * Requires every supplied selector to be the empty string.
     *
     * @param loginType login-type selector
     * @param agentId   agent identifier
     * @param language  language selector
     * @param userType  user-type selector
     * @param label     owning variant label
     * @throws ValidateException if the variant receives a selector owned by another variant
     */
    private static void prohibit(
            final String loginType,
            final String agentId,
            final String language,
            final String userType,
            final String label) {
        if (!loginType.isEmpty() || !agentId.isEmpty() || !language.isEmpty() || !userType.isEmpty()) {
            throw new ValidateException(label + " options prohibit WeCom variant selectors");
        }
    }

    /**
     * Validates WeCom corporate QR-code login selectors.
     *
     * @param loginType effective official login type
     * @param agentId   optional agent identifier, required for {@code CorpApp}
     * @param language  effective interface language
     * @param userType  forbidden service-provider selector
     * @throws ValidateException if selector ownership or the corporate-agent requirement is violated
     */
    private static void workQr(
            final String loginType,
            final String agentId,
            final String language,
            final String userType) {
        if (!Set.of(CORPORATE_APPLICATION, SERVICE_APPLICATION).contains(loginType)) {
            throw new ValidateException("WeCom QR login_type must be CorpApp or ServiceApp");
        }
        Assert.notBlank(language, "WeCom QR language must not be blank");
        if (CORPORATE_APPLICATION.equals(loginType)) {
            Assert.notBlank(agentId, "WeCom CorpApp QR options require agentid");
        }
        if (!userType.isEmpty()) {
            throw new ValidateException("WeCom QR options prohibit service-provider usertype");
        }
    }

    /**
     * Validates WeCom service-provider QR-code selector ownership.
     *
     * @param loginType forbidden corporate login type
     * @param agentId   forbidden agent identifier
     * @param language  forbidden corporate language
     * @param userType  required official service-provider user type
     * @throws IllegalArgumentException if {@code userType} is blank
     * @throws ValidateException        if another WeCom variant selector is present
     */
    private static void thirdParty(
            final String loginType,
            final String agentId,
            final String language,
            final String userType) {
        if (!loginType.isEmpty() || !agentId.isEmpty() || !language.isEmpty()) {
            throw new ValidateException("WeCom service-provider QR options prohibit corporate selectors");
        }
        Assert.notBlank(userType, "WeCom service-provider QR usertype must not be blank");
    }

    /**
     * Validates WeCom web authorization selector ownership.
     *
     * @param loginType forbidden QR login type
     * @param agentId   required web application agent identifier
     * @param language  forbidden QR language
     * @param userType  forbidden service-provider selector
     * @throws IllegalArgumentException if {@code agentId} is blank
     * @throws ValidateException        if a selector owned by another flow is present
     */
    private static void workWeb(
            final String loginType,
            final String agentId,
            final String language,
            final String userType) {
        if (!loginType.isEmpty() || !language.isEmpty() || !userType.isEmpty()) {
            throw new ValidateException("WeCom web options prohibit QR-only selectors");
        }
        Assert.notBlank(agentId, "WeCom web options require agentid");
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<WeChatOptions> type() {
        return WeChatOptions.class;
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, callback, or organization values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "WeChatOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS
                + Builder.REDACTED_VALUE + Symbol.C_BRACKET_RIGHT;
    }

}
