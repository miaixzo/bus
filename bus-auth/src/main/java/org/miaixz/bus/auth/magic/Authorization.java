/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
