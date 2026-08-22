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
package org.miaixz.bus.auth.vendor.dingtalk;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed DingTalk client values for the frozen login and enterprise variants.
 * <p>
 * Organization selectors belong only to the delegated {@code oauth2} variant. The {@code account} variant requires all
 * selectors to remain absent and uses the common client identifier as both official {@code appid} and
 * {@code accessKey}. The {@code enterprise} variant uses the client identifier as AppKey and prohibits every
 * browser-only selector. Credential material remains an external reference in every case.
 * </p>
 *
 * @param vendor          exact DingTalk platform identifier
 * @param variant         exact {@code oauth2}, {@code account}, or {@code enterprise} variant
 * @param clientId        registered client identifier, app id, access key, or AppKey
 * @param credential      external client-secret or shared-secret reference selected by the Variant
 * @param redirectUri     exact registered callback URI, empty for enterprise
 * @param scopes          ordered requested login scopes, empty for enterprise
 * @param orgType         optional official organization type for delegated login
 * @param corpId          optional organization identifier for delegated login
 * @param exclusiveLogin  whether delegated login is restricted to one organization
 * @param exclusiveCorpId optional required organization identifier for exclusive delegated login
 * @author Kimi Liu
 */
public record DingTalkOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, Optional<String> orgType,
        Optional<String> corpId, boolean exclusiveLogin, Optional<String> exclusiveCorpId)
        implements VendorOptions<DingTalkOptions> {

    /**
     * Official organization type currently accepted by DingTalk delegated login.
     */
    private static final String MANAGEMENT = "management";

    /**
     * Validates and freezes one DingTalk registration without resolving credential material.
     *
     * @throws IllegalArgumentException if a required component or collection member is {@code null} or blank
     * @throws ValidateException        if routing, credential type, scopes, or organization selectors violate the
     *                                  variant
     */
    public DingTalkOptions {
        if (!DingTalkManifest.ID.equals(vendor) || (!DingTalkManifest.OAUTH2.equals(variant)
                && !DingTalkManifest.ACCOUNT.equals(variant) && !DingTalkManifest.ENTERPRISE.equals(variant))) {
            throw new ValidateException("DingTalk options must select a supported DingTalk variant");
        }
        Assert.notBlank(clientId, "DingTalk client id must not be blank");
        Assert.notNull(credential, "DingTalk credential reference must not be null");
        Assert.notNull(redirectUri, "DingTalk redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "DingTalk scopes must not be null");
        Assert.notNull(orgType, "DingTalk organization type container must not be null");
        Assert.notNull(corpId, "DingTalk organization id container must not be null");
        Assert.notNull(exclusiveCorpId, "DingTalk exclusive organization id container must not be null");
        orgType = text(orgType, "DingTalk organization type");
        corpId = text(corpId, "DingTalk organization id");
        exclusiveCorpId = text(exclusiveCorpId, "DingTalk exclusive organization id");

        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "DingTalk scope must not be blank");
            if (copy.contains(checked)) {
                throw new ValidateException("DingTalk scopes must be unique");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);

        if (DingTalkManifest.OAUTH2.equals(variant)) {
            browserRedirect(redirectUri);
            oauth2(credential, scopes, orgType, exclusiveLogin, exclusiveCorpId);
        } else if (DingTalkManifest.ACCOUNT.equals(variant)) {
            browserRedirect(redirectUri);
            account(credential, scopes, orgType, corpId, exclusiveLogin, exclusiveCorpId);
        } else {
            enterprise(credential, redirectUri, scopes, orgType, corpId, exclusiveLogin, exclusiveCorpId);
        }
    }

    /**
     * Requires the registered callback shared by both browser login variants.
     *
     * @param redirectUri normalized callback container
     * @throws ValidateException if the callback is absent
     */
    private static void browserRedirect(final Optional<String> redirectUri) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("DingTalk browser options require a registered redirect URI");
        }
        Assert.notBlank(redirectUri.getOrNull(), "DingTalk redirect URI must not be blank");
    }

    /**
     * Validates delegated OAuth 2.0 credential, scope, and organization rules.
     *
     * @param credential      external credential reference
     * @param scopes          immutable requested scopes
     * @param orgType         optional official organization type
     * @param exclusiveLogin  exclusive-login selector
     * @param exclusiveCorpId optional exclusive organization identifier
     */
    private static void oauth2(
            final Credential.Reference credential,
            final List<String> scopes,
            final Optional<String> orgType,
            final boolean exclusiveLogin,
            final Optional<String> exclusiveCorpId) {
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("DingTalk oauth2 credential must reference a client secret");
        }
        if (scopes.stream().anyMatch(scope -> !"openid".equals(scope) && !"corpid".equals(scope))) {
            throw new ValidateException("DingTalk oauth2 scopes may contain only openid and corpid");
        }
        if (orgType.isPresent() && !MANAGEMENT.equals(orgType.getOrNull())) {
            throw new ValidateException("DingTalk org_type may contain only management");
        }
        if (exclusiveLogin != exclusiveCorpId.isPresent()) {
            throw new ValidateException("DingTalk exclusiveLogin and exclusiveCorpId must be supplied together");
        }
    }

    /**
     * Validates proprietary account-login credential, scope, and selector rules.
     *
     * @param credential      external credential reference
     * @param scopes          immutable requested scopes
     * @param orgType         forbidden organization type
     * @param corpId          forbidden organization identifier
     * @param exclusiveLogin  forbidden exclusive-login selector
     * @param exclusiveCorpId forbidden exclusive organization identifier
     */
    private static void account(
            final Credential.Reference credential,
            final List<String> scopes,
            final Optional<String> orgType,
            final Optional<String> corpId,
            final boolean exclusiveLogin,
            final Optional<String> exclusiveCorpId) {
        if (credential.type() != Credential.Type.SHARED_SECRET) {
            throw new ValidateException("DingTalk account credential must reference a shared secret");
        }
        if (scopes.size() > 1 || !scopes.isEmpty() && !"snsapi_login".equals(scopes.get(0))) {
            throw new ValidateException("DingTalk account scope may contain only snsapi_login");
        }
        if (orgType.isPresent() || corpId.isPresent() || exclusiveLogin || exclusiveCorpId.isPresent()) {
            throw new ValidateException("DingTalk account options prohibit delegated organization selectors");
        }
    }

    /**
     * Validates the enterprise App Secret reference and prohibits every login-only value.
     *
     * @param credential      external App Secret reference
     * @param redirectUri     forbidden browser callback
     * @param scopes          forbidden browser scopes
     * @param orgType         forbidden login organization type
     * @param corpId          forbidden login organization identifier
     * @param exclusiveLogin  forbidden exclusive-login selector
     * @param exclusiveCorpId forbidden exclusive organization identifier
     */
    private static void enterprise(
            final Credential.Reference credential,
            final Optional<String> redirectUri,
            final List<String> scopes,
            final Optional<String> orgType,
            final Optional<String> corpId,
            final boolean exclusiveLogin,
            final Optional<String> exclusiveCorpId) {
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("DingTalk enterprise credential must reference an App Secret");
        }
        if (redirectUri.isPresent() || !scopes.isEmpty() || orgType.isPresent() || corpId.isPresent() || exclusiveLogin
                || exclusiveCorpId.isPresent()) {
            throw new ValidateException("DingTalk enterprise options prohibit browser login selectors");
        }
    }

    /**
     * Normalizes one optional non-blank official selector.
     *
     * @param value optional selector container
     * @param label validation label
     * @return normalized optional selector
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        final Optional<String> normalized = Optional.ofNullable(value.getOrNull());
        if (normalized.isPresent()) {
            Assert.notBlank(normalized.getOrNull(), label + " must not be blank");
        }
        return normalized;
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<DingTalkOptions> type() {
        return DingTalkOptions.class;
    }

    /**
     * Returns a diagnostic description without client, credential, callback, or organization identifiers.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "DingTalkOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS
                + Builder.REDACTED_VALUE + Symbol.C_BRACKET_RIGHT;
    }

}
