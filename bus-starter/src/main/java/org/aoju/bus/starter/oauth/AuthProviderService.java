/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.starter.oauth;

import lombok.RequiredArgsConstructor;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Provider;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.metric.StateCache;
import org.aoju.bus.oauth.provider.*;

/**
 * 授权服务提供
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
@RequiredArgsConstructor
public class AuthProviderService {

    public final AuthProperties properties;
    public final StateCache stateCache;

    /**
     * 返回type对象
     *
     * @param type {@link Registry}
     * @return {@link Provider}
     */
    public Provider get(Registry type) {
        Context context = properties.getType().get(type);
        if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(context, stateCache);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(context, stateCache);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(context, stateCache);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(context, stateCache);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(context, stateCache);
        } else if (Registry.CSDN.equals(type)) {
            return new CsdnProvider(context, stateCache);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(context, stateCache);
        } else if (Registry.TENCENT_CLOUD.equals(type)) {
            return new TencentCloudProvider(context, stateCache);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(context, stateCache);
        } else if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(context, stateCache);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(context, stateCache);
        } else if (Registry.WECHAT_OPEN.equals(type)) {
            return new WeChatOPProvider(context, stateCache);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(context, stateCache);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(context, stateCache);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(context, stateCache);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(context, stateCache);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(context, stateCache);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(context, stateCache);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(context, stateCache);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(context, stateCache);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(context, stateCache);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(context, stateCache);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(context, stateCache);
        } else if (Registry.STACK.equals(type)) {
            return new StackOverflowProvider(context, stateCache);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(context, stateCache);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEEProvider(context, stateCache);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(context, stateCache);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(context, stateCache);
        }
        throw new InstrumentException(Builder.Status.UNSUPPORTED.getCode());
    }

}
