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
package org.aoju.bus.oauth;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;

/**
 * 公共接口，所有平台的都需要实现该接口
 * {@link Provider#authorize(String)}
 * {@link Provider#login(Callback)}
 * {@link Provider#revoke(AccToken)}
 * {@link Provider#refresh(AccToken)}
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public interface Provider {

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    default String authorize(String state) {
        throw new InstrumentException(Builder.Status.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 第三方登录
     *
     * @param callback 用于接收回调参数的实体
     * @return 返回登录成功后的用户信息
     */
    default Message login(Callback callback) {
        throw new InstrumentException(Builder.Status.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 撤销授权
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default Message revoke(AccToken token) {
        throw new InstrumentException(Builder.Status.NOT_IMPLEMENTED.getCode());
    }

    /**
     * 刷新access token （续期）
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default Message refresh(AccToken token) {
        throw new InstrumentException(Builder.Status.NOT_IMPLEMENTED.getCode());
    }

}
