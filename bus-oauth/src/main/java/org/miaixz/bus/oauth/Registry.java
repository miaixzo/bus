/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth;

import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.metric.DefaultProvider;
import org.miaixz.bus.oauth.metric.alipay.AlipayProvider;
import org.miaixz.bus.oauth.metric.aliyun.AliyunProvider;
import org.miaixz.bus.oauth.metric.amazon.AmazonProvider;
import org.miaixz.bus.oauth.metric.baidu.BaiduProvider;
import org.miaixz.bus.oauth.metric.coding.CodingProvider;
import org.miaixz.bus.oauth.metric.dingtalk.DingTalkAccountProvider;
import org.miaixz.bus.oauth.metric.dingtalk.DingTalkProvider;
import org.miaixz.bus.oauth.metric.douyin.DouyinProvider;
import org.miaixz.bus.oauth.metric.eleme.ElemeProvider;
import org.miaixz.bus.oauth.metric.facebook.FacebookProvider;
import org.miaixz.bus.oauth.metric.feishu.FeishuProvider;
import org.miaixz.bus.oauth.metric.gitee.GiteeProvider;
import org.miaixz.bus.oauth.metric.github.GithubProvider;
import org.miaixz.bus.oauth.metric.gitlab.GitlabProvider;
import org.miaixz.bus.oauth.metric.google.GoogleProvider;
import org.miaixz.bus.oauth.metric.huawei.HuaweiProvider;
import org.miaixz.bus.oauth.metric.jd.JdProvider;
import org.miaixz.bus.oauth.metric.kujiale.KujialeProvider;
import org.miaixz.bus.oauth.metric.line.LineProvider;
import org.miaixz.bus.oauth.metric.linkedin.LinkedinProvider;
import org.miaixz.bus.oauth.metric.meituan.MeituanProvider;
import org.miaixz.bus.oauth.metric.mi.MiProvider;
import org.miaixz.bus.oauth.metric.microsoft.MicrosoftCnProvider;
import org.miaixz.bus.oauth.metric.microsoft.MicrosoftProvider;
import org.miaixz.bus.oauth.metric.okta.OktaProvider;
import org.miaixz.bus.oauth.metric.oschina.OschinaProvider;
import org.miaixz.bus.oauth.metric.pinterest.PinterestProvider;
import org.miaixz.bus.oauth.metric.proginn.ProginnProvider;
import org.miaixz.bus.oauth.metric.qq.QqProvider;
import org.miaixz.bus.oauth.metric.renren.RenrenProvider;
import org.miaixz.bus.oauth.metric.slack.SlackProvider;
import org.miaixz.bus.oauth.metric.stackoverflow.StackOverflowProvider;
import org.miaixz.bus.oauth.metric.taobao.TaobaoProvider;
import org.miaixz.bus.oauth.metric.teambition.TeambitionProvider;
import org.miaixz.bus.oauth.metric.toutiao.ToutiaoProvider;
import org.miaixz.bus.oauth.metric.twitter.TwitterProvider;
import org.miaixz.bus.oauth.metric.wechat.ee.WeChatEeQrcodeProvider;
import org.miaixz.bus.oauth.metric.wechat.ee.WeChatEeThirdQrcodeProvider;
import org.miaixz.bus.oauth.metric.wechat.ee.WeChatEeWebProvider;
import org.miaixz.bus.oauth.metric.wechat.mp.WeChatMpProvider;
import org.miaixz.bus.oauth.metric.wechat.open.WeChatOpenProvider;
import org.miaixz.bus.oauth.metric.weibo.WeiboProvider;
import org.miaixz.bus.oauth.metric.xmly.XmlyProvider;

