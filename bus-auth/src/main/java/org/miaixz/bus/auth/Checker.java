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
package org.miaixz.bus.auth;

import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Validator for authorization configuration classes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Checker {

    /**
     * Checks if third-party login is supported for the given context and complex type.
     *
     * @param context the authentication context
     * @param complex the complex type of the third-party platform
     * @return true if third-party login is supported, false otherwise
     */
    public static boolean isSupportedAuth(Context context, Complex complex) {
        boolean isSupported = StringKit.isNotEmpty(context.getClientId())
                && StringKit.isNotEmpty(context.getClientSecret());
        if (isSupported && Registry.STACK_OVERFLOW == complex) {
            isSupported = StringKit.isNotEmpty(context.getUnionId());
        }
        if (isSupported && Registry.WECHAT_EE == complex) {
            isSupported = StringKit.isNotEmpty(context.getUnionId());
        }
        if (isSupported && (Registry.CODING == complex || Registry.OKTA == complex)) {
            isSupported = StringKit.isNotEmpty(context.getPrefix());
        }
        if (isSupported && Registry.XIMALAYA == complex) {
            isSupported = StringKit.isNotEmpty(context.getDeviceId()) && null != context.getType();
            if (isSupported) {
                isSupported = "3".equals(context.getType()) || StringKit.isNotEmpty(context.getUnionId());
            }
        }
        return isSupported;
    }

    /**
     * Checks the validity of the configuration. For some platforms, there are specific requirements for the redirect
     * URI. Generally, redirect URIs are http://, but for platforms like Facebook, the redirect URI must be an https
     * link.
     *
     * @param context the authentication context
     * @param complex the complex type of the third-party platform
     * @throws AuthorizedException if the redirect URI is invalid or missing
     */
    public static void check(Context context, Complex complex) {
        String redirectUri = context.getRedirectUri();
        if (context.isIgnoreRedirectUri()) {
            return;
        }
        if (StringKit.isEmpty(redirectUri)) {
            throw new AuthorizedException(ErrorCode._110005.getKey(), complex);
        }
        if (!Protocol.isHttp(redirectUri) && !Protocol.isHttps(redirectUri)) {
            throw new AuthorizedException(ErrorCode._110005.getKey(), complex);
        }
    }

    /**
     * Validates the code returned by the callback. In version {@code v1.10.0}, this method was changed to accept
     * {@code complex} and {@code callback} to uniformly handle different platforms that use different parameters to
     * receive the code.
     *
     * @param complex  the current authorization platform
     * @param callback the collection of parameters passed back from the third-party authorization callback
     * @throws AuthorizedException if the code is missing or invalid
     */
    public static void check(Complex complex, Callback callback) {
        // Twitter platform does not support callback code and state
        if (complex == Registry.TWITTER) {
            return;
        }
        String code = callback.getCode();
        if (StringKit.isEmpty(code) && complex == Registry.HUAWEI) {
            code = callback.getAuthorization_code();
        }
        if (StringKit.isEmpty(code)) {
            throw new AuthorizedException(ErrorCode._110007.getKey(), complex);
        }
    }

    /**
     * Validates the {@code state} returned by the callback. The {@code state} being absent or empty can only occur in
     * two scenarios: 1. The {@code state} has been used and normally cleared. 2. The {@code state} was forged by the
     * frontend and never existed.
     *
     * @param state   the {@code state} parameter, which must not be empty
     * @param complex the current authorization platform
     * @param cache   the {@code cache} implementation for state storage
     * @throws AuthorizedException if the state is empty or does not exist in the cache
     */
    public static void check(String state, Complex complex, CacheX cache) {
        // Twitter platform does not support callback code and state
        if (complex == Registry.TWITTER) {
            return;
        }
        if (StringKit.isEmpty(state) || !cache.containsKey(state)) {
            throw new AuthorizedException(ErrorCode._110008.getKey(), complex);
        }
    }

}
