/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.Cookie;
import org.miaixz.bus.http.UnoUrl;

import java.util.Collections;
import java.util.List;

/**
 * 为HTTP cookie提供策略和持久性
 * 作为策略，此接口的实现负责选择接受和拒绝哪些cookie。一个合理的策略是拒绝所有cookie，
 * 尽管这可能会干扰需要cookie的基于会话的身份验证方案
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CookieJar {

    /**
     * 从不接受任何cookie的设置
     */
    CookieJar NO_COOKIES = new CookieJar() {
        @Override
        public void saveFromResponse(UnoUrl url, List<Cookie> cookies) {
        }

        @Override
        public List<Cookie> loadForRequest(UnoUrl url) {
            return Collections.emptyList();
        }
    };

    /**
     * 据这个jar's的策略将HTTP响应中的{@code cookies}保存到这个存储中
     * 请注意，对于单个HTTP响应，如果响应包含一个拖车，则可以第二次调用此方法
     * 对于这个模糊的HTTP特性，{@code cookie}只包含预告片的cookie
     *
     * @param url     url信息
     * @param cookies cookie
     */
    void saveFromResponse(UnoUrl url, List<Cookie> cookies);

    /**
     * 将HTTP请求的cookie从jar加载到{@code url}
     * 此方法为网络请求返回一个可能为空的cookie列表
     * 简单的实现将返回尚未过期的已接受的Cookie，
     * 并返回{@linkplain Cookie#matches} {@code url}
     *
     * @param url url信息
     * @return the cookies
     */
    List<Cookie> loadForRequest(UnoUrl url);

}
