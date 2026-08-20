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

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the six frozen WeChat identity-source variants and their exact platform wire boundaries.
 * <p>
 * Open Platform and Official Account browser authorization are OAuth 2.0 authorization operations, while their
 * query-authenticated token and profile documents remain private Source-authentication steps because successful token
 * responses omit the mandatory {@code token_type}. Mini Program and all WeCom variants use proprietary platform
 * operations and therefore never expose OAuth token or UserInfo capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
public final class WeChatDefinition implements VendorDefinition<WeChatSourceSettings> {

    /**
     * Stable WeChat platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("wechat");

    /**
     * Stable WeChat Open Platform browser variant identifier.
     */
    public static final Vendor.Variant OPEN = new Vendor.Variant("open");

    /**
     * Stable WeChat Official Account browser variant identifier.
     */
    public static final Vendor.Variant MP = new Vendor.Variant("mp");

    /**
     * Stable WeChat Mini Program direct variant identifier.
     */
    public static final Vendor.Variant MINI = new Vendor.Variant("mini");

    /**
     * Stable WeCom corporate QR-code variant identifier.
     */
    public static final Vendor.Variant EE = new Vendor.Variant("ee");

    /**
     * Stable WeCom service-provider QR-code variant identifier.
     */
    public static final Vendor.Variant EE_QRCODE = new Vendor.Variant("ee-qrcode");

    /**
     * Stable WeCom web authorization variant identifier.
     */
    public static final Vendor.Variant EE_WEB = new Vendor.Variant("ee-web");

    /**
     * WeChat client-secret authentication transported in endpoint query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * WeChat provider-secret authentication transported in a JSON request entity.
     */
    private static final Endpoint.Authentication PROVIDER_SECRET_JSON = new Endpoint.Authentication(
            "provider_secret_json");

    /**
     * Additional enterprise member endpoints retained outside the standard OAuth endpoint set.
     */
    private static final WeChatTargets EE_TARGETS = new WeChatTargets(
            fixed("https://qyapi.weixin.qq.com/cgi-bin/user/get", Http.Method.GET, Endpoint.Authentication.NONE),
            fixed(
                    "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail",
                    Http.Method.POST,
                    Endpoint.Authentication.NONE));

