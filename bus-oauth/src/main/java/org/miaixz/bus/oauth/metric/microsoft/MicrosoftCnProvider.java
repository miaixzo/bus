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
package org.miaixz.bus.oauth.metric.microsoft;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.ErrorCode;

/**
 * 微软中国 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MicrosoftCnProvider extends AbstractMicrosoftProvider {

    public MicrosoftCnProvider(Context context) {
        super(context, Registry.MICROSOFT_CN);
    }

    public MicrosoftCnProvider(Context context, ExtendCache cache) {
        super(context, Registry.MICROSOFT_CN, cache);
    }

    @Override
    protected void checkConfig(Context context) {
        super.checkConfig(context);
        // 微软中国的回调地址必须为https的链接或者localhost,不允许使用http
        if (Registry.MICROSOFT_CN == this.complex && !Protocol.isHttpsOrLocalHost(context.getRedirectUri())) {
            // Microsoft's redirect uri must use the HTTPS or localhost
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), this.complex);
        }
    }

}
