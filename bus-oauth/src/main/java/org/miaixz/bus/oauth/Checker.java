/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;

/**
 * 授权配置类的校验器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Checker {

    /**
     * 是否支持第三方登录
     *
     * @param context context
     * @param complex complex
     * @return true or false
     */
    public static boolean isSupportedAuth(Context context, Complex complex) {
        boolean isSupported = StringKit.isNotEmpty(context.getAppKey())
                && StringKit.isNotEmpty(context.getAppSecret());
        if (isSupported && Registry.STACK_OVERFLOW == complex) {
            isSupported = StringKit.isNotEmpty(context.getStackOverflowKey());
        }
        if (isSupported && Registry.WECHAT_ENTERPRISE == complex) {
            isSupported = StringKit.isNotEmpty(context.getAgentId());
        }
        if (isSupported && (Registry.CODING == complex || Registry.OKTA == complex)) {
            isSupported = StringKit.isNotEmpty(context.getPrefix());
        }
        if (isSupported && Registry.XMLY == complex) {
            isSupported = StringKit.isNotEmpty(context.getDeviceId()) && null != context.getClientOsType();
            if (isSupported) {
                isSupported = context.getClientOsType() == 3 || StringKit.isNotEmpty(context.getPackId());
            }
        }
        return isSupported;
    }

    /**
     * 检查配置合法性。针对部分平台， 对redirect uri有特定要求。一般来说redirect uri都是http://，而对于facebook平台， redirect uri 必须是https的链接
     *
     * @param context context
     * @param complex complex
     */
    public static void checkConfig(Context context, Complex complex) {
        String redirectUri = context.getRedirectUri();
        if (context.isIgnoreCheckRedirectUri()) {
            return;
        }
        if (StringKit.isEmpty(redirectUri)) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), complex);
        }
        if (!Http.isHttp(redirectUri) && !Http.isHttps(redirectUri)) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), complex);
        }
        // facebook的回调地址必须为https的链接
        if (Registry.FACEBOOK == complex && !Http.isHttps(redirectUri)) {
            // FacebookScope's redirect uri must use the HTTPS protocol
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), complex);
        }
        // 微软的回调地址必须为https的链接或者localhost,不允许使用http
        if (Registry.MICROSOFT == complex && !Http.isHttpsOrLocalHost(redirectUri)) {
            // MicrosoftScope's redirect uri must use the HTTPS or localhost
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), complex);
        }
        // 微软中国的回调地址必须为https的链接或者localhost,不允许使用http
        if (Registry.MICROSOFT_CN == complex && !Http.isHttpsOrLocalHost(redirectUri)) {
            // MicrosoftScope's redirect uri must use the HTTPS or localhost
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), complex);
        }
    }

    /**
     * 校验回调传回的code
     * {@code v1.10.0}版本中改为传入{@code complex}和{@code callback}，对于不同平台使用不同参数接受code的情况统一做处理
     *
     * @param complex  当前授权平台
     * @param callback 从第三方授权回调回来时传入的参数集合
     */
    public static void checkCode(Complex complex, Callback callback) {
        // 推特平台不支持回调 code 和 state
        if (complex == Registry.TWITTER) {
            return;
        }
        String code = callback.getCode();
        if (complex == Registry.HUAWEI) {
            code = callback.getAuthorization_code();
        }
        if (StringKit.isEmpty(code)) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_CODE.getCode(), complex);
        }
    }

    /**
     * 校验回调传回的{@code state}，为空或者不存在
     * {@code state}不存在的情况只有两种：
     * 1. {@code state}已使用，被正常清除
     * 2. {@code state}为前端伪造，本身就不存在
     *
     * @param state          {@code state}一定不为空
     * @param complex        {@code complex}当前授权平台
     * @param authorizeCache {@code authorizeCache} state缓存实现
     */
    public static void checkState(String state, Complex complex, ExtendCache authorizeCache) {
        // 推特平台不支持回调 code 和 state
        if (complex == Registry.TWITTER) {
            return;
        }
        if (StringKit.isEmpty(state) || !authorizeCache.containsKey(state)) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_STATUS.getCode(), complex);
        }
    }

}