    /**
     * Browser Source authentication plus the public OAuth authorization operation.
     */
    private static final Capability.Manifest OAUTH_BROWSER_MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION));

    /**
     * Direct one-time-code Source authentication exposed by Mini Program.
     */
    private static final Capability.Manifest MINI_MANIFEST = new Capability.Manifest(
            List.of(SourceAuthentication.initiate(Set.of(Capability.Interaction.DIRECT))));

    /**
     * Browser Source authentication exposed by each proprietary WeCom variant.
     */
    private static final Capability.Manifest WORK_BROWSER_MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT))));

    /**
     * Registered Open Platform deviations confined to authorization and private Source authentication.
     */
    private static final List<VendorDeviation> OPEN_DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "appid",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "code/appid/secret/grant_type",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "access_token/refresh_token/expires_in/openid without token_type",
                    "token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/openid/lang",
                    "Bearer profile request",
                    Optional.empty(),
                    Http.Method.GET,
                    false));

    /**
     * Registered Official Account deviations including snapshot-user identity projection.
     */
    private static final List<VendorDeviation> MP_DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "appid",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "authorize",
                    VendorDeviation.Location.RESPONSE,
                    "#wechat_redirect",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "code/appid/secret/grant_type",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "access_token/refresh_token/expires_in/openid/scope/is_snapshotuser without token_type",
                    "token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/openid/lang",
                    "Bearer profile request",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "snapshot user identity",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Registered Mini Program code-to-session wire deviations.
     */
    private static final List<VendorDeviation> MINI_DEVIATIONS = List.of(
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.QUERY,
                    "appid/secret/js_code/grant_type",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.RESPONSE,
                    "session_key/openid/unionid/errcode/errmsg",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Registered WeCom corporate QR-code wire deviations.
     */
    private static final List<VendorDeviation> EE_DEVIATIONS = List.of(
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.QUERY,
                    "login_type/appid/agentid/redirect_uri/state/lang",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.RESPONSE,
                    "#wechat_redirect",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "corpid/corpsecret",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/code",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "userid/user_ticket/errcode/errmsg",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Registered WeCom service-provider QR-code wire deviations.
     */
    private static final List<VendorDeviation> EE_QRCODE_DEVIATIONS = List.of(
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.QUERY,
                    "appid/redirect_uri/state/usertype",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.JSON,
                    "corpid/provider_secret",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    null,
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.JSON,
                    "auth_code",
                    OAuth2.Parameters.CODE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "corp_info/user_info/redirect_login_info",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Registered WeCom web authorization wire deviations.
     */
    private static final List<VendorDeviation> EE_WEB_DEVIATIONS = List.of(
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.QUERY,
                    "appid/agentid/redirect_uri/response_type/scope/state",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.RESPONSE,
                    "#wechat_redirect",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "corpid/corpsecret",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/code",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "UserId/DeviceId/errcode/errmsg",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable WeChat Open Platform definition.
     */
    private static final VendorDefinition.Definition OPEN_DEFINITION = new VendorDefinition.Definition(ID, OPEN,
            Protocol.OAUTH2, List.of("snsapi_login"),
            targets(
                    fixed(
                            "https://open.weixin.qq.com/connect/qrconnect",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    fixed("https://api.weixin.qq.com/sns/oauth2/access_token", Http.Method.GET, CLIENT_SECRET_QUERY),
                    fixed("https://api.weixin.qq.com/sns/userinfo", Http.Method.GET, Endpoint.Authentication.NONE),
                    fixed(
                            "https://api.weixin.qq.com/sns/oauth2/refresh_token",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE)),
            OAUTH_BROWSER_MANIFEST, OPEN_DEVIATIONS);

    /**
     * Complete immutable WeChat Official Account definition.
     */
    private static final VendorDefinition.Definition MP_DEFINITION = new VendorDefinition.Definition(ID, MP,
            Protocol.OAUTH2, List.of("snsapi_userinfo"),
            targets(
                    fixed(
                            "https://open.weixin.qq.com/connect/oauth2/authorize",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    fixed("https://api.weixin.qq.com/sns/oauth2/access_token", Http.Method.GET, CLIENT_SECRET_QUERY),
                    fixed("https://api.weixin.qq.com/sns/userinfo", Http.Method.GET, Endpoint.Authentication.NONE),
                    fixed(
                            "https://api.weixin.qq.com/sns/oauth2/refresh_token",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE)),
            OAUTH_BROWSER_MANIFEST, MP_DEVIATIONS);

    /**
     * Complete immutable WeChat Mini Program definition.
     */
    private static final VendorDefinition.Definition MINI_DEFINITION = new VendorDefinition.Definition(ID, MINI,
            Protocol.VENDOR_AUTH, List.of(),
            targets(
                    null,
                    fixed("https://api.weixin.qq.com/sns/jscode2session", Http.Method.GET, CLIENT_SECRET_QUERY),
                    null,
                    null),
            MINI_MANIFEST, MINI_DEVIATIONS);

    /**
     * Complete immutable WeCom corporate QR-code definition.
     */
    private static final VendorDefinition.Definition EE_DEFINITION = new VendorDefinition.Definition(ID, EE,
            Protocol.VENDOR_AUTH, List.of(),
            targets(
                    fixed(
                            "https://login.work.weixin.qq.com/wwlogin/sso/login",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    fixed("https://qyapi.weixin.qq.com/cgi-bin/gettoken", Http.Method.GET, CLIENT_SECRET_QUERY),
                    fixed(
                            "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    null),
            WORK_BROWSER_MANIFEST, EE_DEVIATIONS);

    /**
     * Complete immutable WeCom service-provider QR-code definition.
     */
    private static final VendorDefinition.Definition EE_QRCODE_DEFINITION = new VendorDefinition.Definition(ID,
            EE_QRCODE, Protocol.VENDOR_AUTH, List.of(),
            targets(
                    fixed(
                            "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    fixed(
                            "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token",
                            Http.Method.POST,
                            PROVIDER_SECRET_JSON),
                    fixed(
                            "https://qyapi.weixin.qq.com/cgi-bin/service/get_login_info",
                            Http.Method.POST,
                            Endpoint.Authentication.NONE),
                    null),
            WORK_BROWSER_MANIFEST, EE_QRCODE_DEVIATIONS);

    /**
     * Complete immutable WeCom web authorization definition.
     */
    private static final VendorDefinition.Definition EE_WEB_DEFINITION = new VendorDefinition.Definition(ID, EE_WEB,
            Protocol.VENDOR_AUTH, List.of("snsapi_base"),
            targets(
                    fixed(
                            "https://open.weixin.qq.com/connect/oauth2/authorize",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    fixed("https://qyapi.weixin.qq.com/cgi-bin/gettoken", Http.Method.GET, CLIENT_SECRET_QUERY),
                    fixed(
                            "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    null),
            WORK_BROWSER_MANIFEST, EE_WEB_DEVIATIONS);

    /**
     * Creates the stateless WeChat definition used by Vendor directory assembly.
     */
    public WeChatDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one endpoint set using the four historical WeChat slots.
     *
     * @param authorization authorization endpoint, or {@code null} for direct authentication
     * @param token         token or private credential endpoint
     * @param userInfo      private identity endpoint, or {@code null}
     * @param refresh       private refresh endpoint, or {@code null}
     * @return immutable endpoint set
     */
    private static VendorTargets targets(
            final VendorTargets.Fixed authorization,
            final VendorTargets.Fixed token,
            final VendorTargets.Fixed userInfo,
            final VendorTargets.Fixed refresh) {
        return new VendorTargets(Optional.ofNullable(authorization), Optional.empty(), Optional.ofNullable(token),
                Optional.ofNullable(userInfo), Optional.ofNullable(refresh), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Creates one fixed credential-free WeChat HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication method
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable registered WeChat wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact WeChat field or representation name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the result uses a platform envelope
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName), mediaType,
                method, enveloped);
    }

    /**
     * Returns the stable WeChat routing identifier.
     *
     * @return WeChat platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive WeChat presentation metadata.
     *
     * @return immutable WeChat management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("WeChat",
                "WeChat Open Platform, Official Account, Mini Program, and WeCom authentication", "wechat");
    }

    /**
     * Returns the exact settings record shared by all WeChat variants.
     *
     * @return WeChat Source settings class
     */
    @Override
    public Class<WeChatSourceSettings> settingsType() {
        return WeChatSourceSettings.class;
    }

    /**
     * Returns the additional enterprise member endpoints used after an EE identity callback.
     *
     * @return immutable enterprise member endpoint association
     */
    public WeChatTargets enterpriseTargets() {
        return EE_TARGETS;
    }

    /**
     * Returns all six variants in stable management order.
     *
     * @return immutable ordered variant definitions
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(
                OPEN_DEFINITION,
                MP_DEFINITION,
                MINI_DEFINITION,
                EE_DEFINITION,
                EE_QRCODE_DEFINITION,
                EE_WEB_DEFINITION);
    }

    /**
     * Resolves the unique selected WeChat variant.
     *
     * @param variant requested WeChat variant
     * @return exact immutable definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (OPEN.equals(variant)) {
            return OPEN_DEFINITION;
        }
        if (MP.equals(variant)) {
            return MP_DEFINITION;
        }
        if (MINI.equals(variant)) {
            return MINI_DEFINITION;
        }
        if (EE.equals(variant)) {
            return EE_DEFINITION;
        }
        if (EE_QRCODE.equals(variant)) {
            return EE_QRCODE_DEFINITION;
        }
        if (EE_WEB.equals(variant)) {
            return EE_WEB_DEFINITION;
        }
        throw new ValidateException("WeChat Vendor variant is not supported");
    }

}
