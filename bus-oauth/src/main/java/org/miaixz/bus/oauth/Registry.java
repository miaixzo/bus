/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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

/**
 * 内置的各api需要的url, 用枚举类分平台类型管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry implements Complex {

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
    },
    /**
     * Coding
     */
    CODING {
        @Override
        public String authorize() {
            return "https://coding.net/oauth_authorize.html";
        }

        @Override
        public String accessToken() {
            return "https://coding.net/api/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://coding.net/api/account/current_user";
        }
    },
    /**
     * 钉钉
     */
    DINGTALK {
        @Override
        public String authorize() {
            return "https://oapi.dingtalk.com/connect/oauth2/sns_authorize";
        }

        @Override
        public String accessToken() {
            throw new AuthorizedException(Builder.ErrorCode.UNSUPPORTED.getCode());
        }

        @Override
        public String userInfo() {
            return "https://oapi.dingtalk.com/sns/getuserinfo_bycode";
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
    },
    /**
     * Facebook
     */
    FACEBOOK {
        @Override
        public String authorize() {
            return "https://www.facebook.com/v10.0/dialog/oauth";
        }

        @Override
        public String accessToken() {
            return "https://graph.facebook.com/v10.0/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://graph.facebook.com/v10.0/me";
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
    },
    /**
     * gitee
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
    },
    /**
     * Line
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
    },
    /**
     * 微软
     */
    MICROSOFT {
        @Override
        public String authorize() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        }

        @Override
        public String accessToken() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        }

        @Override
        public String userInfo() {
            return "https://graph.microsoft.com/v1.0/me";
        }

        @Override
        public String refresh() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        }
    },
    /**
     * 小米
     */
    XIAOMI {
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
    },
    /**
     * Stack Overflow
     */
    STACKOVERFLOW {
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
            throw new AuthorizedException(Builder.ErrorCode.UNSUPPORTED.getCode());
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
    },
    /**
     * 腾讯云开发者平台
     */
    TENCENT {
        @Override
        public String authorize() {
            return "https://dev.tencent.com/oauth_authorize.html";
        }

        @Override
        public String accessToken() {
            return "https://dev.tencent.com/api/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://dev.tencent.com/api/account/current_user";
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
    },
    /**
     * 企业微信
     */
    WECHAT_EE {
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
    },
    /**
     * 微信开放平台
     */
    WECHAT_OP {
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
    },
    /**
     * 微信小程序
     */
    WECHAT_MA {
        @Override
        public String authorize() {
            return "https://api.weixin.qq.com/sns/jscode2session";
        }

        @Override
        public String accessToken() {
            return "https://api.weixin.qq.com/cgi-bin/token";
        }

        @Override
        public String userInfo() {
            return "";
        }

        @Override
        public String refresh() {
            return "";
        }
    },
    /**
     * 新浪微博
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

        @Override
        public String refresh() {
            return "https://oauth.aliyun.com/v1/token";
        }
    };

    public static Registry get(String name) {
        for (Registry registry : Registry.values()) {
            if (registry.name().equalsIgnoreCase(name)) {
                return registry;
            }
        }
        throw new IllegalArgumentException("not support");
    }

}
