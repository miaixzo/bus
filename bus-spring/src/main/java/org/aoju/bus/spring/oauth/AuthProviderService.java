/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.spring.oauth;

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
 * @version 5.0.1
 * @since JDK 1.8+
 */
@RequiredArgsConstructor
public class AuthProviderService {

    final AuthProperties properties;
    final StateCache stateCache;

    /**
     * 返回type对象
     *
     * @param type {@link Registry}
     * @return {@link Provider}
     */
    public Provider get(Registry type) {
        Context config = properties.getType().get(type);
        if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(config, stateCache);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(config, stateCache);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(config, stateCache);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(config, stateCache);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(config, stateCache);
        } else if (Registry.CSDN.equals(type)) {
            return new CsdnProvider(config, stateCache);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(config, stateCache);
        } else if (Registry.TENCENT_CLOUD.equals(type)) {
            return new TencentCloudProvider(config, stateCache);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(config, stateCache);
        } else if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(config, stateCache);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(config, stateCache);
        } else if (Registry.WECHAT.equals(type)) {
            return new WeChatProvider(config, stateCache);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(config, stateCache);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(config, stateCache);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(config, stateCache);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(config, stateCache);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(config, stateCache);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(config, stateCache);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(config, stateCache);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(config, stateCache);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(config, stateCache);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(config, stateCache);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(config, stateCache);
        } else if (Registry.STACK.equals(type)) {
            return new StackOverflowProvider(config, stateCache);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(config, stateCache);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEEProvider(config, stateCache);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(config, stateCache);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(config, stateCache);
        }
        throw new InstrumentException(Builder.Status.UNSUPPORTED.getCode());
    }

}
