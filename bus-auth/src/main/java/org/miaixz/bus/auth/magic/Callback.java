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
package org.miaixz.bus.auth.magic;

import java.io.Serializable;

import org.miaixz.bus.core.xyz.StringKit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Parameter class for authorization callbacks. This class encapsulates various parameters received during the callback
 * phase of an authentication flow.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Callback implements Serializable {

    /**
     * The 'code' parameter returned after accessing the AuthorizeUrl.
     */
    private String code;

    /**
     * The 'auth_code' parameter returned after accessing the AuthorizeUrl. This parameter is currently only used for
     * Alipay login.
     */
    private String auth_code;

    /**
     * The 'state' parameter returned after accessing the AuthorizeUrl. Used to compare with the state before requesting
     * the AuthorizeUrl to prevent CSRF attacks.
     */
    private String state;

    /**
     * The parameter name for 'code' when receiving authorization from Huawei login.
     */
    private String authorization_code;

    /**
     * The 'oauth_token' returned after a Twitter callback.
     */
    private String oauth_token;

    /**
     * The 'oauth_verifier' returned after a Twitter callback.
     */
    private String oauth_verifier;

    /**
     * Apple returns this value only when the user authorizes the application for the first time. If your application
     * has already obtained user authorization, Apple will not return this value again.
     * 
     * @see <a href="https://developer.apple.com/documentation/sign_in_with_apple/useri">Apple User Info</a>
     */
    private String user;

    /**
     * Apple error message, returned only when the user cancels authorization.
     * 
     * @see <a href=
     *      "https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_js/incorporating_sign_in_with_apple_into_other_platforms">Apple
     *      Error Response</a>
     */
    private String error;

    /**
     * This parameter is currently only used for Douyin Mini Program anonymous login.
     */
    private String anonymous_code;

    /**
     * VK device ID.
     */
    private String device_id;

    /**
     * Retrieves the authorization code. If the 'code' field is empty, it returns the 'auth_code' field.
     *
     * @return the authorization code
     */
    public String getCode() {
        return StringKit.isEmpty(code) ? auth_code : code;
    }

}
