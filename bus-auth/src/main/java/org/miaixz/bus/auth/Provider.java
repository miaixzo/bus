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
package org.miaixz.bus.auth;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

/**
 * Common interface for all authentication providers. All platform providers must implement this interface. This
 * interface defines core authentication operations such as authorization, login, token revocation, and token
 * refreshing. Methods include:
 * <ul>
 * <li>{@link Provider#build(String)}</li>
 * <li>{@link Provider#authorize(Callback)}</li>
 * <li>{@link Provider#revoke(Authorization)}</li>
 * <li>{@link Provider#refresh(Authorization)}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends org.miaixz.bus.core.Provider {

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     * @throws AuthorizedException if the method is not implemented by the specific provider
     */
    default Message build(String state) {
        throw new AuthorizedException(ErrorCode._110001.getKey());
    }

    /**
     * Performs third-party login.
     *
     * @param callback the entity used to receive callback parameters after authorization
     * @return a {@link Message} containing the user information upon successful login
     * @throws AuthorizedException if the method is not implemented by the specific provider
     */
    default Message authorize(Callback callback) {
        throw new AuthorizedException(ErrorCode._110001.getKey());
    }

    /**
     * Retrieves the access token.
     *
     * @param callback the callback parameters after successful authorization
     * @return the {@link Authorization} containing access token details
     * @see AbstractProvider#build(String)
     */
    Message token(Callback callback);

    /**
     * Exchanges the token for user information.
     *
     * @param authorization the token information
     * @return {@link Claims} containing the user's information
     * @see AbstractProvider#token(Callback)
     */
    Message userInfo(Authorization authorization);

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if the method is not implemented by the specific provider
     */
    default Message refresh(Authorization authorization) {
        throw new AuthorizedException(ErrorCode._110001.getKey());
    }

    /**
     * Revokes the authorization.
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} indicating the result of the revocation
     * @throws AuthorizedException if the method is not implemented by the specific provider
     */
    default Message revoke(Authorization authorization) {
        throw new AuthorizedException(ErrorCode._110001.getKey());
    }

    /**
     * Returns the provider type identifier.
     *
     * @return the provider type identifier, which is {@link EnumValue.Povider#AUTH}
     */
    @Override
    default Object type() {
        return EnumValue.Povider.AUTH;
    }

}
