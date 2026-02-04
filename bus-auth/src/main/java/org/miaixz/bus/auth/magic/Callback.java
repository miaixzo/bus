/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
