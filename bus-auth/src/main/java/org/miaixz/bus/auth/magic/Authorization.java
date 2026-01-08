/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Authentication token required for authorization. This class encapsulates various token-related information obtained
 * during the authentication process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Authorization implements Serializable {

    /**
     * The access token issued by the authorization server.
     */
    private String token;
    /**
     * The lifetime in seconds of the access token.
     */
    private int expireIn;
    /**
     * The refresh token, which can be used to obtain new access tokens.
     */
    private String refresh;
    /**
     * The lifetime in seconds of the refresh token.
     */
    private int refreshExpireIn;
    /**
     * User ID, typically from the third-party platform.
     */
    private String uid;
    /**
     * Open ID, typically from the third-party platform.
     */
    private String openId;

    /**
     * Union ID, a unique identifier across multiple applications of the same platform (e.g., WeChat).
     */
    private String unionId;

    /**
     * Google-specific additional property: scope of the granted access.
     */
    private String scope;
    /**
     * Google-specific additional property: type of the token issued.
     */
    private String token_type;
    /**
     * Google-specific additional property: ID Token, a JSON Web Token (JWT) that contains claims about the
     * authentication of an end-user.
     */
    private String idToken;

    /**
     * Xiaomi-specific additional property: MAC algorithm used for signing.
     */
    private String macAlgorithm;
    /**
     * Xiaomi-specific additional property: MAC key used for signing.
     */
    private String macKey;

    /**
     * WeChat Work-specific additional property: authorization code.
     */
    private String code;
    /**
     * WeChat Official Account - available for web authorization login. WeChat adds a snapshot page logic for web
     * authorization login, where the uid, oid, avatar, and nickname obtained are virtual information.
     */
    private boolean snapshotUser;

    /**
     * Twitter-specific additional property: OAuth token.
     */
    private String oauthToken;
    /**
     * Twitter-specific additional property: OAuth token secret.
     */
    private String oauthTokenSecret;
    /**
     * Twitter-specific additional property: user ID.
     */
    private String userId;
    /**
     * Twitter-specific additional property: screen name.
     */
    private String screenName;
    /**
     * Twitter-specific additional property: indicates if the OAuth callback was confirmed.
     */
    private Boolean oauthCallbackConfirmed;

    /**
     * Apple-specific additional property: username.
     */
    private String username;

    /**
     * VK-specific additional property: device ID.
     */
    private String deviceId;

}