/**
 * 内置的各api需要的url， 用枚举类分平台类型管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry implements Complex {

    /**
     * 爱发电
     */
    AFDIAN {
        @Override
        public String authorize() {
            return "https://afdian.net/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://afdian.net/api/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return Normal.EMPTY;
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return ProginnProvider.class;
        }
    },
    /**
     * 支付宝
     */
    ALIPAY {
        @Override
        public String authorize() {
            return "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm";
        }

        @Override
        public String accessToken() {
            return "https://openapi.alipay.com/gateway.do";
        }

        @Override
        public String userInfo() {
            return "https://openapi.alipay.com/gateway.do";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return AlipayProvider.class;
        }
    },
    /**
     * 阿里云
     */
    ALIYUN {
        @Override
        public String authorize() {
            return "https://signin.aliyun.com/oauth2/v1/auth";
        }

        @Override
        public String accessToken() {
            return "https://oauth.aliyun.com/v1/token";
        }

        @Override
        public String userInfo() {
            return "https://oauth.aliyun.com/v1/userinfo";
        }

        @Override
        public String refresh() {
            return "https://oauth.aliyun.com/v1/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return AliyunProvider.class;
        }
    },
    /**
     * Amazon
     */
    AMAZON {
        @Override
        public String authorize() {
            return "https://www.amazon.com/ap/oa";
        }

        @Override
        public String accessToken() {
            return "https://api.amazon.com/auth/o2/token";
        }

        @Override
        public String userInfo() {
            return "https://api.amazon.com/user/profile";
        }

        @Override
        public String refresh() {
            return "https://api.amazon.com/auth/o2/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return AmazonProvider.class;
        }
    },
    /**
     * 百度
     */
    BAIDU {
        @Override
        public String authorize() {
            return "https://openapi.baidu.com/oauth/2.0/authorize";
        }

        @Override
        public String accessToken() {
            return "https://openapi.baidu.com/oauth/2.0/token";
        }

        @Override
        public String userInfo() {
            return "https://openapi.baidu.com/rest/2.0/passport/users/getInfo";
        }

        @Override
        public String revoke() {
            return "https://openapi.baidu.com/rest/2.0/passport/auth/revokeAuthorization";
        }

        @Override
        public String refresh() {
            return "https://openapi.baidu.com/oauth/2.0/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return BaiduProvider.class;
        }
    },
    /**
     * Coding
     */
    CODING {
        @Override
        public String authorize() {
            return "https://%s.coding.net/oauth_authorize.html";
        }

        @Override
        public String accessToken() {
            return "https://%s.coding.net/api/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://%s.coding.net/api/account/current_user";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return CodingProvider.class;
        }
    },
    /**
     * 钉钉扫码
     */
    DINGTALK {
        @Override
        public String authorize() {
            return "https://oapi.dingtalk.com/connect/qrconnect";
        }

        @Override
        public String accessToken() {
            throw new AuthorizedException(ErrorCode.UNSUPPORTED.getCode());
        }

        @Override
        public String userInfo() {
            return "https://oapi.dingtalk.com/sns/getuserinfo_bycode";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return DingTalkProvider.class;
        }
    },
    /**
     * 钉钉账号
     */
    DINGTALK_ACCOUNT {
        @Override
        public String authorize() {
            return "https://oapi.dingtalk.com/connect/oauth2/sns_authorize";
        }

        @Override
        public String accessToken() {
            return DINGTALK.accessToken();
        }

        @Override
        public String userInfo() {
            return DINGTALK.userInfo();
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return DingTalkAccountProvider.class;
        }
    },
    /**
     * 抖音
     */
    DOUYIN {
        @Override
        public String authorize() {
            return "https://open.douyin.com/platform/oauth/connect";
        }

        @Override
        public String accessToken() {
            return "https://open.douyin.com/oauth/access_token/";
        }

        @Override
        public String userInfo() {
            return "https://open.douyin.com/oauth/userinfo/";
        }

        @Override
        public String refresh() {
            return "https://open.douyin.com/oauth/refresh_token/";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return DouyinProvider.class;
        }
    },
    /**
     * 饿了么
     */
    ELEME {
        @Override
        public String authorize() {
            return "https://open-api.shop.ele.me/authorize";
        }

        @Override
        public String accessToken() {
            return "https://open-api.shop.ele.me/token";
        }

        @Override
        public String userInfo() {
            return "https://open-api.shop.ele.me/api/v1/";
        }

        @Override
        public String refresh() {
            return "https://open-api.shop.ele.me/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return ElemeProvider.class;
        }
    },
    /**
     * Facebook
     */
    FACEBOOK {
        @Override
        public String authorize() {
            return "https://www.facebook.com/v18.0/dialog/oauth";
        }

        @Override
        public String accessToken() {
            return "https://graph.facebook.com/v18.0/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://graph.facebook.com/v18.0/me";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return FacebookProvider.class;
        }
    },
    /**
     * 飞书
     */
    FEISHU {
        @Override
        public String authorize() {
            return "https://open.feishu.cn/open-apis/authen/v1/index";
        }

        @Override
        public String accessToken() {
            return "https://open.feishu.cn/open-apis/authen/v1/access_token";
        }

        @Override
        public String userInfo() {
            return "https://open.feishu.cn/open-apis/authen/v1/user_info";
        }

        @Override
        public String refresh() {
            return "https://open.feishu.cn/open-apis/authen/v1/refresh_access_token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return FeishuProvider.class;
        }
    },
    /**
     * Gitee
     */
    GITEE {
        @Override
        public String authorize() {
            return "https://gitee.com/oauth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://gitee.com/oauth/token";
        }

        @Override
        public String userInfo() {
            return "https://gitee.com/api/v5/user";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return GiteeProvider.class;
        }
    },

    /**
     * Github
     */
    GITHUB {
        @Override
        public String authorize() {
            return "https://github.com/login/oauth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://github.com/login/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.github.com/user";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return GithubProvider.class;
        }
    },
    /**
     * Gitlab
     */
    GITLAB {
        @Override
        public String authorize() {
            return "https://gitlab.com/oauth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://gitlab.com/oauth/token";
        }

        @Override
        public String userInfo() {
            return "https://gitlab.com/api/v4/user";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return GitlabProvider.class;
        }
    },
    /**
     * Google
     */
    GOOGLE {
        @Override
        public String authorize() {
            return "https://accounts.google.com/o/oauth2/v2/auth";
        }

        @Override
        public String accessToken() {
            return "https://www.googleapis.com/oauth2/v4/token";
        }

        @Override
        public String userInfo() {
            return "https://www.googleapis.com/oauth2/v3/userinfo";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return GoogleProvider.class;
        }
    },
    /**
     * 华为
     */
    HUAWEI {
        @Override
        public String authorize() {
            return "https://oauth-login.cloud.huawei.com/oauth2/v2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://oauth-login.cloud.huawei.com/oauth2/v2/token";
        }

        @Override
        public String userInfo() {
            return "https://api.vmall.com/rest.php";
        }

        @Override
        public String refresh() {
            return "https://oauth-login.cloud.huawei.com/oauth2/v2/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return HuaweiProvider.class;
        }
    },
    /**
     * 京东
     */
    JD {
        @Override
        public String authorize() {
            return "https://open-oauth.jd.com/oauth2/to_login";
        }

        @Override
        public String accessToken() {
            return "https://open-oauth.jd.com/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.jd.com/routerjson";
        }

        @Override
        public String refresh() {
            return "https://open-oauth.jd.com/oauth2/refresh_token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return JdProvider.class;
        }
    },
    /**
     * 酷家乐
     */
    KUJIALE {
        @Override
        public String authorize() {
            return "https://oauth.kujiale.com/oauth2/show";
        }

        @Override
        public String accessToken() {
            return "https://oauth.kujiale.com/oauth2/auth/token";
        }

        @Override
        public String userInfo() {
            return "https://oauth.kujiale.com/oauth2/openapi/user";
        }

        @Override
        public String refresh() {
            return "https://oauth.kujiale.com/oauth2/auth/token/refresh";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return KujialeProvider.class;
        }
    },
    /**
     * line
     */
    LINE {
        @Override
        public String authorize() {
            return "https://access.line.me/oauth2/v2.1/authorize";
        }

        @Override
        public String accessToken() {
            return "https://api.line.me/oauth2/v2.1/token";
        }

        @Override
        public String userInfo() {
            return "https://api.line.me/v2/profile";
        }

        @Override
        public String refresh() {
            return "https://api.line.me/oauth2/v2.1/token";
        }

        @Override
        public String revoke() {
            return "https://api.line.me/oauth2/v2.1/revoke";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return LineProvider.class;
        }
    },
    /**
     * 领英
     */
    LINKEDIN {
        @Override
        public String authorize() {
            return "https://www.linkedin.com/oauth/v2/authorization";
        }

        @Override
        public String accessToken() {
            return "https://www.linkedin.com/oauth/v2/accessToken";
        }

        @Override
        public String userInfo() {
            return "https://api.linkedin.com/v2/me";
        }

        @Override
        public String refresh() {
            return "https://www.linkedin.com/oauth/v2/accessToken";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return LinkedinProvider.class;
        }
    },
    /**
     * 美团
     */
    MEITUAN {
        @Override
        public String authorize() {
            return "https://openapi.waimai.meituan.com/oauth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://openapi.waimai.meituan.com/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://openapi.waimai.meituan.com/oauth/userinfo";
        }

        @Override
        public String refresh() {
            return "https://openapi.waimai.meituan.com/oauth/refresh_token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return MeituanProvider.class;
        }
    },
    /**
     * 小米
     */
    MI {
        @Override
        public String authorize() {
            return "https://account.xiaomi.com/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://account.xiaomi.com/oauth2/token";
        }

        @Override
        public String userInfo() {
            return "https://open.account.xiaomi.com/user/profile";
        }

        @Override
        public String refresh() {
            return "https://account.xiaomi.com/oauth2/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return MiProvider.class;
        }
    },
    /**
     * 微软
     */
    MICROSOFT {
        @Override
        public String authorize() {
            return "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize";
        }

        @Override
        public String accessToken() {
            return "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
        }

        @Override
        public String userInfo() {
            return "https://graph.microsoft.com/v1.0/me";
        }

        @Override
        public String refresh() {
            return "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return MicrosoftProvider.class;
        }
    },
    /**
     * 微软中国
     */
    MICROSOFT_CN {
        @Override
        public String authorize() {
            return "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/authorize";
        }

        @Override
        public String accessToken() {
            return "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token";
        }

        @Override
        public String userInfo() {
            return "https://microsoftgraph.chinacloudapi.cn/v1.0/me";
        }

        @Override
        public String refresh() {
            return "https://login.partner.microsoftonline.cn/%s/oauth2/v2.0/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return MicrosoftCnProvider.class;
        }
    },
    /**
     * Okta
     * 团队/组织的域名不同，此处通过配置动态组装
     */
    OKTA {
        @Override
        public String authorize() {
            return "https://%s.okta.com/oauth2/%s/v1/authorize";
        }

        @Override
        public String accessToken() {
            return "https://%s.okta.com/oauth2/%s/v1/token";
        }

        @Override
        public String refresh() {
            return "https://%s.okta.com/oauth2/%s/v1/token";
        }

        @Override
        public String userInfo() {
            return "https://%s.okta.com/oauth2/%s/v1/userinfo";
        }

        @Override
        public String revoke() {
            return "https://%s.okta.com/oauth2/%s/v1/revoke";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return OktaProvider.class;
        }
    },
    /**
     * oschina 开源中国
     */
    OSCHINA {
        @Override
        public String authorize() {
            return "https://www.oschina.net/action/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://www.oschina.net/action/openapi/token";
        }

        @Override
        public String userInfo() {
            return "https://www.oschina.net/action/openapi/user";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return OschinaProvider.class;
        }
    },
    /**
     * Pinterest
     */
    PINTEREST {
        @Override
        public String authorize() {
            return "https://api.pinterest.com/oauth";
        }

        @Override
        public String accessToken() {
            return "https://api.pinterest.com/v1/oauth/token";
        }

        @Override
        public String userInfo() {
            return "https://api.pinterest.com/v1/me";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return PinterestProvider.class;
        }
    },
    /**
     * 程序员客栈
     */
    PROGINN {
        @Override
        public String authorize() {
            return "https://www.proginn.com/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://www.proginn.com/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://www.proginn.com/openapi/user/basic_info";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return ProginnProvider.class;
        }
    },

    /**
     * QQ
     */
    QQ {
        @Override
        public String authorize() {
            return "https://graph.qq.com/oauth2.0/authorize";
        }

        @Override
        public String accessToken() {
            return "https://graph.qq.com/oauth2.0/token";
        }

        @Override
        public String userInfo() {
            return "https://graph.qq.com/user/get_user_info";
        }

        @Override
        public String refresh() {
            return "https://graph.qq.com/oauth2.0/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return QqProvider.class;
        }
    },
    /**
     * 人人网
     */
    RENREN {
        @Override
        public String authorize() {
            return "https://graph.renren.com/oauth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://graph.renren.com/oauth/token";
        }

        @Override
        public String refresh() {
            return "https://graph.renren.com/oauth/token";
        }

        @Override
        public String userInfo() {
            return "https://api.renren.com/v2/user/get";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return RenrenProvider.class;
        }
    },

    /**
     * Slack
     */
    SLACK {
        @Override
        public String authorize() {
            return "https://slack.com/oauth/v2/authorize";
        }

        /**
         * 该 API 获取到的是 access token
         * https://slack.com/api/oauth.token 获取到的是 workspace token
         *
         * @return String
         */
        @Override
        public String accessToken() {
            return "https://slack.com/api/oauth.v2.access";
        }

        @Override
        public String userInfo() {
            return "https://slack.com/api/users.info";
        }

        @Override
        public String revoke() {
            return "https://slack.com/api/auth.revoke";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return SlackProvider.class;
        }
    },
    /**
     * Stack Overflow
     */
    STACK_OVERFLOW {
        @Override
        public String authorize() {
            return "https://stackoverflow.com/oauth";
        }

        @Override
        public String accessToken() {
            return "https://stackoverflow.com/oauth/access_token/json";
        }

        @Override
        public String userInfo() {
            return "https://api.stackexchange.com/2.2/me";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return StackOverflowProvider.class;
        }
    },
    /**
     * 淘宝
     */
    TAOBAO {
        @Override
        public String authorize() {
            return "https://oauth.taobao.com/authorize";
        }

        @Override
        public String accessToken() {
            return "https://oauth.taobao.com/token";
        }

        @Override
        public String userInfo() {
            throw new AuthorizedException(ErrorCode.UNSUPPORTED.getCode());
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return TaobaoProvider.class;
        }
    },
    /**
     * Teambition
     */
    TEAMBITION {
        @Override
        public String authorize() {
            return "https://account.teambition.com/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://account.teambition.com/oauth2/access_token";
        }

        @Override
        public String refresh() {
            return "https://account.teambition.com/oauth2/refresh_token";
        }

        @Override
        public String userInfo() {
            return "https://api.teambition.com/users/me";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return TeambitionProvider.class;
        }
    },
    /**
     * 今日头条
     */
    TOUTIAO {
        @Override
        public String authorize() {
            return "https://open.snssdk.com/auth/authorize";
        }

        @Override
        public String accessToken() {
            return "https://open.snssdk.com/auth/token";
        }

        @Override
        public String userInfo() {
            return "https://open.snssdk.com/data/user_profile";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return ToutiaoProvider.class;
        }
    },
    /**
     * Twitter
     */
    TWITTER {
        @Override
        public String authorize() {
            return "https://api.twitter.com/oauth/authenticate";
        }

        @Override
        public String accessToken() {
            return "https://api.twitter.com/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.twitter.com/1.1/account/verify_credentials.json";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return TwitterProvider.class;
        }
    },
    /**
     * 企业微信二维码
     */
    WECHAT_ENTERPRISE {
        @Override
        public String authorize() {
            return "https://open.work.weixin.qq.com/wwopen/sso/qrConnect";
        }

        @Override
        public String accessToken() {
            return "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
        }

        @Override
        public String userInfo() {
            return "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeChatEeQrcodeProvider.class;
        }
    },
    /**
     * 企业微信二维码第三方
     */
    WECHAT_ENTERPRISE_QRCODE_THIRD {
        /**
         * 授权的api
         *
         * @return url
         */
        @Override
        public String authorize() {
            return "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect";
        }

        /**
         * 获取accessToken的api
         *
         * @return url
         */
        @Override
        public String accessToken() {
            return "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token";
        }

        /**
         * 获取用户信息的api
         *
         * @return url
         */
        @Override
        public String userInfo() {
            return "https://qyapi.weixin.qq.com/cgi-bin/service/get_login_info";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeChatEeThirdQrcodeProvider.class;
        }
    },
    /**
     * 企业微信网页
     */
    WECHAT_ENTERPRISE_WEB {
        @Override
        public String authorize() {
            return "https://open.weixin.qq.com/connect/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
        }

        @Override
        public String userInfo() {
            return "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeChatEeWebProvider.class;
        }
    },
    /**
     * 微信公众平台
     */
    WECHAT_MP {
        @Override
        public String authorize() {
            return "https://open.weixin.qq.com/connect/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://api.weixin.qq.com/sns/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.weixin.qq.com/sns/userinfo";
        }

        @Override
        public String refresh() {
            return "https://api.weixin.qq.com/sns/oauth2/refresh_token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeChatMpProvider.class;
        }
    },

    /**
     * 微信开放平台
     */
    WECHAT_OPEN {
        @Override
        public String authorize() {
            return "https://open.weixin.qq.com/connect/qrconnect";
        }

        @Override
        public String accessToken() {
            return "https://api.weixin.qq.com/sns/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.weixin.qq.com/sns/userinfo";
        }

        @Override
        public String refresh() {
            return "https://api.weixin.qq.com/sns/oauth2/refresh_token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeChatOpenProvider.class;
        }
    },
    /**
     * 微博
     */
    WEIBO {
        @Override
        public String authorize() {
            return "https://api.weibo.com/oauth2/authorize";
        }

        @Override
        public String accessToken() {
            return "https://api.weibo.com/oauth2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.weibo.com/2/users/show.json";
        }

        @Override
        public String revoke() {
            return "https://api.weibo.com/oauth2/revokeoauth2";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return WeiboProvider.class;
        }
    },
    /**
     * 喜马拉雅
     */
    XMLY {
        @Override
        public String authorize() {
            return "https://api.ximalaya.com/oauth2/js/authorize";
        }

        @Override
        public String accessToken() {
            return "https://api.ximalaya.com/oauth2/v2/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.ximalaya.com/profile/user_info";
        }

        @Deprecated
        @Override
        public String refresh() {
            return "https://oauth.aliyun.com/v1/token";
        }

        @Override
        public Class<? extends DefaultProvider> getTargetClass() {
            return XmlyProvider.class;
        }
    }

}
