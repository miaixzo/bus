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
import java.util.Optional;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Abstract base class for authorization processing, supporting various protocols such as OAuth2, SAML, and LDAP.
 * Provides common logic for authorization, token acquisition, and user information retrieval, with protocol-specific
 * implementations handled by subclasses.
 *
 * @author Kimi Liu
 * @since Java 21+
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
            throw new AuthorizedException(ErrorCode._110002);
        }
        // Validate configuration
        this.validate(this.context);
    }

    /**
     * Processes the login flow, validates callback data, obtains an access token, and retrieves user information.
     *
     * @param callback the callback object containing authorization data (e.g., code, state)
     * @return a {@link Claims} object containing user information or an error message
     */
    @Override
    public Message authorize(Callback callback) {
        // Validate callback data
        this.validate(callback);
        // For OAuth2, validate the state parameter (if not ignored)
        if (!this.context.isIgnoreState() && Protocol.OIDC == this.complex.getProtocol()) {
            Checker.check(callback.getState(), this.complex, this.cache);
        }

        // Obtain access token
        Message message = this.token(callback);

        Authorization token = JsonKit.toPojo(JsonKit.toJsonString(message.getData()), Authorization.class);
        // Retrieve user information
        return this.userInfo(token);
    }

    /**
     * Generates the authorization URL to initiate the authentication flow.
     *
     * @param state the state parameter used to prevent CSRF attacks
     * @return the authorization URL, or null for protocols like LDAP that do not use an authorization URL
     */
    @Override
    public Message build(String state) {
        if (Protocol.OIDC == this.complex.getProtocol()) {
            // Build OAuth2 authorization URL
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Builder.fromUrl(getEndpoint(Endpoint.AUTHORIZE)).queryParam("response_type", "code")
                                    .queryParam("client_id", this.context.getClientId())
                                    .queryParam("redirect_uri", this.context.getRedirectUri())
                                    .queryParam("state", getRealState(state))
                                    .queryParam("scope", getScopes(Symbol.SPACE, true, getScopes(null))).build())
                    .build();
        } else if (Protocol.SAML == this.complex.getProtocol()) {
            // Build SAML single sign-on URL
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Builder.fromUrl(getEndpoint(Endpoint.AUTHORIZE))
                                    .queryParam("RelayState", getRealState(state)).build())
                    .build();
        }
        return null; // LDAP does not use an authorization URL
    }

    /**
     * Builds the OAuth2 access token URL.
     *
     * @param code the authorization code
     * @return the access token URL
     */
    protected String tokenUrl(String code) {
        return Builder.fromUrl(getEndpoint(Endpoint.TOKEN)).queryParam("code", code)
                .queryParam("client_id", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * Builds the OAuth2 refresh token URL.
     *
     * @param token the refresh token
     * @return the refresh token URL
     */
    protected String refreshUrl(String token) {
        return Builder.fromUrl(getEndpoint(Endpoint.REFRESH)).queryParam("client_id", context.getClientId())
                .queryParam("client_secret", context.getClientSecret()).queryParam("refresh_token", token)
                .queryParam("grant_type", "refresh_token").queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * Builds the OAuth2 user info URL.
     *
     * @param authorization the access token
     * @return the user information URL
     */
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(getEndpoint(Endpoint.USERINFO)).queryParam("access_token", authorization.getToken())
                .build();
    }

    /**
     * Builds the OAuth2 revoke authorization URL.
     *
     * @param authorization the access token
     * @return the revoke authorization URL
     */
    protected String revokeUrl(Authorization authorization) {
        return Builder.fromUrl(getEndpoint(Endpoint.REVOKE)).queryParam("access_token", authorization.getToken())
                .build();
    }

    /**
     * Executes a POST request to obtain an OAuth2 access token.
     *
     * @param code the authorization code
     * @return the response content
     */
    protected String doPostToken(String code) {
        return Httpx.post(tokenUrl(code));
    }

    /**
     * Executes a GET request to obtain an OAuth2 access token.
     *
     * @param code the authorization code
     * @return the response content
     */
    protected String doGetToken(String code) {
        return Httpx.get(tokenUrl(code));
    }

    /**
     * Executes a GET request to obtain OAuth2 user information.
     *
     * @param authorization the access token
     * @return the response content
     */
    protected String doGetUserInfo(Authorization authorization) {
        return Httpx.get(userInfoUrl(authorization));
    }

    /**
     * Executes a GET request to revoke OAuth2 authorization.
     *
     * @param authorization the access token
     * @return the response content
     */
    protected String doGetRevoke(Authorization authorization) {
        return Httpx.get(revokeUrl(authorization));
    }

    /**
     * Gets the endpoint URL.
     * <p>
     * Prioritizes the configuration from {@code context.getEndpoint()}. If not configured or empty, it falls back to
     * the default configuration from {@code complex.endpoint()}.
     *
     * @param endpoint the endpoint type
     * @return the endpoint URL
     */
    protected String getEndpoint(Endpoint endpoint) {
        return Optional.ofNullable(this.context.getEndpoint()).map(ep -> ep.get(endpoint)).filter(StringKit::isNotEmpty)
                .orElse(this.complex.endpoint().get(endpoint));
    }

    /**
     * Retrieves a list of default scopes from an array of {@link AuthorizeScope}.
     *
     * @param scopes an array of authorization scopes
     * @return a list of default scope names, or null if no scopes are provided or no default scopes are found
     */
    public static List<String> getScopes(AuthorizeScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).filter(AuthorizeScope::isDefault).map(AuthorizeScope::getScope)
                .collect(Collectors.toList());
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
     * Generates or retrieves a cached state parameter used to prevent CSRF attacks.
     *
     * @param state the provided state value; if empty, a new value will be generated
     * @return the non-empty state value
     */
    protected String getRealState(String state) {
        if (StringKit.isEmpty(state)) {
            state = ID.objectId();
        }
        this.cache.write(state, state);
        return state;
    }

    /**
     * Validates the completeness of the context configuration, checking required fields based on the protocol type.
     *
     * @param context the context configuration
     * @throws AuthorizedException if the configuration is incomplete
     */
    protected void validate(Context context) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(context, this.complex);
        }
    }

    /**
     * Validates callback data, primarily used for OAuth2 protocol.
     *
     * @param callback the callback object containing authorization data
     * @throws AuthorizedException if OAuth2 validation fails
     */
    protected void validate(Callback callback) {
        if (complex.getProtocol() == Protocol.OIDC) {
            Checker.check(complex, callback);
        }
    }

}
