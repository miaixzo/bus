/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.wechat;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.metric.AbstractProvider;

/**
 * 微信 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractWeChatProvider extends AbstractProvider {

    public AbstractWeChatProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public AbstractWeChatProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    /**
     * 获取微信平台用户的实际性别，0表示未定义，1表示男性，2表示女性
     *
     * @param originalGender 用户第三方标注的原始性别
     * @return 用户性别
     */
    public static Gender getWechatRealGender(String originalGender) {
        if (StringKit.isEmpty(originalGender) || "0".equals(originalGender)) {
            return Gender.UNKNOWN;
        }
        return Gender.of(originalGender);
    }

}
