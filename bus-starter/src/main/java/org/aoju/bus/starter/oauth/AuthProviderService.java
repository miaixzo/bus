/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.starter.oauth;

import lombok.RequiredArgsConstructor;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Provider;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.provider.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 授权服务提供
 *
 * @author Kimi Liu
 * @version 5.8.6
 * @since JDK 1.8+
 */
@RequiredArgsConstructor
public class AuthProviderService {

    /**
     * 组件配置
     */
    private static Map<Registry, Context> AUTH_CACHE = new ConcurrentHashMap<>();
    public final AuthProperties properties;

    /**
     * 注册组件
     *
     * @param type    组件名称
     * @param context 组件对象
     */
    public static void register(Registry type, Context context) {
        if (AUTH_CACHE.containsKey(type)) {
            throw new InstrumentException("重复注册同名称的组件：" + type.name());
        }
        AUTH_CACHE.putIfAbsent(type, context);
    }

    /**
     * 返回type对象
     *
     * @param type {@link Registry}
     * @return {@link Provider}
     */
    public Provider require(Registry type) {
        Context context = AUTH_CACHE.get(type);
        if (ObjectUtils.isEmpty(context)) {
            context = properties.getType().get(type);
        }
        if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(context);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(context);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(context);
        } else if (Registry.CSDN.equals(type)) {
            return new CsdnProvider(context);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(context);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(context);
        } else if (Registry.ELEME.equals(type)) {
            return new ElemeProvider(context);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(context);
        } else if (Registry.FEISHU.equals(type)) {
            return new FeishuProvider(context);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(context);
        } else if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(context);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(context);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(context);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(context);
        } else if (Registry.JD.equals(type)) {
            return new JdProvider(context);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(context);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(context);
        } else if (Registry.MEITUAN.equals(type)) {
            return new MeituanProvider(context);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(context);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(context);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(context);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(context);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(context);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(context);
        } else if (Registry.STACKOVERFLOW.equals(type)) {
            return new StackOverflowProvider(context);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(context);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(context);
        } else if (Registry.TENCENT.equals(type)) {
            return new TencentProvider(context);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(context);
        } else if (Registry.TWITTER.equals(type)) {
            return new TwitterProvider(context);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEEProvider(context);
        } else if (Registry.WECHAT_MP.equals(type)) {
            return new WeChatMpProvider(context);
        } else if (Registry.WECHAT_OP.equals(type)) {
            return new WeChatOPProvider(context);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(context);
        }
        throw new InstrumentException(Builder.ErrorCode.UNSUPPORTED.getMsg());
    }

}
