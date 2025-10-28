/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth;

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.auth.nimble.alipay.AlipayProvider;
import org.miaixz.bus.auth.nimble.aliyun.AliyunProvider;
import org.miaixz.bus.auth.nimble.amazon.AmazonProvider;
import org.miaixz.bus.auth.nimble.apple.AppleProvider;
import org.miaixz.bus.auth.nimble.baidu.BaiduProvider;
import org.miaixz.bus.auth.nimble.coding.CodingProvider;
import org.miaixz.bus.auth.nimble.dingtalk.DingTalkAccountProvider;
import org.miaixz.bus.auth.nimble.dingtalk.DingTalkProvider;
import org.miaixz.bus.auth.nimble.douyin.DouyinMiniProvider;
import org.miaixz.bus.auth.nimble.douyin.DouyinProvider;
import org.miaixz.bus.auth.nimble.eleme.ElemeProvider;
import org.miaixz.bus.auth.nimble.facebook.FacebookProvider;
import org.miaixz.bus.auth.nimble.feishu.FeishuProvider;
import org.miaixz.bus.auth.nimble.figma.FigmaProvider;
import org.miaixz.bus.auth.nimble.gitee.GiteeProvider;
import org.miaixz.bus.auth.nimble.github.GithubProvider;
import org.miaixz.bus.auth.nimble.gitlab.GitlabProvider;
import org.miaixz.bus.auth.nimble.google.GoogleProvider;
import org.miaixz.bus.auth.nimble.huawei.HuaweiProvider;
import org.miaixz.bus.auth.nimble.jd.JdProvider;
import org.miaixz.bus.auth.nimble.kujiale.KujialeProvider;
import org.miaixz.bus.auth.nimble.line.LineProvider;
import org.miaixz.bus.auth.nimble.linkedin.LinkedinProvider;
import org.miaixz.bus.auth.nimble.meituan.MeituanProvider;
import org.miaixz.bus.auth.nimble.mi.MiProvider;
import org.miaixz.bus.auth.nimble.microsoft.MicrosoftCnProvider;
import org.miaixz.bus.auth.nimble.microsoft.MicrosoftProvider;
import org.miaixz.bus.auth.nimble.oidc.OIDCProvider;
import org.miaixz.bus.auth.nimble.okta.OktaProvider;
import org.miaixz.bus.auth.nimble.oschina.OschinaProvider;
import org.miaixz.bus.auth.nimble.pinterest.PinterestProvider;
import org.miaixz.bus.auth.nimble.proginn.ProginnProvider;
import org.miaixz.bus.auth.nimble.qq.QqMiniProvider;
import org.miaixz.bus.auth.nimble.qq.QqProvider;
import org.miaixz.bus.auth.nimble.rednote.RednoteMarketiProvider;
import org.miaixz.bus.auth.nimble.renren.RenrenProvider;
import org.miaixz.bus.auth.nimble.slack.SlackProvider;
import org.miaixz.bus.auth.nimble.stackoverflow.StackOverflowProvider;
import org.miaixz.bus.auth.nimble.taobao.TaobaoProvider;
import org.miaixz.bus.auth.nimble.teambition.TeambitionProvider;
import org.miaixz.bus.auth.nimble.toutiao.ToutiaoProvider;
import org.miaixz.bus.auth.nimble.twitter.TwitterProvider;
import org.miaixz.bus.auth.nimble.vk.VKProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeQrcodeProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeThirdQrcodeProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeWebProvider;
import org.miaixz.bus.auth.nimble.wechat.mini.WeChatMiniProvider;
import org.miaixz.bus.auth.nimble.wechat.mp.WeChatMpProvider;
import org.miaixz.bus.auth.nimble.wechat.open.WeChatOpenProvider;
import org.miaixz.bus.auth.nimble.weibo.WeiboProvider;
import org.miaixz.bus.auth.nimble.ximalaya.XimalayaProvider;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Built-in configurations required by various protocols, managed by platform type using an enum.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry implements Complex {

    /**
     * AfDian platform registry.
     */
    AFDIAN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://afdian.com/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://afdian.com/api/oauth2/access_token");
            map.put(Endpoint.USERINFO, Normal.EMPTY);
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return ProginnProvider.class;
        }
    },
    /**
     * Alipay platform registry.
     */
    ALIPAY {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm");
            map.put(Endpoint.TOKEN, "https://openapi.alipay.com/gateway.do");
            map.put(Endpoint.USERINFO, "https://openapi.alipay.com/gateway.do");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AlipayProvider.class;
        }
    },
    /**
     * Aliyun platform registry.
     */
    ALIYUN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://signin.aliyun.com/oauth2/v1/auth");
            map.put(Endpoint.TOKEN, "https://oauth.aliyun.com/v1/token");
            map.put(Endpoint.USERINFO, "https://oauth.aliyun.com/v1/userinfo");
            map.put(Endpoint.REFRESH, "https://oauth.aliyun.com/v1/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AliyunProvider.class;
        }
    },
    /**
     * Amazon platform registry.
     */
    AMAZON {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.amazon.com/ap/oa");
            map.put(Endpoint.TOKEN, "https://api.amazon.com/auth/o2/token");
            map.put(Endpoint.USERINFO, "https://api.amazon.com/user/profile");
            map.put(Endpoint.REFRESH, "https://api.amazon.com/auth/o2/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AmazonProvider.class;
        }
    },
    /**
     * Apple platform registry.
     */
    APPLE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://appleid.apple.com/auth/authorize");
            map.put(Endpoint.TOKEN, "https://appleid.apple.com/auth/token");
            map.put(Endpoint.USERINFO, "");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AppleProvider.class;
        }
    },
    /**
     * Baidu platform registry.
     */
    BAIDU {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openapi.baidu.com/oauth/2.0/authorize");
            map.put(Endpoint.TOKEN, "https://openapi.baidu.com/oauth/2.0/token");
            map.put(Endpoint.USERINFO, "https://openapi.baidu.com/rest/2.0/passport/users/getInfo");
            map.put(Endpoint.REVOKE, "https://openapi.baidu.com/rest/2.0/passport/auth/revokeAuthorization");
            map.put(Endpoint.REFRESH, "https://openapi.baidu.com/oauth/2.0/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return BaiduProvider.class;
        }
    },
    /**
     * Coding platform registry.
     */
    CODING {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://%s.coding.net/oauth_authorize.html");
            map.put(Endpoint.TOKEN, "https://%s.coding.net/api/oauth/access_token");
            map.put(Endpoint.USERINFO, "https://%s.coding.net/api/account/current_user");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return CodingProvider.class;
        }
    },
    /**
     * DingTalk platform registry.
     */
    DINGTALK {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.dingtalk.com/oauth2/challenge.htm");
            map.put(Endpoint.TOKEN, "https://api.dingtalk.com/v1.0/OIDC/userAccessToken");
            map.put(Endpoint.USERINFO, "https://api.dingtalk.com/v1.0/contact/users/me");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return DingTalkProvider.class;
        }
    },
    /**
     * DingTalk Account platform registry.
     */
    DINGTALK_ACCOUNT {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oapi.dingtalk.com/connect/oauth2/sns_authorize");
            map.put(Endpoint.TOKEN, DINGTALK.token());
            map.put(Endpoint.USERINFO, DINGTALK.userinfo());
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return DingTalkAccountProvider.class;
        }
    },
    /**
     * Douyin platform registry.
     */
    DOUYIN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.douyin.com/platform/oauth/connect");
            map.put(Endpoint.TOKEN, "https://open.douyin.com/oauth/access_token/");
            map.put(Endpoint.USERINFO, "https://open.douyin.com/oauth/userinfo/");
            map.put(Endpoint.REFRESH, "https://open.douyin.com/oauth/refresh_token/");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return DouyinProvider.class;
        }
    },
    /**
     * Douyin Mini Program platform registry.
     */
    DOUYIN_MINI {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            // See
            // https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/api/open-capacity/log-in/tt-login
            map.put(Endpoint.AUTHORIZE, Normal.EMPTY);
            // See https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/server/log-in/code-2-session
            map.put(Endpoint.TOKEN, "https://minigame.zijieapi.com/mgplatform/api/apps/jscode2session");
            // See
            // https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/guide/open-api/info/tt-get-user-info
            map.put(Endpoint.USERINFO, Normal.EMPTY);
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return DouyinMiniProvider.class;
        }
    },
    /**
     * Eleme platform registry.
     */
    ELEME {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open-api.shop.ele.me/authorize");
            map.put(Endpoint.TOKEN, "https://open-api.shop.ele.me/token");
            map.put(Endpoint.USERINFO, "https://open-api.shop.ele.me/api/v1/");
            map.put(Endpoint.REFRESH, "https://open-api.shop.ele.me/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return ElemeProvider.class;
        }
    },
    /**
     * Facebook platform registry.
     */
    FACEBOOK {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.facebook.com/v18.0/dialog/oauth");
            map.put(Endpoint.TOKEN, "https://graph.facebook.com/v18.0/oauth/access_token");
            map.put(Endpoint.USERINFO, "https://graph.facebook.com/v18.0/me");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return FacebookProvider.class;
        }
    },
    /**
     * Feishu platform registry.
     */
    FEISHU {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.feishu.cn/open-apis/authen/v1/index");
            map.put(Endpoint.TOKEN, "https://open.feishu.cn/open-apis/authen/v1/access_token");
            map.put(Endpoint.USERINFO, "https://open.feishu.cn/open-apis/authen/v1/user_info");
            map.put(Endpoint.REFRESH, "https://open.feishu.cn/open-apis/authen/v1/refresh_access_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return FeishuProvider.class;
        }
    },
    /**
     * Figma platform registry.
     */
    FIGMA {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.figma.com/oauth");
            map.put(Endpoint.TOKEN, "https://www.figma.com/api/oauth/token");
            map.put(Endpoint.USERINFO, "https://api.figma.com/v1/me");
            map.put(Endpoint.REFRESH, "https://www.figma.com/api/oauth/refresh");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return FigmaProvider.class;
        }
    },
    /**
     * Gitee platform registry.
     */
    GITEE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://gitee.com/oauth/authorize");
            map.put(Endpoint.TOKEN, "https://gitee.com/oauth/token");
            map.put(Endpoint.USERINFO, "https://gitee.com/api/v5/user");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return GiteeProvider.class;
        }
    },
    /**
     * Github platform registry.
     */
    GITHUB {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://github.com/login/oauth/authorize");
            map.put(Endpoint.TOKEN, "https://github.com/login/oauth/access_token");
            map.put(Endpoint.USERINFO, "https://api.github.com/user");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return GithubProvider.class;
        }
    },
    /**
     * Gitlab platform registry.
     */
    GITLAB {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://gitlab.com/oauth/authorize");
            map.put(Endpoint.TOKEN, "https://gitlab.com/oauth/token");
            map.put(Endpoint.USERINFO, "https://gitlab.com/api/v4/user");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return GitlabProvider.class;
        }
    },
    /**
     * Google platform registry.
     */
    GOOGLE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://accounts.google.com/o/oauth2/v2/auth");
            map.put(Endpoint.TOKEN, "https://oauth2.googleapis.com/token");
            map.put(Endpoint.USERINFO, "https://openidconnect.googleapis.com/v1/userinfo");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return GoogleProvider.class;
        }
    },
    /**
     * Huawei platform registry.
     */
    HUAWEI {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize");
            map.put(Endpoint.TOKEN, "https://oauth-login.cloud.huawei.com/oauth2/v3/token");
            map.put(Endpoint.USERINFO, "https://account.cloud.huawei.com/rest.php");
            map.put(Endpoint.REFRESH, "https://oauth-login.cloud.huawei.com/oauth2/v3/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return HuaweiProvider.class;
        }
    },
    /**
     * JD (Jingdong) platform registry.
     */
    JD {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open-oauth.jd.com/oauth2/to_login");
            map.put(Endpoint.TOKEN, "https://open-oauth.jd.com/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://api.jd.com/routerjson");
            map.put(Endpoint.REFRESH, "https://open-oauth.jd.com/OIDC/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return JdProvider.class;
        }
    },
    /**
     * Kujiale platform registry.
     */
    KUJIALE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth.kujiale.com/oauth2/show");
            map.put(Endpoint.TOKEN, "https://oauth.kujiale.com/oauth2/auth/token");
            map.put(Endpoint.USERINFO, "https://oauth.kujiale.com/oauth2/openapi/user");
            map.put(Endpoint.REFRESH, "https://oauth.kujiale.com/oauth2/auth/token/refresh");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return KujialeProvider.class;
        }
    },
    /**
     * LINE platform registry.
     */
    LINE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://access.line.me/oauth2/v2.1/authorize");
            map.put(Endpoint.TOKEN, "https://api.line.me/oauth2/v2.1/token");
            map.put(Endpoint.USERINFO, "https://api.line.me/v2/profile");
            map.put(Endpoint.REFRESH, "https://api.line.me/oauth2/v2.1/token");
            map.put(Endpoint.REVOKE, "https://api.line.me/oauth2/v2.1/revoke");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return LineProvider.class;
        }
    },
    /**
     * LinkedIn platform registry.
     */
    LINKEDIN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.linkedin.com/oauth/v2/authorization");
            map.put(Endpoint.TOKEN, "https://www.linkedin.com/oauth/v2/accessToken");
            map.put(Endpoint.USERINFO, "https://api.linkedin.com/v2/me");
            map.put(Endpoint.REFRESH, "https://www.linkedin.com/oauth/v2/accessToken");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return LinkedinProvider.class;
        }
    },
    /**
     * Meituan platform registry.
     */
    MEITUAN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openapi.waimai.meituan.com/oauth/authorize");
            map.put(Endpoint.TOKEN, "https://openapi.waimai.meituan.com/oauth/access_token");
            map.put(Endpoint.USERINFO, "https://openapi.waimai.meituan.com/oauth/userinfo");
            map.put(Endpoint.REFRESH, "https://openapi.waimai.meituan.com/oauth/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return MeituanProvider.class;
        }
    },
    /**
     * Xiaomi (Mi) platform registry.
     */
    MI {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://account.xiaomi.com/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://account.xiaomi.com/OIDC/token");
            map.put(Endpoint.USERINFO, "https://open.account.xiaomi.com/user/profile");
            map.put(Endpoint.REFRESH, "https://account.xiaomi.com/OIDC/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return MiProvider.class;
        }
    },
    /**
     * Microsoft platform registry.
     */
    MICROSOFT {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize");
            map.put(Endpoint.TOKEN, "https://login.microsoftonline.com/%s/oauth2/v2.0/token");
            map.put(Endpoint.USERINFO, "https://graph.microsoft.com/v1.0/me");
            map.put(Endpoint.REFRESH, "https://login.microsoftonline.com/%s/oauth2/v2.0/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return MicrosoftProvider.class;
        }
    },
    /**
     * Microsoft China platform registry.
     */
    MICROSOFT_CN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/authorize");
            map.put(Endpoint.TOKEN, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token");
            map.put(Endpoint.USERINFO, "https://microsoftgraph.chinacloudapi.cn/v1.0/me");
            map.put(Endpoint.REFRESH, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return MicrosoftCnProvider.class;
        }
    },
    /**
     * OIDC/OAuth2 platform registry. 标准的OIDC协议，
     */
    OIDC {

        @Override
        public Map<Endpoint, String> endpoint() {
            return MapKit.empty();
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return OIDCProvider.class;
        }
    },
    /**
     * Okta platform registry.
     */
    OKTA {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://%s.okta.com/oauth2/%s/v1/authorize");
            map.put(Endpoint.TOKEN, "https://%s.okta.com/oauth2/%s/v1/token");
            map.put(Endpoint.USERINFO, "https://%s.okta.com/oauth2/%s/v1/userinfo");
            map.put(Endpoint.REFRESH, "https://%s.okta.com/oauth2/%s/v1/token");
            map.put(Endpoint.REVOKE, "https://%s.okta.com/oauth2/%s/v1/revoke");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return OktaProvider.class;
        }
    },
    /**
     * OSChina platform registry.
     */
    OSCHINA {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.oschina.net/action/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://www.oschina.net/action/openapi/token");
            map.put(Endpoint.USERINFO, "https://www.oschina.net/action/openapi/user");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return OschinaProvider.class;
        }
    },
    /**
     * Pinterest platform registry.
     */
    PINTEREST {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.pinterest.com/oauth");
            map.put(Endpoint.TOKEN, "https://api.pinterest.com/v1/oauth/token");
            map.put(Endpoint.USERINFO, "https://api.pinterest.com/v1/me");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return PinterestProvider.class;
        }
    },
    /**
     * Proginn platform registry.
     */
    PROGINN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.proginn.com/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://www.proginn.com/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://www.proginn.com/openapi/user/basic_info");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return ProginnProvider.class;
        }
    },
    /**
     * QQ platform registry.
     */
    QQ {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://graph.qq.com/oauth2.0/authorize");
            map.put(Endpoint.TOKEN, "https://graph.qq.com/oauth2.0/token");
            map.put(Endpoint.USERINFO, "https://graph.qq.com/user/get_user_info");
            map.put(Endpoint.REFRESH, "https://graph.qq.com/oauth2.0/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return QqProvider.class;
        }
    },
    /**
     * QQ Mini Program platform registry.
     */
    QQ_MINI {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.TOKEN, "https://api.q.qq.com/sns/jscode2session");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return QqMiniProvider.class;
        }
    },
    /**
     * Renren platform registry.
     */
    RENREN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://graph.renren.com/oauth/authorize");
            map.put(Endpoint.TOKEN, "https://graph.renren.com/oauth/token");
            map.put(Endpoint.USERINFO, "https://api.renren.com/v2/user/get");
            map.put(Endpoint.REFRESH, "https://graph.renren.com/oauth/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return RenrenProvider.class;
        }
    },
    /**
     * Rednote Market platform registry.
     */
    REDNOTE_MARKET {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://ad-market.xiaohongshu.com/auth");
            map.put(Endpoint.TOKEN, "https://adapi.xiaohongshu.com/api/open/oauth2/access_token");
            map.put(Endpoint.USERINFO, Normal.EMPTY);
            map.put(Endpoint.REFRESH, "https://adapi.xiaohongshu.com/api/open/oauth2/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return RednoteMarketiProvider.class;
        }
    },
    /**
     * Slack platform registry.
     */
    SLACK {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://slack.com/oauth/v2/authorize");
            map.put(Endpoint.TOKEN, "https://slack.com/api/oauth.v2.access");
            map.put(Endpoint.USERINFO, "https://slack.com/api/users.info");
            map.put(Endpoint.REVOKE, "https://slack.com/api/auth.revoke");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return SlackProvider.class;
        }
    },
    /**
     * Stack Overflow platform registry.
     */
    STACK_OVERFLOW {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://stackoverflow.com/oauth");
            map.put(Endpoint.TOKEN, "https://stackoverflow.com/oauth/access_token/json");
            map.put(Endpoint.USERINFO, "https://api.stackexchange.com/2.2/me");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return StackOverflowProvider.class;
        }
    },
    /**
     * Taobao platform registry.
     */
    TAOBAO {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth.taobao.com/authorize");
            map.put(Endpoint.TOKEN, "https://oauth.taobao.com/token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return TaobaoProvider.class;
        }
    },
    /**
     * Teambition platform registry.
     */
    TEAMBITION {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://account.teambition.com/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://account.teambition.com/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://api.teambition.com/users/me");
            map.put(Endpoint.REFRESH, "https://account.teambition.com/oauth2/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return TeambitionProvider.class;
        }
    },
    /**
     * Toutiao platform registry.
     */
    TOUTIAO {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.snssdk.com/auth/authorize");
            map.put(Endpoint.TOKEN, "https://open.snssdk.com/auth/token");
            map.put(Endpoint.USERINFO, "https://open.snssdk.com/data/user_profile");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return ToutiaoProvider.class;
        }
    },
    /**
     * Twitter platform registry.
     */
    TWITTER {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.twitter.com/oauth/authenticate");
            map.put(Endpoint.TOKEN, "https://api.twitter.com/oauth/access_token");
            map.put(Endpoint.USERINFO, "https://api.twitter.com/1.1/account/verify_credentials.json");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return TwitterProvider.class;
        }
    },
    /**
     * VK platform registry.
     */
    VK {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://id.vk.com/authorize");
            map.put(Endpoint.TOKEN, "https://id.vk.com/oauth2/auth");
            map.put(Endpoint.USERINFO, "https://id.vk.com/oauth2/user_info");
            map.put(Endpoint.REVOKE, "https://id.vk.com/oauth2/revoke");
            map.put(Endpoint.REFRESH, "https://id.vk.com/oauth2/auth");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return VKProvider.class;
        }
    },
    /**
     * WeChat Enterprise platform registry.
     */
    WECHAT_EE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.work.weixin.qq.com/wwlogin/sso/login");
            map.put(Endpoint.TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
            map.put(Endpoint.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatEeQrcodeProvider.class;
        }
    },
    /**
     * WeChat Enterprise QR Code platform registry.
     */
    WECHAT_EE_QRCODE {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect");
            map.put(Endpoint.TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token");
            map.put(Endpoint.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/service/get_login_info");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatEeThirdQrcodeProvider.class;
        }
    },
    /**
     * WeChat Enterprise Web platform registry.
     */
    WECHAT_EE_WEB {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
            map.put(Endpoint.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatEeWebProvider.class;
        }
    },
    /**
     * WeChat Official Account (MP) platform registry.
     */
    WECHAT_MP {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://api.weixin.qq.com/sns/userinfo");
            map.put(Endpoint.REFRESH, "https://api.weixin.qq.com/sns/oauth2/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatMpProvider.class;
        }
    },
    /**
     * WeChat Mini Program platform registry.
     */
    WECHAT_MINI {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.TOKEN, "https://api.weixin.qq.com/sns/jscode2session");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatMiniProvider.class;
        }
    },
    /**
     * WeChat Open Platform registry.
     */
    WECHAT_OPEN {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/qrconnect");
            map.put(Endpoint.TOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://api.weixin.qq.com/sns/userinfo");
            map.put(Endpoint.REFRESH, "https://api.weixin.qq.com/sns/oauth2/refresh_token");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeChatOpenProvider.class;
        }
    },
    /**
     * Weibo platform registry.
     */
    WEIBO {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.weibo.com/oauth2/authorize");
            map.put(Endpoint.TOKEN, "https://api.weibo.com/oauth2/access_token");
            map.put(Endpoint.USERINFO, "https://api.weibo.com/2/users/show.json");
            map.put(Endpoint.REVOKE, "https://api.weibo.com/oauth2/revokeOIDC");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WeiboProvider.class;
        }
    },
    /**
     * Ximalaya platform registry.
     */
    XIMALAYA {

        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.ximalaya.com/oauth2/js/authorize");
            map.put(Endpoint.TOKEN, "https://api.ximalaya.com/oauth2/v2/access_token");
            map.put(Endpoint.USERINFO, "https://api.ximalaya.com/profile/user_info");
            return map;
        }

        @Override
        public Protocol getProtocol() {
            return Protocol.OIDC;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return XimalayaProvider.class;
        }
    };

    private final static Set<Registry> values = Arrays.stream(Registry.values()).collect(Collectors.toSet());

    /**
     * Retrieves a {@link Registry} enum by its name (case-insensitive).
     *
     * @param name the name of the registry to retrieve
     * @return the matching {@link Registry} enum instance
     * @throws IllegalArgumentException if no registry with the given name is found
     */
    public static Registry require(String name) {
        for (Registry registry : Registry.values()) {
            if (registry.name().equalsIgnoreCase(name)) {
                return registry;
            }
        }
        throw new IllegalArgumentException("Unsupported type for " + name);
    }

    /**
     * Retrieves a set of {@link Registry} enums based on the provided name. If the name is "all", returns all registry
     * values. If the name matches a single registry, returns a singleton set. Otherwise, returns an empty set.
     *
     * @param name the name to match, or "all" for all registries
     * @return a set of matching {@link Registry} enums
     */
    public static Set<Registry> from(String name) {
        if (name.equals("all"))
            return values;
        for (Registry r : Registry.values()) {
            if (r.name().equals(name)) {
                return Collections.singleton(r);
            }
        }
        return Collections.emptySet();
    }

    /**
     * Retrieves a set of {@link Registry} enums based on a list of names. If the list contains "all", returns all
     * registry values. Otherwise, returns a set of registries matching the names in the list.
     *
     * @param list a list of names to match
     * @return a set of matching {@link Registry} enums
     */
    public static Set<Registry> from(List<String> list) {
        Set<Registry> result = new HashSet<>();
        for (String obj : list) {
            if (obj.equals("all")) {
                return values;
            }
            for (Registry r : Registry.values()) {
                if (r.name().equals(obj)) {
                    result.add(r);
                }
            }
        }
        return result;
    }

}
