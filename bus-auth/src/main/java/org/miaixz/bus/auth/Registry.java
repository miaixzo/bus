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
import org.miaixz.bus.auth.nimble.renren.RenrenProvider;
import org.miaixz.bus.auth.nimble.slack.SlackProvider;
import org.miaixz.bus.auth.nimble.stackoverflow.StackOverflowProvider;
import org.miaixz.bus.auth.nimble.taobao.TaobaoProvider;
import org.miaixz.bus.auth.nimble.teambition.TeambitionProvider;
import org.miaixz.bus.auth.nimble.toutiao.ToutiaoProvider;
import org.miaixz.bus.auth.nimble.twitter.TwitterProvider;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://afdian.com/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://afdian.com/api/oauth2/access_token");
            config.put(Builder.USERINFO, Normal.EMPTY);
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm");
            config.put(Builder.ACCESSTOKEN, "https://openapi.alipay.com/gateway.do");
            config.put(Builder.USERINFO, "https://openapi.alipay.com/gateway.do");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://signin.aliyun.com/oauth2/v1/auth");
            config.put(Builder.ACCESSTOKEN, "https://oauth.aliyun.com/v1/token");
            config.put(Builder.USERINFO, "https://oauth.aliyun.com/v1/userinfo");
            config.put(Builder.REFRESH, "https://oauth.aliyun.com/v1/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.amazon.com/ap/oa");
            config.put(Builder.ACCESSTOKEN, "https://api.amazon.com/auth/o2/token");
            config.put(Builder.USERINFO, "https://api.amazon.com/user/profile");
            config.put(Builder.REFRESH, "https://api.amazon.com/auth/o2/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://appleid.apple.com/auth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://appleid.apple.com/auth/token");
            config.put(Builder.USERINFO, "");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://openapi.baidu.com/oauth/2.0/authorize");
            config.put(Builder.ACCESSTOKEN, "https://openapi.baidu.com/oauth/2.0/token");
            config.put(Builder.USERINFO, "https://openapi.baidu.com/rest/2.0/passport/users/getInfo");
            config.put(Builder.REVOKE, "https://openapi.baidu.com/rest/2.0/passport/auth/revokeAuthorization");
            config.put(Builder.REFRESH, "https://openapi.baidu.com/oauth/2.0/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://%s.coding.net/oauth_authorize.html");
            config.put(Builder.ACCESSTOKEN, "https://%s.coding.net/api/oauth/access_token");
            config.put(Builder.USERINFO, "https://%s.coding.net/api/account/current_user");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://login.dingtalk.com/oauth2/challenge.htm");
            config.put(Builder.ACCESSTOKEN, "https://api.dingtalk.com/v1.0/OIDC/userAccessToken");
            config.put(Builder.USERINFO, "https://api.dingtalk.com/v1.0/contact/users/me");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://oapi.dingtalk.com/connect/oauth2/sns_authorize");
            config.put(Builder.ACCESSTOKEN, DINGTALK.getConfig().get(Builder.ACCESSTOKEN));
            config.put(Builder.USERINFO, DINGTALK.getConfig().get(Builder.USERINFO));
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.douyin.com/platform/oauth/connect");
            config.put(Builder.ACCESSTOKEN, "https://open.douyin.com/oauth/access_token/");
            config.put(Builder.USERINFO, "https://open.douyin.com/oauth/userinfo/");
            config.put(Builder.REFRESH, "https://open.douyin.com/oauth/refresh_token/");
            return config;
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
    ELEME {
        @Override
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open-api.shop.ele.me/authorize");
            config.put(Builder.ACCESSTOKEN, "https://open-api.shop.ele.me/token");
            config.put(Builder.USERINFO, "https://open-api.shop.ele.me/api/v1/");
            config.put(Builder.REFRESH, "https://open-api.shop.ele.me/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.facebook.com/v18.0/dialog/oauth");
            config.put(Builder.ACCESSTOKEN, "https://graph.facebook.com/v18.0/oauth/access_token");
            config.put(Builder.USERINFO, "https://graph.facebook.com/v18.0/me");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.feishu.cn/open-apis/authen/v1/index");
            config.put(Builder.ACCESSTOKEN, "https://open.feishu.cn/open-apis/authen/v1/access_token");
            config.put(Builder.USERINFO, "https://open.feishu.cn/open-apis/authen/v1/user_info");
            config.put(Builder.REFRESH, "https://open.feishu.cn/open-apis/authen/v1/refresh_access_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.figma.com/oauth");
            config.put(Builder.ACCESSTOKEN, "https://www.figma.com/api/oauth/token");
            config.put(Builder.USERINFO, "https://api.figma.com/v1/me");
            config.put(Builder.REFRESH, "https://www.figma.com/api/oauth/refresh");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://gitee.com/oauth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://gitee.com/oauth/token");
            config.put(Builder.USERINFO, "https://gitee.com/api/v5/user");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://github.com/login/oauth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://github.com/login/oauth/access_token");
            config.put(Builder.USERINFO, "https://api.github.com/user");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://gitlab.com/oauth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://gitlab.com/oauth/token");
            config.put(Builder.USERINFO, "https://gitlab.com/api/v4/user");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://accounts.google.com/o/oauth2/v2/auth");
            config.put(Builder.ACCESSTOKEN, "https://oauth2.googleapis.com/token");
            config.put(Builder.USERINFO, "https://openidconnect.googleapis.com/v1/userinfo");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize");
            config.put(Builder.ACCESSTOKEN, "https://oauth-login.cloud.huawei.com/oauth2/v3/token");
            config.put(Builder.USERINFO, "https://account.cloud.huawei.com/rest.php");
            config.put(Builder.REFRESH, "https://oauth-login.cloud.huawei.com/oauth2/v3/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open-oauth.jd.com/oauth2/to_login");
            config.put(Builder.ACCESSTOKEN, "https://open-oauth.jd.com/oauth2/access_token");
            config.put(Builder.USERINFO, "https://api.jd.com/routerjson");
            config.put(Builder.REFRESH, "https://open-oauth.jd.com/OIDC/refresh_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://oauth.kujiale.com/oauth2/show");
            config.put(Builder.ACCESSTOKEN, "https://oauth.kujiale.com/oauth2/auth/token");
            config.put(Builder.USERINFO, "https://oauth.kujiale.com/oauth2/openapi/user");
            config.put(Builder.REFRESH, "https://oauth.kujiale.com/oauth2/auth/token/refresh");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://access.line.me/oauth2/v2.1/authorize");
            config.put(Builder.ACCESSTOKEN, "https://api.line.me/oauth2/v2.1/token");
            config.put(Builder.USERINFO, "https://api.line.me/v2/profile");
            config.put(Builder.REFRESH, "https://api.line.me/oauth2/v2.1/token");
            config.put(Builder.REVOKE, "https://api.line.me/oauth2/v2.1/revoke");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.linkedin.com/oauth/v2/authorization");
            config.put(Builder.ACCESSTOKEN, "https://www.linkedin.com/oauth/v2/accessToken");
            config.put(Builder.USERINFO, "https://api.linkedin.com/v2/me");
            config.put(Builder.REFRESH, "https://www.linkedin.com/oauth/v2/accessToken");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://openapi.waimai.meituan.com/oauth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://openapi.waimai.meituan.com/oauth/access_token");
            config.put(Builder.USERINFO, "https://openapi.waimai.meituan.com/oauth/userinfo");
            config.put(Builder.REFRESH, "https://openapi.waimai.meituan.com/oauth/refresh_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://account.xiaomi.com/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://account.xiaomi.com/OIDC/token");
            config.put(Builder.USERINFO, "https://open.account.xiaomi.com/user/profile");
            config.put(Builder.REFRESH, "https://account.xiaomi.com/OIDC/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize");
            config.put(Builder.ACCESSTOKEN, "https://login.microsoftonline.com/%s/oauth2/v2.0/token");
            config.put(Builder.USERINFO, "https://graph.microsoft.com/v1.0/me");
            config.put(Builder.REFRESH, "https://login.microsoftonline.com/%s/oauth2/v2.0/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/authorize");
            config.put(Builder.ACCESSTOKEN, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token");
            config.put(Builder.USERINFO, "https://microsoftgraph.chinacloudapi.cn/v1.0/me");
            config.put(Builder.REFRESH, "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://%s.okta.com/oauth2/%s/v1/authorize");
            config.put(Builder.ACCESSTOKEN, "https://%s.okta.com/oauth2/%s/v1/token");
            config.put(Builder.USERINFO, "https://%s.okta.com/oauth2/%s/v1/userinfo");
            config.put(Builder.REFRESH, "https://%s.okta.com/oauth2/%s/v1/token");
            config.put(Builder.REVOKE, "https://%s.okta.com/oauth2/%s/v1/revoke");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.oschina.net/action/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://www.oschina.net/action/openapi/token");
            config.put(Builder.USERINFO, "https://www.oschina.net/action/openapi/user");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://api.pinterest.com/oauth");
            config.put(Builder.ACCESSTOKEN, "https://api.pinterest.com/v1/oauth/token");
            config.put(Builder.USERINFO, "https://api.pinterest.com/v1/me");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://www.proginn.com/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://www.proginn.com/oauth2/access_token");
            config.put(Builder.USERINFO, "https://www.proginn.com/openapi/user/basic_info");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://graph.qq.com/oauth2.0/authorize");
            config.put(Builder.ACCESSTOKEN, "https://graph.qq.com/oauth2.0/token");
            config.put(Builder.USERINFO, "https://graph.qq.com/user/get_user_info");
            config.put(Builder.REFRESH, "https://graph.qq.com/oauth2.0/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.ACCESSTOKEN, "https://api.q.qq.com/sns/jscode2session");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://graph.renren.com/oauth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://graph.renren.com/oauth/token");
            config.put(Builder.USERINFO, "https://api.renren.com/v2/user/get");
            config.put(Builder.REFRESH, "https://graph.renren.com/oauth/token");
            return config;
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
    SLACK {
        @Override
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://slack.com/oauth/v2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://slack.com/api/oauth.v2.access");
            config.put(Builder.USERINFO, "https://slack.com/api/users.info");
            config.put(Builder.REVOKE, "https://slack.com/api/auth.revoke");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://stackoverflow.com/oauth");
            config.put(Builder.ACCESSTOKEN, "https://stackoverflow.com/oauth/access_token/json");
            config.put(Builder.USERINFO, "https://api.stackexchange.com/2.2/me");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://oauth.taobao.com/authorize");
            config.put(Builder.ACCESSTOKEN, "https://oauth.taobao.com/token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://account.teambition.com/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://account.teambition.com/oauth2/access_token");
            config.put(Builder.USERINFO, "https://api.teambition.com/users/me");
            config.put(Builder.REFRESH, "https://account.teambition.com/oauth2/refresh_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.snssdk.com/auth/authorize");
            config.put(Builder.ACCESSTOKEN, "https://open.snssdk.com/auth/token");
            config.put(Builder.USERINFO, "https://open.snssdk.com/data/user_profile");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://api.twitter.com/oauth/authenticate");
            config.put(Builder.ACCESSTOKEN, "https://api.twitter.com/oauth/access_token");
            config.put(Builder.USERINFO, "https://api.twitter.com/1.1/account/verify_credentials.json");
            return config;
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
    WECHAT_EE {
        @Override
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://login.work.weixin.qq.com/wwlogin/sso/login");
            config.put(Builder.ACCESSTOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
            config.put(Builder.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect");
            config.put(Builder.ACCESSTOKEN, "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token");
            config.put(Builder.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/service/get_login_info");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://qyapi.weixin.qq.com/cgi-bin/gettoken");
            config.put(Builder.USERINFO, "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.weixin.qq.com/connect/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
            config.put(Builder.USERINFO, "https://api.weixin.qq.com/sns/userinfo");
            config.put(Builder.REFRESH, "https://api.weixin.qq.com/sns/oauth2/refresh_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.ACCESSTOKEN, "https://api.weixin.qq.com/sns/jscode2session");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://open.weixin.qq.com/connect/qrconnect");
            config.put(Builder.ACCESSTOKEN, "https://api.weixin.qq.com/sns/oauth2/access_token");
            config.put(Builder.USERINFO, "https://api.weixin.qq.com/sns/userinfo");
            config.put(Builder.REFRESH, "https://api.weixin.qq.com/sns/oauth2/refresh_token");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://api.weibo.com/oauth2/authorize");
            config.put(Builder.ACCESSTOKEN, "https://api.weibo.com/oauth2/access_token");
            config.put(Builder.USERINFO, "https://api.weibo.com/2/users/show.json");
            config.put(Builder.REVOKE, "https://api.weibo.com/oauth2/revokeOIDC");
            return config;
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
        public Map<String, String> getConfig() {
            Map<String, String> config = new HashMap<>();
            config.put(Builder.AUTHORIZE, "https://api.ximalaya.com/oauth2/js/authorize");
            config.put(Builder.ACCESSTOKEN, "https://api.ximalaya.com/oauth2/v2/access_token");
            config.put(Builder.USERINFO, "https://api.ximalaya.com/profile/user_info");
            return config;
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