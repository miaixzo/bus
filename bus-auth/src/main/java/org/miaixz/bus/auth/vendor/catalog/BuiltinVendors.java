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
package org.miaixz.bus.auth.vendor.catalog;

import java.util.*;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.vendor.VendorConfiguration;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorEndpoint;
import org.miaixz.bus.auth.vendor.VendorProvider;
import org.miaixz.bus.auth.vendor.afdian.AfDianProvider;
import org.miaixz.bus.auth.vendor.alipay.AlipayProvider;
import org.miaixz.bus.auth.vendor.aliyun.AliyunProvider;
import org.miaixz.bus.auth.vendor.amazon.AmazonProvider;
import org.miaixz.bus.auth.vendor.apple.AppleProvider;
import org.miaixz.bus.auth.vendor.baidu.BaiduProvider;
import org.miaixz.bus.auth.vendor.coding.CodingProvider;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkAccountProvider;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkProvider;
import org.miaixz.bus.auth.vendor.douyin.DouyinMiniProvider;
import org.miaixz.bus.auth.vendor.douyin.DouyinProvider;
import org.miaixz.bus.auth.vendor.eleme.ElemeProvider;
import org.miaixz.bus.auth.vendor.facebook.FacebookProvider;
import org.miaixz.bus.auth.vendor.feishu.FeishuProvider;
import org.miaixz.bus.auth.vendor.figma.FigmaProvider;
import org.miaixz.bus.auth.vendor.gitee.GiteeProvider;
import org.miaixz.bus.auth.vendor.github.GithubProvider;
import org.miaixz.bus.auth.vendor.gitlab.GitlabProvider;
import org.miaixz.bus.auth.vendor.google.GoogleProvider;
import org.miaixz.bus.auth.vendor.huawei.HuaweiProvider;
import org.miaixz.bus.auth.vendor.jd.JdProvider;
import org.miaixz.bus.auth.vendor.kujiale.KujialeProvider;
import org.miaixz.bus.auth.vendor.line.LineProvider;
import org.miaixz.bus.auth.vendor.linkedin.LinkedinProvider;
import org.miaixz.bus.auth.vendor.meituan.MeituanProvider;
import org.miaixz.bus.auth.vendor.mi.MiProvider;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftCnProvider;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftProvider;
import org.miaixz.bus.auth.vendor.oidc.OIDCProvider;
import org.miaixz.bus.auth.vendor.okta.OktaProvider;
import org.miaixz.bus.auth.vendor.oschina.OschinaProvider;
import org.miaixz.bus.auth.vendor.pinterest.PinterestProvider;
import org.miaixz.bus.auth.vendor.proginn.ProginnProvider;
import org.miaixz.bus.auth.vendor.qq.QqMiniProvider;
import org.miaixz.bus.auth.vendor.qq.QqProvider;
import org.miaixz.bus.auth.vendor.rednote.RednoteMarketiProvider;
import org.miaixz.bus.auth.vendor.renren.RenrenProvider;
import org.miaixz.bus.auth.vendor.slack.SlackProvider;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowProvider;
import org.miaixz.bus.auth.vendor.taobao.TaobaoProvider;
import org.miaixz.bus.auth.vendor.teambition.TeambitionProvider;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoProvider;
import org.miaixz.bus.auth.vendor.twitter.TwitterProvider;
import org.miaixz.bus.auth.vendor.vk.VKProvider;
import org.miaixz.bus.auth.vendor.wechat.ee.WeChatEeQrcodeProvider;
import org.miaixz.bus.auth.vendor.wechat.ee.WeChatEeThirdQrcodeProvider;
import org.miaixz.bus.auth.vendor.wechat.ee.WeChatEeWebProvider;
import org.miaixz.bus.auth.vendor.wechat.mini.WeChatMiniProvider;
import org.miaixz.bus.auth.vendor.wechat.mp.WeChatMpProvider;
import org.miaixz.bus.auth.vendor.wechat.open.WeChatOpenProvider;
import org.miaixz.bus.auth.vendor.weibo.WeiboProvider;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaProvider;
import org.miaixz.bus.core.net.Protocol;

