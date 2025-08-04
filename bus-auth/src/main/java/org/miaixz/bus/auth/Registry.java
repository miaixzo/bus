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

/**
 * 内置的各协议需要的配置，用枚举类分平台类型管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry implements Complex {

    AFDIAN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://afdian.com/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://afdian.com/api/oauth2/access_token");
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
    ALIPAY {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm");
            map.put(Endpoint.ACCESS_TOKEN, "https://openapi.alipay.com/gateway.do");
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
    ALIYUN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://signin.aliyun.com/oauth2/v1/auth");
            map.put(Endpoint.ACCESS_TOKEN, "https://oauth.aliyun.com/v1/token");
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
    AMAZON {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.amazon.com/ap/oa");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.amazon.com/auth/o2/token");
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
    APPLE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://appleid.apple.com/auth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://appleid.apple.com/auth/token");
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
    BAIDU {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openapi.baidu.com/oauth/2.0/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://openapi.baidu.com/oauth/2.0/token");
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
    CODING {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://%s.coding.net/oauth_authorize.html");
            map.put(Endpoint.ACCESS_TOKEN, "https://%s.coding.net/api/oauth/access_token");
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
    DINGTALK {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.dingtalk.com/oauth2/challenge.htm");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.dingtalk.com/v1.0/OIDC/userAccessToken");
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
    DINGTALK_ACCOUNT {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oapi.dingtalk.com/connect/oauth2/sns_authorize");
            map.put(Endpoint.ACCESS_TOKEN, DINGTALK.accessToken());
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
    DOUYIN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.douyin.com/platform/oauth/connect");
            map.put(Endpoint.ACCESS_TOKEN, "https://open.douyin.com/oauth/access_token/");
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
    DOUYIN_MINI {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            // 参见
            // https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/api/open-capacity/log-in/tt-login
            map.put(Endpoint.AUTHORIZE, Normal.EMPTY);
            // 参见 https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/server/log-in/code-2-session
            map.put(Endpoint.ACCESS_TOKEN, "https://minigame.zijieapi.com/mgplatform/api/apps/jscode2session");
            // 参见
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
    ELEME {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open-api.shop.ele.me/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://open-api.shop.ele.me/token");
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
    FACEBOOK {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.facebook.com/v18.0/dialog/oauth");
            map.put(Endpoint.ACCESS_TOKEN, "https://graph.facebook.com/v18.0/oauth/access_token");
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
    FEISHU {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.feishu.cn/open-apis/authen/v1/index");
            map.put(Endpoint.ACCESS_TOKEN, "https://open.feishu.cn/open-apis/authen/v1/access_token");
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
    FIGMA {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.figma.com/oauth");
            map.put(Endpoint.ACCESS_TOKEN, "https://www.figma.com/api/oauth/token");
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
    GITEE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://gitee.com/oauth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://gitee.com/oauth/token");
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
    GITHUB {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://github.com/login/oauth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://github.com/login/oauth/access_token");
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
    GITLAB {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://gitlab.com/oauth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://gitlab.com/oauth/token");
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
    GOOGLE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://accounts.google.com/o/oauth2/v2/auth");
            map.put(Endpoint.ACCESS_TOKEN, "https://oauth2.googleapis.com/token");
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
    HUAWEI {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://oauth-login.cloud.huawei.com/oauth2/v3/token");
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
    JD {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open-oauth.jd.com/oauth2/to_login");
            map.put(Endpoint.ACCESS_TOKEN, "https://open-oauth.jd.com/oauth2/access_token");
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
    KUJIALE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth.kujiale.com/oauth2/show");
            map.put(Endpoint.ACCESS_TOKEN, "https://oauth.kujiale.com/oauth2/auth/token");
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
    LINE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://access.line.me/oauth2/v2.1/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.line.me/oauth2/v2.1/token");
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
    LINKEDIN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.linkedin.com/oauth/v2/authorization");
            map.put(Endpoint.ACCESS_TOKEN, "https://www.linkedin.com/oauth/v2/accessToken");
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
    MEITUAN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://openapi.waimai.meituan.com/oauth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://openapi.waimai.meituan.com/oauth/access_token");
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
    MI {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://account.xiaomi.com/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://account.xiaomi.com/OIDC/token");
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
    MICROSOFT {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://login.microsoftonline.com/%s/oauth2/v2.0/token");
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
    MICROSOFT_CN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token");
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
    OKTA {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://%s.okta.com/oauth2/%s/v1/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://%s.okta.com/oauth2/%s/v1/token");
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
    OSCHINA {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.oschina.net/action/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://www.oschina.net/action/openapi/token");
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
    PINTEREST {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.pinterest.com/oauth");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.pinterest.com/v1/oauth/token");
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
    PROGINN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://www.proginn.com/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://www.proginn.com/oauth2/access_token");
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
    QQ {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://graph.qq.com/oauth2.0/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://graph.qq.com/oauth2.0/token");
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
    QQ_MINI {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.ACCESS_TOKEN, "https://api.q.qq.com/sns/jscode2session");
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
    RENREN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://graph.renren.com/oauth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://graph.renren.com/oauth/token");
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
    REDNOTE_MARKET {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://ad-market.xiaohongshu.com/auth");
            map.put(Endpoint.ACCESS_TOKEN, "https://adapi.xiaohongshu.com/api/open/oauth2/access_token");
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
    SLACK {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://slack.com/oauth/v2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://slack.com/api/oauth.v2.access");
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
    STACK_OVERFLOW {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://stackoverflow.com/oauth");
            map.put(Endpoint.ACCESS_TOKEN, "https://stackoverflow.com/oauth/access_token/json");
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
    TAOBAO {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://oauth.taobao.com/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://oauth.taobao.com/token");
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
    TEAMBITION {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://account.teambition.com/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://account.teambition.com/oauth2/access_token");
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
    TOUTIAO {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.snssdk.com/auth/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://open.snssdk.com/auth/token");
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
    TWITTER {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.twitter.com/oauth/authenticate");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.twitter.com/oauth/access_token");
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
    VK {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://id.vk.com/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://id.vk.com/oauth2/auth");
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
    WECHAT_EE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://login.work.weixin.qq.com/wwlogin/sso/login");
            map.put(Endpoint.ACCESS_TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
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
    WECHAT_EE_QRCODE {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect");
            map.put(Endpoint.ACCESS_TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token");
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
    WECHAT_EE_WEB {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
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
    WECHAT_MP {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
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
    WECHAT_MINI {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.ACCESS_TOKEN, "https://api.weixin.qq.com/sns/jscode2session");
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
    WECHAT_OPEN {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://open.weixin.qq.com/connect/qrconnect");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
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
    WEIBO {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.weibo.com/oauth2/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.weibo.com/oauth2/access_token");
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
    XIMALAYA {
        @Override
        public Map<Endpoint, String> endpoint() {
            Map<Endpoint, String> map = new HashMap<>();
            map.put(Endpoint.AUTHORIZE, "https://api.ximalaya.com/oauth2/js/authorize");
            map.put(Endpoint.ACCESS_TOKEN, "https://api.ximalaya.com/oauth2/v2/access_token");
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

    public static Registry require(String name) {
        for (Registry registry : Registry.values()) {
            if (registry.name().equalsIgnoreCase(name)) {
                return registry;
            }
        }
        throw new IllegalArgumentException("Unsupported type for " + name);
    }

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