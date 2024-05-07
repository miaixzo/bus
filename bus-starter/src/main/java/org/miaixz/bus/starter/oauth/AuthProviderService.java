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
package org.miaixz.bus.starter.oauth;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.cache.OauthCache;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.metric.AuthorizeProvider;
import org.miaixz.bus.oauth.metric.afdian.AfDianProvider;
import org.miaixz.bus.oauth.metric.alipay.AlipayProvider;
import org.miaixz.bus.oauth.metric.aliyun.AliyunProvider;
import org.miaixz.bus.oauth.metric.amazon.AmazonProvider;
import org.miaixz.bus.oauth.metric.baidu.BaiduProvider;
import org.miaixz.bus.oauth.metric.coding.CodingProvider;
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
import org.miaixz.bus.oauth.metric.ximalaya.XimalayaProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 授权服务提供
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuthProviderService {

    /**
     * 组件配置
     */
    private static Map<Registry, Context> CACHE = new ConcurrentHashMap<>();
    public AuthProperties properties;
    public ExtendCache extendCache;

    public AuthProviderService(AuthProperties properties) {
        this(properties, OauthCache.INSTANCE);
    }

    public AuthProviderService(AuthProperties properties, ExtendCache extendCache) {
        this.properties = properties;
        this.extendCache = extendCache;
    }

    /**
     * 注册组件
     *
     * @param type    组件名称
     * @param context 组件对象
     */
    public static void register(Registry type, Context context) {
        if (CACHE.containsKey(type)) {
            throw new InternalException("重复注册同名称的组件：" + type.name());
        }
        CACHE.putIfAbsent(type, context);
    }

    /**
     * 返回type对象
     *
     * @param type {@link Registry}
     * @return {@link AuthorizeProvider}
     */

    public AuthorizeProvider require(Registry type) {
        Context context = CACHE.get(type);
        if (ObjectKit.isEmpty(context)) {
            context = properties.getType().get(type);
        }
        if (Registry.AFDIAN.equals(type)) {
            return new AfDianProvider(context, extendCache);
        } else if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(context, extendCache);
        } else if (Registry.ALIYUN.equals(type)) {
            return new AliyunProvider(context, extendCache);
        } else if (Registry.AMAZON.equals(type)) {
            return new AmazonProvider(context, extendCache);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(context, extendCache);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(context, extendCache);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(context, extendCache);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(context, extendCache);
        } else if (Registry.ELEME.equals(type)) {
            return new ElemeProvider(context, extendCache);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(context, extendCache);
        } else if (Registry.FEISHU.equals(type)) {
            return new FeishuProvider(context, extendCache);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(context, extendCache);
        } else if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(context, extendCache);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(context, extendCache);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(context, extendCache);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(context, extendCache);
        } else if (Registry.JD.equals(type)) {
            return new JdProvider(context, extendCache);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(context, extendCache);
        } else if (Registry.LINE.equals(type)) {
            return new LineProvider(context, extendCache);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(context, extendCache);
        } else if (Registry.MEITUAN.equals(type)) {
            return new MeituanProvider(context, extendCache);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(context, extendCache);
        } else if (Registry.MICROSOFT_CN.equals(type)) {
            return new MicrosoftCnProvider(context, extendCache);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(context, extendCache);
        } else if (Registry.OKTA.equals(type)) {
            return new OktaProvider(context, extendCache);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(context, extendCache);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(context, extendCache);
        } else if (Registry.PROGINN.equals(type)) {
            return new ProginnProvider(context, extendCache);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(context, extendCache);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(context, extendCache);
        } else if (Registry.SLACK.equals(type)) {
            return new SlackProvider(context, extendCache);
        } else if (Registry.STACK_OVERFLOW.equals(type)) {
            return new StackOverflowProvider(context, extendCache);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(context, extendCache);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(context, extendCache);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(context, extendCache);
        } else if (Registry.TWITTER.equals(type)) {
            return new TwitterProvider(context, extendCache);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEeQrcodeProvider(context, extendCache);
        } else if (Registry.WECHAT_EE_QRCODE_THIRD.equals(type)) {
            return new WeChatEeThirdQrcodeProvider(context, extendCache);
        } else if (Registry.WECHAT_EE_WEB.equals(type)) {
            return new WeChatEeWebProvider(context, extendCache);
        } else if (Registry.WECHAT_MP.equals(type)) {
            return new WeChatMpProvider(context, extendCache);
        } else if (Registry.WECHAT_OPEN.equals(type)) {
            return new WeChatOpenProvider(context, extendCache);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(context, extendCache);
        } else if (Registry.XIMALAYA.equals(type)) {
            return new XimalayaProvider(context, extendCache);
        }
        throw new InternalException(ErrorCode.UNSUPPORTED.getDesc());
    }

}