/**
 * Closed catalog of built-in third-party authentication clients.
 *
 * <p>
 * Each constant owns one immutable set of concrete Fabric endpoints and one correctly typed provider factory. Empty
 * strings and URI templates are not endpoint capabilities; providers requiring tenant-specific hosts obtain concrete
 * addresses exclusively from {@link VendorConfiguration#registration()} overrides.
 * </p>
 *
 * @author Kimi Liu
 */
public enum BuiltinVendors implements VendorDefinition {

    /**
     * AfDian OAuth client.
     */
    AFDIAN(AfDianProvider::new, "https://afdian.com/oauth2/authorize", "https://afdian.com/api/oauth2/access_token",
            null, null, null),
    /**
     * Alipay OAuth client.
     */
    ALIPAY(AlipayProvider::new, "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm",
            "https://openapi.alipay.com/gateway.do", "https://openapi.alipay.com/gateway.do", null, null),
    /**
     * Aliyun OpenID Connect client.
     */
    ALIYUN(AliyunProvider::new, "https://signin.aliyun.com/oauth2/v1/auth", "https://oauth.aliyun.com/v1/token",
            "https://oauth.aliyun.com/v1/userinfo", "https://oauth.aliyun.com/v1/token", null),
    /**
     * Login with Amazon client.
     */
    AMAZON(AmazonProvider::new, "https://www.amazon.com/ap/oa", "https://api.amazon.com/auth/o2/token",
            "https://api.amazon.com/user/profile", "https://api.amazon.com/auth/o2/token", null),
    /**
     * Sign in with Apple client.
     */
    APPLE(AppleProvider::new, "https://appleid.apple.com/auth/authorize", "https://appleid.apple.com/auth/token", null,
            null, null),
    /**
     * Baidu OAuth client.
     */
    BAIDU(BaiduProvider::new, "https://openapi.baidu.com/oauth/2.0/authorize",
            "https://openapi.baidu.com/oauth/2.0/token", "https://openapi.baidu.com/rest/2.0/passport/users/getInfo",
            "https://openapi.baidu.com/oauth/2.0/token",
            "https://openapi.baidu.com/rest/2.0/passport/auth/revokeAuthorization"),
    /**
     * Coding client whose concrete tenant host is registration-owned.
     */
    CODING(CodingProvider::new, null, null, null, null, null),
    /**
     * DingTalk OpenID Connect client.
     */
    DINGTALK(DingTalkProvider::new, "https://login.dingtalk.com/oauth2/challenge.htm",
            "https://api.dingtalk.com/v1.0/OIDC/userAccessToken", "https://api.dingtalk.com/v1.0/contact/users/me",
            null, null),
    /**
     * DingTalk account OAuth client.
     */
    DINGTALK_ACCOUNT(DingTalkAccountProvider::new, "https://oapi.dingtalk.com/connect/oauth2/sns_authorize",
            "https://api.dingtalk.com/v1.0/OIDC/userAccessToken", "https://api.dingtalk.com/v1.0/contact/users/me",
            null, null),
    /**
     * Douyin OAuth client.
     */
    DOUYIN(DouyinProvider::new, "https://open.douyin.com/platform/oauth/connect",
            "https://open.douyin.com/oauth/access_token/", "https://open.douyin.com/oauth/userinfo/",
            "https://open.douyin.com/oauth/refresh_token/", null),
    /**
     * Douyin mini-program code exchange client.
     */
    DOUYIN_MINI(DouyinMiniProvider::new, null, "https://minigame.zijieapi.com/mgplatform/api/apps/jscode2session", null,
            null, null),
    /**
     * Eleme OAuth client.
     */
    ELEME(ElemeProvider::new, "https://open-api.shop.ele.me/authorize", "https://open-api.shop.ele.me/token",
            "https://open-api.shop.ele.me/api/v1/", "https://open-api.shop.ele.me/token", null),
    /**
     * Facebook OAuth client.
     */
    FACEBOOK(FacebookProvider::new, "https://www.facebook.com/v18.0/dialog/oauth",
            "https://graph.facebook.com/v18.0/oauth/access_token", "https://graph.facebook.com/v18.0/me", null, null),
    /**
     * Feishu OAuth client.
     */
    FEISHU(FeishuProvider::new, "https://open.feishu.cn/open-apis/authen/v1/index",
            "https://open.feishu.cn/open-apis/authen/v1/access_token",
            "https://open.feishu.cn/open-apis/authen/v1/user_info",
            "https://open.feishu.cn/open-apis/authen/v1/refresh_access_token", null),
    /**
     * Figma OAuth client.
     */
    FIGMA(FigmaProvider::new, "https://www.figma.com/oauth", "https://www.figma.com/api/oauth/token",
            "https://api.figma.com/v1/me", "https://www.figma.com/api/oauth/refresh", null),
    /**
     * Gitee OAuth client.
     */
    GITEE(GiteeProvider::new, "https://gitee.com/oauth/authorize", "https://gitee.com/oauth/token",
            "https://gitee.com/api/v5/user", null, null),
    /**
     * GitHub OAuth client.
     */
    GITHUB(GithubProvider::new, "https://github.com/login/oauth/authorize",
            "https://github.com/login/oauth/access_token", "https://api.github.com/user", null, null),
    /**
     * GitLab OAuth client.
     */
    GITLAB(GitlabProvider::new, "https://gitlab.com/oauth/authorize", "https://gitlab.com/oauth/token",
            "https://gitlab.com/api/v4/user", null, null),
    /**
     * Google OpenID Connect client.
     */
    GOOGLE(GoogleProvider::new, "https://accounts.google.com/o/oauth2/v2/auth", "https://oauth2.googleapis.com/token",
            "https://openidconnect.googleapis.com/v1/userinfo", null, null),
    /**
     * Huawei OAuth client.
     */
    HUAWEI(HuaweiProvider::new, "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize",
            "https://oauth-login.cloud.huawei.com/oauth2/v3/token", "https://account.cloud.huawei.com/rest.php",
            "https://oauth-login.cloud.huawei.com/oauth2/v3/token", null),
    /**
     * JD OAuth client.
     */
    JD(JdProvider::new, "https://open-oauth.jd.com/oauth2/to_login", "https://open-oauth.jd.com/oauth2/access_token",
            "https://api.jd.com/routerjson", "https://open-oauth.jd.com/OIDC/refresh_token", null),
    /**
     * Kujiale OAuth client.
     */
    KUJIALE(KujialeProvider::new, "https://oauth.kujiale.com/oauth2/show",
            "https://oauth.kujiale.com/oauth2/auth/token", "https://oauth.kujiale.com/oauth2/openapi/user",
            "https://oauth.kujiale.com/oauth2/auth/token/refresh", null),
    /**
     * LINE OAuth client.
     */
    LINE(LineProvider::new, "https://access.line.me/oauth2/v2.1/authorize", "https://api.line.me/oauth2/v2.1/token",
            "https://api.line.me/v2/profile", "https://api.line.me/oauth2/v2.1/token",
            "https://api.line.me/oauth2/v2.1/revoke"),
    /**
     * LinkedIn OAuth client.
     */
    LINKEDIN(LinkedinProvider::new, "https://www.linkedin.com/oauth/v2/authorization",
            "https://www.linkedin.com/oauth/v2/accessToken", "https://api.linkedin.com/v2/me",
            "https://www.linkedin.com/oauth/v2/accessToken", null),
    /**
     * Meituan OAuth client.
     */
    MEITUAN(MeituanProvider::new, "https://openapi.waimai.meituan.com/oauth/authorize",
            "https://openapi.waimai.meituan.com/oauth/access_token",
            "https://openapi.waimai.meituan.com/oauth/userinfo",
            "https://openapi.waimai.meituan.com/oauth/refresh_token", null),
    /**
     * Xiaomi OAuth client.
     */
    MI(MiProvider::new, "https://account.xiaomi.com/oauth2/authorize", "https://account.xiaomi.com/OIDC/token",
            "https://open.account.xiaomi.com/user/profile", "https://account.xiaomi.com/OIDC/token", null),
    /**
     * Microsoft global client whose tenant endpoints are registration-owned.
     */
    MICROSOFT(MicrosoftProvider::new, null, null, "https://graph.microsoft.com/v1.0/me", null, null),
    /**
     * Microsoft China client whose tenant endpoints are registration-owned.
     */
    MICROSOFT_CN(MicrosoftCnProvider::new, null, null, "https://microsoftgraph.chinacloudapi.cn/v1.0/me", null, null),
    /**
     * Generic OpenID Connect client whose complete endpoint set is registration-owned.
     */
    OIDC(OIDCProvider::new, null, null, null, null, null),
    /**
     * Okta client whose organization and authorization-server endpoints are registration-owned.
     */
    OKTA(OktaProvider::new, null, null, null, null, null),
    /**
     * OSChina OAuth client.
     */
    OSCHINA(OschinaProvider::new, "https://www.oschina.net/action/oauth2/authorize",
            "https://www.oschina.net/action/openapi/token", "https://www.oschina.net/action/openapi/user", null, null),
    /**
     * Pinterest OAuth client.
     */
    PINTEREST(PinterestProvider::new, "https://api.pinterest.com/oauth", "https://api.pinterest.com/v1/oauth/token",
            "https://api.pinterest.com/v1/me", null, null),
    /**
     * Proginn OAuth client.
     */
    PROGINN(ProginnProvider::new, "https://www.proginn.com/oauth2/authorize",
            "https://www.proginn.com/oauth2/access_token", "https://www.proginn.com/openapi/user/basic_info", null,
            null),
    /**
     * QQ OAuth client.
     */
    QQ(QqProvider::new, "https://graph.qq.com/oauth2.0/authorize", "https://graph.qq.com/oauth2.0/token",
            "https://graph.qq.com/user/get_user_info", "https://graph.qq.com/oauth2.0/token", null),
    /**
     * QQ mini-program code exchange client.
     */
    QQ_MINI(QqMiniProvider::new, null, "https://api.q.qq.com/sns/jscode2session", null, null, null),
    /**
     * Renren OAuth client.
     */
    RENREN(RenrenProvider::new, "https://graph.renren.com/oauth/authorize", "https://graph.renren.com/oauth/token",
            "https://api.renren.com/v2/user/get", "https://graph.renren.com/oauth/token", null),
    /**
     * Rednote marketing OAuth client.
     */
    REDNOTE_MARKET(RednoteMarketiProvider::new, "https://ad-market.xiaohongshu.com/auth",
            "https://adapi.xiaohongshu.com/api/open/oauth2/access_token", null,
            "https://adapi.xiaohongshu.com/api/open/oauth2/refresh_token", null),
    /**
     * Slack OAuth client.
     */
    SLACK(SlackProvider::new, "https://slack.com/oauth/v2/authorize", "https://slack.com/api/oauth.v2.access",
            "https://slack.com/api/users.info", null, "https://slack.com/api/auth.revoke"),
    /**
     * Stack Overflow OAuth client.
     */
    STACK_OVERFLOW(StackOverflowProvider::new, "https://stackoverflow.com/oauth",
            "https://stackoverflow.com/oauth/access_token/json", "https://api.stackexchange.com/2.2/me", null, null),
    /**
     * Taobao OAuth client whose token result is locally derived from its callback.
     */
    TAOBAO(TaobaoProvider::new, "https://oauth.taobao.com/authorize", "https://oauth.taobao.com/token", null, null,
            null),
    /**
     * Teambition OAuth client.
     */
    TEAMBITION(TeambitionProvider::new, "https://account.teambition.com/oauth2/authorize",
            "https://account.teambition.com/oauth2/access_token", "https://api.teambition.com/users/me",
            "https://account.teambition.com/oauth2/refresh_token", null),
    /**
     * Toutiao OAuth client.
     */
    TOUTIAO(ToutiaoProvider::new, "https://open.snssdk.com/auth/authorize", "https://open.snssdk.com/auth/token",
            "https://open.snssdk.com/data/user_profile", null, null),
    /**
     * Twitter OAuth client.
     */
    TWITTER(TwitterProvider::new, "https://api.twitter.com/oauth/authenticate",
            "https://api.twitter.com/oauth/access_token", "https://api.twitter.com/1.1/account/verify_credentials.json",
            null, null),
    /**
     * VK OAuth client.
     */
    VK(VKProvider::new, "https://id.vk.com/authorize", "https://id.vk.com/oauth2/auth",
            "https://id.vk.com/oauth2/user_info", "https://id.vk.com/oauth2/auth", "https://id.vk.com/oauth2/revoke"),
    /**
     * WeChat Work QR-code client.
     */
    WECHAT_EE(WeChatEeQrcodeProvider::new, "https://login.work.weixin.qq.com/wwlogin/sso/login",
            "https://qyapi.weixin.qq.com/cgi-bin/gettoken", "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo",
            null, null),
    /**
     * WeChat Work third-party QR-code client.
     */
    WECHAT_EE_QRCODE(WeChatEeThirdQrcodeProvider::new, "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect",
            "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token",
            "https://qyapi.weixin.qq.com/cgi-bin/service/get_login_info", null, null),
    /**
     * WeChat Work web client.
     */
    WECHAT_EE_WEB(WeChatEeWebProvider::new, "https://open.weixin.qq.com/connect/oauth2/authorize",
            "https://qyapi.weixin.qq.com/cgi-bin/gettoken", "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo",
            null, null),
    /**
     * WeChat official-account OAuth client.
     */
    WECHAT_MP(WeChatMpProvider::new, "https://open.weixin.qq.com/connect/oauth2/authorize",
            "https://api.weixin.qq.com/sns/oauth2/access_token", "https://api.weixin.qq.com/sns/userinfo",
            "https://api.weixin.qq.com/sns/oauth2/refresh_token", null),
    /**
     * WeChat mini-program code exchange client.
     */
    WECHAT_MINI(WeChatMiniProvider::new, null, "https://api.weixin.qq.com/sns/jscode2session", null, null, null),
    /**
     * WeChat open-platform OAuth client.
     */
    WECHAT_OPEN(WeChatOpenProvider::new, "https://open.weixin.qq.com/connect/qrconnect",
            "https://api.weixin.qq.com/sns/oauth2/access_token", "https://api.weixin.qq.com/sns/userinfo",
            "https://api.weixin.qq.com/sns/oauth2/refresh_token", null),
    /**
     * Weibo OAuth client.
     */
    WEIBO(WeiboProvider::new, "https://api.weibo.com/oauth2/authorize", "https://api.weibo.com/oauth2/access_token",
            "https://api.weibo.com/2/users/show.json", null, "https://api.weibo.com/oauth2/revokeOIDC"),
    /**
     * Ximalaya OAuth client.
     */
    XIMALAYA(XimalayaProvider::new, "https://api.ximalaya.com/oauth2/js/authorize",
            "https://api.ximalaya.com/oauth2/v2/access_token", "https://api.ximalaya.com/profile/user_info", null,
            null);

