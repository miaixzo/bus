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
package org.miaixz.bus.oauth;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

/**
 * {@code Provider}公共接口，所有平台的{@code Provider}都需要实现该接口 {@link Provider#authorize(String)}
 * {@link Provider#login(Callback)} {@link Provider#revoke(AccToken)} {@link Provider#refresh(AccToken)}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends org.miaixz.bus.core.Provider {

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    default String authorize(String state) {
        throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 第三方登录
     *
     * @param callback 用于接收回调参数的实体
     * @return 返回登录成功后的用户信息
     */
    default Message login(Callback callback) {
        throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 撤销授权
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    default Message revoke(AccToken accToken) {
        throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    default Message refresh(AccToken accToken) {
        throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 获取access token
     *
     * @param callback 授权成功后的回调参数
     * @return token
     * @see AbstractProvider#authorize(String)
     */
    AccToken getAccessToken(Callback callback);

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see AbstractProvider#getAccessToken(Callback)
     */
    Material getUserInfo(AccToken accToken);

    @Override
    default Object type() {
        return EnumValue.Povider.OAUTH;
    }

}
