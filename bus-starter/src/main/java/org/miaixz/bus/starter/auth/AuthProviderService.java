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
package org.miaixz.bus.starter.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.nimble.afdian.AfDianProvider;
import org.miaixz.bus.auth.nimble.alipay.AlipayProvider;
import org.miaixz.bus.auth.nimble.aliyun.AliyunProvider;
import org.miaixz.bus.auth.nimble.amazon.AmazonProvider;
import org.miaixz.bus.auth.nimble.baidu.BaiduProvider;
import org.miaixz.bus.auth.nimble.coding.CodingProvider;
import org.miaixz.bus.auth.nimble.dingtalk.DingTalkProvider;
import org.miaixz.bus.auth.nimble.douyin.DouyinProvider;
import org.miaixz.bus.auth.nimble.eleme.ElemeProvider;
import org.miaixz.bus.auth.nimble.facebook.FacebookProvider;
import org.miaixz.bus.auth.nimble.feishu.FeishuProvider;
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
import org.miaixz.bus.auth.nimble.wechat.mp.WeChatMpProvider;
import org.miaixz.bus.auth.nimble.wechat.open.WeChatOpenProvider;
import org.miaixz.bus.auth.nimble.weibo.WeiboProvider;
import org.miaixz.bus.auth.nimble.ximalaya.XimalayaProvider;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

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
    public ExtendCache cache;

    public AuthProviderService(AuthProperties properties) {
        this(properties, AuthCache.INSTANCE);
    }

    public AuthProviderService(AuthProperties properties, ExtendCache cache) {
        this.properties = properties;
        this.cache = cache;
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
     * @return {@link Provider}
     */

    public Provider require(Registry type) {
        Context context = CACHE.get(type);
        if (ObjectKit.isEmpty(context)) {
            context = properties.getType().get(type);
        }
        if (Registry.AFDIAN.equals(type)) {
            return new AfDianProvider(context, cache);
        } else if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(context, cache);
        } else if (Registry.ALIYUN.equals(type)) {
            return new AliyunProvider(context, cache);
        } else if (Registry.AMAZON.equals(type)) {
            return new AmazonProvider(context, cache);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(context, cache);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(context, cache);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(context, cache);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(context, cache);
        } else if (Registry.ELEME.equals(type)) {
            return new ElemeProvider(context, cache);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(context, cache);
        } else if (Registry.FEISHU.equals(type)) {
            return new FeishuProvider(context, cache);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(context, cache);
        } else if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(context, cache);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(context, cache);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(context, cache);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(context, cache);
        } else if (Registry.JD.equals(type)) {
            return new JdProvider(context, cache);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(context, cache);
        } else if (Registry.LINE.equals(type)) {
            return new LineProvider(context, cache);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(context, cache);
        } else if (Registry.MEITUAN.equals(type)) {
            return new MeituanProvider(context, cache);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(context, cache);
        } else if (Registry.MICROSOFT_CN.equals(type)) {
            return new MicrosoftCnProvider(context, cache);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(context, cache);
        } else if (Registry.OKTA.equals(type)) {
            return new OktaProvider(context, cache);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(context, cache);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(context, cache);
        } else if (Registry.PROGINN.equals(type)) {
            return new ProginnProvider(context, cache);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(context, cache);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(context, cache);
        } else if (Registry.SLACK.equals(type)) {
            return new SlackProvider(context, cache);
        } else if (Registry.STACK_OVERFLOW.equals(type)) {
            return new StackOverflowProvider(context, cache);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(context, cache);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(context, cache);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(context, cache);
        } else if (Registry.TWITTER.equals(type)) {
            return new TwitterProvider(context, cache);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEeQrcodeProvider(context, cache);
        } else if (Registry.WECHAT_EE_QRCODE.equals(type)) {
            return new WeChatEeThirdQrcodeProvider(context, cache);
        } else if (Registry.WECHAT_EE_WEB.equals(type)) {
            return new WeChatEeWebProvider(context, cache);
        } else if (Registry.WECHAT_MP.equals(type)) {
            return new WeChatMpProvider(context, cache);
        } else if (Registry.WECHAT_OPEN.equals(type)) {
            return new WeChatOpenProvider(context, cache);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(context, cache);
        } else if (Registry.XIMALAYA.equals(type)) {
            return new XimalayaProvider(context, cache);
        }
        throw new InternalException(ErrorCode.UNSUPPORTED.getDesc());
    }

}