    /**
     * Immutable complete catalog set in declaration order.
     */
    private static final Set<BuiltinVendors> ALL = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values())));

    /**
     * Immutable concrete endpoint map for this vendor.
     */
    private final Map<VendorEndpoint, Endpoint> endpoints;

    /**
     * Typed provider factory paired with this vendor constant.
     */
    private final Provider.Factory<VendorConfiguration, ? extends VendorProvider> factory;

    /**
     * Creates one immutable catalog entry.
     *
     * @param factory   provider factory paired one-to-one with this constant
     * @param authorize concrete authorization endpoint, or null
     * @param token     concrete token endpoint, or null
     * @param userInfo  concrete user-information endpoint, or null
     * @param refresh   concrete refresh endpoint, or null
     * @param revoke    concrete revocation endpoint, or null
     */
    BuiltinVendors(final Provider.Factory<VendorConfiguration, ? extends VendorProvider> factory,
            final String authorize, final String token, final String userInfo, final String refresh,
            final String revoke) {
        this.factory = Objects.requireNonNull(factory, "Vendor factory must not be null");
        final Map<VendorEndpoint, Endpoint> values = new LinkedHashMap<>();
        add(values, VendorEndpoint.AUTHORIZE, authorize);
        add(values, VendorEndpoint.TOKEN, token);
        add(values, VendorEndpoint.USERINFO, userInfo);
        add(values, VendorEndpoint.REFRESH, refresh);
        add(values, VendorEndpoint.REVOKE, revoke);
        this.endpoints = Map.copyOf(values);
    }

    /**
     * Resolves a catalog entry by case-insensitive enum name.
     *
     * @param name non-null vendor name
     * @return matching catalog entry
     * @throws IllegalArgumentException if the name is unknown
     */
    public static BuiltinVendors require(final String name) {
        final String expected = Objects.requireNonNull(name, "Vendor name must not be null");
        for (BuiltinVendors vendor : values()) {
            if (vendor.name().equalsIgnoreCase(expected)) {
                return vendor;
            }
        }
        throw new IllegalArgumentException("Unsupported vendor: " + expected);
    }

    /**
     * Resolves one exact catalog name or the complete catalog.
     *
     * @param name exact enum name, or case-insensitive {@code all}
     * @return immutable matching set
     */
    public static Set<BuiltinVendors> from(final String name) {
        final String expected = Objects.requireNonNull(name, "Vendor name must not be null");
        if ("all".equalsIgnoreCase(expected)) {
            return ALL;
        }
        try {
            return Set.of(valueOf(expected.toUpperCase(Locale.ROOT)));
        } catch (final IllegalArgumentException ignored) {
            return Set.of();
        }
    }

    /**
     * Resolves multiple exact catalog names or the complete catalog.
     *
     * @param names non-null names; {@code all} returns the complete catalog
     * @return immutable matching set in request order
     */
    public static Set<BuiltinVendors> from(final List<String> names) {
        final LinkedHashSet<BuiltinVendors> result = new LinkedHashSet<>();
        for (String name : List.copyOf(Objects.requireNonNull(names, "Vendor names must not be null"))) {
            if ("all".equalsIgnoreCase(name)) {
                return ALL;
            }
            result.addAll(from(name));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Adds one concrete endpoint when an address exists.
     *
     * @param endpoints mutable constructor-owned endpoint map
     * @param role      endpoint role
     * @param address   concrete address, or null
     */
    private static void add(
            final Map<VendorEndpoint, Endpoint> endpoints,
            final VendorEndpoint role,
            final String address) {
        if (address != null) {
            endpoints.put(role, Endpoint.of(Protocol.OIDC, address));
        }
    }

    /**
     * Returns the immutable concrete endpoint map.
     *
     * @return non-null endpoint snapshot without empty or template addresses
     */
    @Override
    public Map<VendorEndpoint, Endpoint> endpoints() {
        return endpoints;
    }

    /**
     * Returns the wire protocol shared by built-in vendor clients.
     *
     * @return OpenID Connect protocol family
     */
    @Override
    public Protocol protocol() {
        return Protocol.OIDC;
    }

    /**
     * Returns the provider factory paired with this exact constant.
     *
     * @return non-null typed provider factory
     */
    @Override
    public Provider.Factory<VendorConfiguration, ? extends VendorProvider> factory() {
        return factory;
    }

}
