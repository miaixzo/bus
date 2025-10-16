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
package org.miaixz.bus.auth.nimble;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;

/**
 * Abstract base class for authorization processing, supporting various protocols such as OAuth2, SAML, and LDAP.
 * Provides common logic for authorization, token acquisition, and user information retrieval, with protocol-specific
 * implementations handled by subclasses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements Provider {

    /**
     * The context object containing protocol-specific configurations.
     */
    protected Context context;
    /**
     * The protocol object defining protocol endpoints or configurations.
     */
    protected Complex complex;
    /**
     * The cache implementation used to store state or other temporary data.
     */
    protected CacheX cache;

    /**
     * Constructs an {@code AbstractProvider} with the specified context and protocol configuration.
     *
     * @param context the context configuration
     * @param complex the protocol configuration
     */
    public AbstractProvider(Context context, Complex complex) {
        this(context, complex, AuthCache.INSTANCE);
    }

    /**
     * Constructs an {@code AbstractProvider} with a custom cache implementation.
     *
     * @param context the context configuration
     * @param complex the protocol configuration
     * @param cache   the cache implementation
     * @throws AuthorizedException if the configuration is incomplete or invalid
     */
    public AbstractProvider(Context context, Complex complex, CacheX cache) {
        this.context = context;
        this.complex = complex;
        this.cache = cache;
        // Validate authorization support
        if (!Checker.isSupportedAuth(this.context, this.complex)) {
            throw new AuthorizedException(ErrorCode.PARAMETER_INCOMPLETE);
        }
        // Validate configuration
        check(this.context);
    }

    /**
     * Retrieves a list of default scopes from an array of {@link AuthorizeScope}.
     *
     * @param scopes an array of authorization scopes
     * @return a list of default scope names, or null if no scopes are provided or no default scopes are found
     */
    public static List<String> getDefaultScopes(AuthorizeScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).filter(AuthorizeScope::isDefault).map(AuthorizeScope::getScope)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of scope names from a variable number of {@link AuthorizeScope} arguments.
     *
     * @param scopes a variable number of authorization scopes
     * @return a list of scope names, or null if no scopes are provided
     */
    public static List<String> getScopes(AuthorizeScope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).map(AuthorizeScope::getScope).collect(Collectors.toList());
    }

    /**
     * Processes the login flow, validates callback data, obtains an access token, and retrieves user information.
     *
     * @param callback the callback object containing authorization data (e.g., code, state)
     * @return a {@link Message} object containing user information or an error message
     */
    @Override
    public Message login(Callback callback) {
        try {
            // Validate callback data
            check(callback);
            // For OAuth2, validate the state parameter (if not ignored)
            if (!context.isIgnoreState() && complex.getProtocol() == Protocol.OIDC) {
                Checker.check(callback.getState(), complex, cache);
            }

            // Obtain access token
            AuthToken authToken = this.getAccessToken(callback);
            // Retrieve user information
            Material user = this.getUserInfo(authToken);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(user).build();
        } catch (Exception e) {
            Logger.error("Authorization login failed.", e);
            return this.responseError(e);
        }
    }

    /**
     * Validates callback data, primarily used for OAuth2 protocol.
     *
     * @param callback the callback object containing authorization data
     * @throws AuthorizedException if OAuth2 validation fails
     */
    protected void check(Callback callback) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(complex, callback);
        }
    }

    /**
     * Constructs an error response message for exceptions during the login process.
     *
     * @param e the exception that occurred
     * @return a {@link Message} object containing error details
     */
    protected Message responseError(Exception e) {
        String errorCode = ErrorCode._FAILURE.getKey();
        String errorMsg = e.getMessage();
        if (e instanceof AuthorizedException) {
            AuthorizedException authException = ((AuthorizedException) e);
            errorCode = authException.getErrcode();
            if (StringKit.isNotEmpty(authException.getErrmsg())) {
                errorMsg = authException.getErrmsg();
            }
        }
        return Message.builder().errcode(errorCode).errmsg(errorMsg).build();
    }

    /**
     * Generates the authorization URL to initiate the authentication flow.
     *
     * @param state the state parameter used to prevent CSRF attacks
     * @return the authorization URL, or null for protocols like LDAP that do not use an authorization URL
     */
    @Override
    public String authorize(String state) {
        if (complex.getProtocol() == Protocol.OIDC) {
            // Build OAuth2 authorization URL
            return Builder.fromUrl(this.complex.authorize()).queryParam("response_type", "code")
                    .queryParam("client_id", this.context.getAppKey())
                    .queryParam("redirect_uri", this.context.getRedirectUri()).queryParam("state", getRealState(state))
                    .queryParam("scope", getScopes(Symbol.SPACE, true, getDefaultScopes(null))).build();
        } else if (this.complex.getProtocol() == Protocol.SAML) {
            // Build SAML single sign-on URL
            return Builder.fromUrl(this.complex.endpoint().get("ssoEndpoint"))
                    .queryParam("RelayState", getRealState(state)).build();
        }
        return null; // LDAP does not use an authorization URL
    }

    /**
     * Constructs the OAuth2 access token URL.
     *
     * @param code the authorization code
     * @return the access token URL
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * Constructs the OAuth2 refresh token URL.
     *
     * @param refreshToken the refresh token
     * @return the refresh token URL
     */
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh()).queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret()).queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token").queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * Constructs the OAuth2 user information URL.
     *
     * @param authToken the access token
     * @return the user information URL
     */
    protected String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(complex.userinfo()).queryParam("access_token", authToken.getAccessToken()).build();
    }

    /**
     * Constructs the OAuth2 revoke authorization URL.
     *
     * @param authToken the access token
     * @return the revoke authorization URL
     */
    protected String revokeUrl(AuthToken authToken) {
        return Builder.fromUrl(complex.revoke()).queryParam("access_token", authToken.getAccessToken()).build();
    }

    /**
     * Generates or retrieves a cached state parameter used to prevent CSRF attacks.
     *
     * @param state the provided state value; if empty, a new value will be generated
     * @return the non-empty state value
     */
    protected String getRealState(String state) {
        if (StringKit.isEmpty(state)) {
            state = ID.objectId();
        }
        cache.write(state, state);
        return state;
    }

    /**
     * Executes a POST request to obtain an OAuth2 access token.
     *
     * @param code the authorization code
     * @return the response content
     */
    protected String doPostAuthorizationCode(String code) {
        return Httpx.post(accessTokenUrl(code));
    }

    /**
     * Executes a GET request to obtain an OAuth2 access token.
     *
     * @param code the authorization code
     * @return the response content
     */
    protected String doGetAuthorizationCode(String code) {
        return Httpx.get(accessTokenUrl(code));
    }

    /**
     * Executes a GET request to obtain OAuth2 user information.
     *
     * @param authToken the access token
     * @return the response content
     */
    protected String doGetUserInfo(AuthToken authToken) {
        return Httpx.get(userInfoUrl(authToken));
    }

    /**
     * Executes a GET request to revoke OAuth2 authorization.
     *
     * @param authToken the access token
     * @return the response content
     */
    protected String doGetRevoke(AuthToken authToken) {
        return Httpx.get(revokeUrl(authToken));
    }

    /**
     * Constructs a scope string from configured or default scopes.
     *
     * @param separator     the delimiter for multiple scopes
     * @param encode        whether to URL-encode the scope string
     * @param defaultScopes default scopes to use if no scopes are configured
     * @return the scope string, or an empty string if no scopes are found
     */
    protected String getScopes(String separator, boolean encode, List<String> defaultScopes) {
        List<String> scopes = context.getScopes();
        if (null == scopes || scopes.isEmpty()) {
            if (null == defaultScopes || defaultScopes.isEmpty()) {
                return Normal.EMPTY;
            }
            scopes = defaultScopes;
        }
        if (null == separator) {
            separator = Symbol.SPACE;
        }
        String scope = String.join(separator, scopes);
        return encode ? UrlEncoder.encodeAll(scope) : scope;
    }

    /**
     * Validates the completeness of the context configuration, checking required fields based on the protocol type.
     *
     * @param context the context configuration
     * @throws AuthorizedException if the configuration is incomplete
     */
    protected void check(Context context) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(context, this.complex);
        }
    }

}
