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
import org.aoju.bus.oauth.Complex;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Provider;
import org.aoju.bus.oauth.cache.StateCache;
import org.aoju.bus.oauth.provider.*;

import static org.aoju.bus.oauth.Registry.*;

@RequiredArgsConstructor
public class AuthProviderService {

    private final AuthProperties properties;
    private final StateCache stateCache;

    /**
     * 返回Complex对象
     *
     * @param complex {@link Complex}
     * @return {@link Provider}
     */
    public Provider get(Complex complex) {
        Context config = properties.getType().get(complex);
        if (GITHUB.equals(complex)) {
            return new GithubProvider(config, stateCache);
        } else if (WEIBO.equals(complex)) {
            return new WeiboProvider(config, stateCache);
        } else if (GITEE.equals(complex)) {
            return new GiteeProvider(config, stateCache);
        } else if (DINGTALK.equals(complex)) {
            return new DingTalkProvider(config, stateCache);
        } else if (BAIDU.equals(complex)) {
            return new BaiduProvider(config, stateCache);
        } else if (CSDN.equals(complex)) {
            return new CsdnProvider(config, stateCache);
        } else if (CODING.equals(complex)) {
            return new CodingProvider(config, stateCache);
        } else if (TENCENT_CLOUD.equals(complex)) {
            return new TencentCloudProvider(config, stateCache);
        } else if (OSCHINA.equals(complex)) {
            return new OschinaProvider(config, stateCache);
        } else if (ALIPAY.equals(complex)) {
            return new AlipayProvider(config, stateCache);
        } else if (QQ.equals(complex)) {
            return new QqProvider(config, stateCache);
        } else if (WECHAT.equals(complex)) {
            return new WeChatProvider(config, stateCache);
        } else if (TAOBAO.equals(complex)) {
            return new TaobaoProvider(config, stateCache);
        } else if (GOOGLE.equals(complex)) {
            return new GoogleProvider(config, stateCache);
        } else if (FACEBOOK.equals(complex)) {
            return new FacebookProvider(config, stateCache);
        } else if (DOUYIN.equals(complex)) {
            return new DouyinProvider(config, stateCache);
        } else if (LINKEDIN.equals(complex)) {
            return new LinkedinProvider(config, stateCache);
        } else if (MICROSOFT.equals(complex)) {
            return new MicrosoftProvider(config, stateCache);
        } else if (MI.equals(complex)) {
            return new MiProvider(config, stateCache);
        } else if (TOUTIAO.equals(complex)) {
            return new ToutiaoProvider(config, stateCache);
        } else if (TEAMBITION.equals(complex)) {
            return new TeambitionProvider(config, stateCache);
        } else if (RENREN.equals(complex)) {
            return new RenrenProvider(config, stateCache);
        } else if (PINTEREST.equals(complex)) {
            return new PinterestProvider(config, stateCache);
        } else if (STACK_OVERFLOW.equals(complex)) {
            return new StackOverflowProvider(config, stateCache);
        } else if (HUAWEI.equals(complex)) {
            return new HuaweiProvider(config, stateCache);
        } else if (WECHAT_ENTERPRISE.equals(complex)) {
            return new WeChatEEProvider(config, stateCache);
        } else if (GITLAB.equals(complex)) {
            return new GitlabProvider(config, stateCache);
        } else if (KUJIALE.equals(complex)) {
            return new KujialeProvider(config, stateCache);
        }
        throw new InstrumentException(Builder.Status.UNSUPPORTED.getCode());
    }

}
