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
package org.miaixz.bus.auth.nimble.router;

import java.util.Map;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;

/**
 * Interface for OAuth2 protocol routing.
 * <p>
 * Defines standard methods for OAuth2 protocol forwarding, supporting authorization, token exchange, and user
 * information retrieval. Implements OAuth2 RFC 6749 specification while supporting extensions for different platforms.
 * </p>
 * <p>
 * Uses existing {@link Authorization}, {@link Callback}, and {@link Claims} classes to maintain consistency with the
 * bus-auth framework.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface OAuth2Router {

    /**
     * Builds the OAuth2 authorization URL.
     * <p>
     * Generates an OAuth2 authorization server URL based on the provided parameters. Users will be redirected to this
     * URL to initiate the authorization process.
     * </p>
     *
     * @param authUrl     the authorization endpoint URL
     * @param clientId    the client identifier
     * @param redirectUri the URI to redirect to after authorization
     * @param scope       the authorization scope
     * @param state       the state parameter for CSRF protection
     * @param params      extra parameters for platform-specific extensions (non-standard OAuth2 parameters)
     * @return the authorization URL as a string
     */
    String buildUrl(
            String authUrl,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> params);

    /**
     * Obtains the access token.
     * <p>
     * Exchanges the authorization code with the OAuth2 server to obtain an access token.
     * </p>
     *
     * @param callback     the callback information containing the authorization code
     * @param tokenUrl     the token endpoint URL
     * @param clientId     the client identifier
     * @param clientSecret the client secret
     * @param redirectUri  the URI to redirect to after authorization
     * @param params       extra parameters for platform-specific extensions (non-standard OAuth2 parameters)
     * @return {@link Authorization} containing access token and related information
     */
    Authorization getToken(
            Callback callback,
            String tokenUrl,
            String clientId,
            String clientSecret,
            String redirectUri,
            Map<String, Object> params);

    /**
     * Retrieves user information.
     * <p>
     * Uses the access token to fetch user information from the OAuth2 server.
     * </p>
     *
     * @param authorization the authorization information containing the access token
     * @param userinfoUrl   the user information endpoint URL
     * @return {@link Claims} containing user information
     */
    Claims getUserinfo(Authorization authorization, String userinfoUrl);

    /**
     * Refreshes the access token.
     * <p>
     * Obtains a new access token using the refresh token. By default, this operation is not supported. Subclasses can
     * optionally implement this method.
     * </p>
     *
     * @param accessToken the refresh token
     * @return {@link Authorization} containing the new access token
     */
    default Authorization refresh(String accessToken) {
        throw new UnsupportedOperationException("Refresh token not supported");
    }

    /**
     * Revokes the access token.
     * <p>
     * Revokes the specified access token. By default, this operation is not supported. Subclasses can optionally
     * implement this method.
     * </p>
     *
     * @param accessToken the access token to revoke
     * @return {@code true} if revocation was successful, {@code false} otherwise
     */
    default boolean revoke(String accessToken) {
        throw new UnsupportedOperationException("Revoke token not supported");
    }

}
