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

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;

/**
 * Abstract base class for OAuth2 routers.
 * <p>
 * Provides common OAuth2 protocol forwarding functionality and encapsulates frequently used utility methods. Subclasses
 * only need to implement platform-specific logic.
 * </p>
 * <p>
 * This class integrates codec functionality (state/token encoding/decoding) and parameter extraction, eliminating the
 * need for separate codec classes. All functionality uses existing bus-xx framework utilities.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRouter implements OAuth2Router {

    /**
     * Standard OAuth2 parameter names.
     * <p>
     * These parameters are filtered out and will not be extracted as extra parameters.
     * </p>
     */
    protected static final String[] STANDARD_PARAMS = { "client_id", "response_type", "redirect_uri", "scope", "state",
            "code", "grant_type", "refresh_token", "access_token", "client_secret", "username", "password", "prompt",
            "corp_id", "corpId" };

    @Override
    public String buildUrl(
            String authUrl,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> params) {
        // Build authorization URL using Builder
        Builder builder = Builder.fromUrl(authUrl).queryParam("client_id", clientId).queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri);

        // Optional parameters
        if (scope != null) {
            builder.queryParam("scope", scope);
        }
        if (state != null) {
            builder.queryParam("state", state);
        }

        // Add platform-specific parameters
        addPlatformAuthorizeParams(builder, params);

        // Add non-standard parameters
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!isStandardParam(entry.getKey())) {
                    builder.queryParam(entry.getKey(), StringKit.toString(entry.getValue()));
                }
            }
        }

        String authorizeUrl = builder.build();
        Logger.debug("Built authorize URL: {}", authorizeUrl);
        return authorizeUrl;
    }

    @Override
    public Authorization getToken(
            Callback callback,
            String tokenUrl,
            String clientId,
            String clientSecret,
            String redirectUri,
            Map<String, Object> params) {
        try {
            // Build token request using Builder
            Builder builder = Builder.fromUrl(tokenUrl).queryParam("grant_type", "authorization_code")
                    .queryParam("client_id", clientId).queryParam("client_secret", clientSecret)
                    .queryParam("code", callback.getCode()).queryParam("redirect_uri", redirectUri);

            // Add platform-specific parameters
            addPlatformTokenParams(builder, callback, params);

            // Add non-standard parameters
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (!isStandardParam(entry.getKey())) {
                        builder.queryParam(entry.getKey(), StringKit.toString(entry.getValue()));
                    }
                }
            }

            String url = builder.build();
            Logger.debug("Token request URL: {}", url);

            // Send request
            String body = Httpx.get(url);
            Logger.debug("Token response body: {}", body);

            Map<String, Object> data = JsonKit.toMap(body);

            // Build Authorization object
            return buildAuthorization(data);
        } catch (Exception e) {
            Logger.error("Failed to get token", e);
            throw new RuntimeException("Failed to get token: " + e.getMessage(), e);
        }
    }

    @Override
    public Claims getUserinfo(Authorization authorization, String userinfoUrl) {
        try {
            // Build request headers
            Map<String, String> headers = new HashMap<>();
            addUserinfoHeaders(headers, authorization);

            // Build user info URL using Builder
            Builder builder = Builder.fromUrl(userinfoUrl);
            addPlatformUserinfoParams(builder, authorization);

            String url = builder.build();
            Logger.debug("Userinfo request URL: {}", url);

            // Send request
            String body = Httpx.get(url, null, headers);
            Logger.debug("Userinfo response body: {}", body);

            Map<String, Object> data = JsonKit.toMap(body);

            // Build Claims object
            return buildClaims(data, body);
        } catch (Exception e) {
            Logger.error("Failed to get userinfo", e);
            throw new RuntimeException("Failed to get userinfo: " + e.getMessage(), e);
        }
    }

    /**
     * Adds platform-specific authorization parameters.
     * <p>
     * Subclasses can override this method to add platform-specific parameters.
     * </p>
     *
     * @param builder the URL builder
     * @param params  extra parameters for platform-specific extensions
     */
    protected void addPlatformAuthorizeParams(Builder builder, Map<String, Object> params) {
        // Default implementation adds nothing, subclasses can override
    }

    /**
     * Adds platform-specific token request parameters.
     * <p>
     * Subclasses can override this method to add platform-specific parameters.
     * </p>
     *
     * @param builder  the URL builder
     * @param callback the callback information containing the authorization code
     * @param params   extra parameters for platform-specific extensions
     */
    protected void addPlatformTokenParams(Builder builder, Callback callback, Map<String, Object> params) {
        // Default implementation adds nothing, subclasses can override
    }

    /**
     * Adds user info request headers.
     * <p>
     * Subclasses can override this method to add platform-specific headers.
     * </p>
     *
     * @param headers       the request headers map
     * @param authorization the authorization information
     */
    protected void addUserinfoHeaders(Map<String, String> headers, Authorization authorization) {
        if (authorization.getToken() != null) {
            headers.put(HTTP.AUTHORIZATION, HTTP.BEARER + authorization.getToken());
        }
    }

    /**
     * Adds platform-specific user info request parameters.
     * <p>
     * Subclasses can override this method to add platform-specific parameters.
     * </p>
     *
     * @param builder       the URL builder
     * @param authorization the authorization information
     */
    protected void addPlatformUserinfoParams(Builder builder, Authorization authorization) {
        // Default implementation adds nothing, subclasses can override
    }

    /**
     * Builds an Authorization object from response data.
     * <p>
     * Subclasses can override this method to handle platform-specific response formats.
     * </p>
     *
     * @param data the response data
     * @return an Authorization object
     */
    protected Authorization buildAuthorization(Map<String, Object> data) {
        return Authorization.builder().token((String) data.get("access_token")).expireIn(getInt(data, "expires_in"))
                .refresh((String) data.get("refresh_token")).scope((String) data.get("scope")).build();
    }

    /**
     * Builds a Claims object from response data.
     * <p>
     * Subclasses can override this method to handle platform-specific response formats.
     * </p>
     *
     * @param data    the response data
     * @param rawJson the raw JSON string
     * @return a Claims object
     */
    protected Claims buildClaims(Map<String, Object> data, String rawJson) {
        Claims claims = Claims.builder().uuid((String) data.get("id")).username((String) data.get("name"))
                .email((String) data.get("email")).nickname((String) data.get("nickname"))
                .avatar((String) data.get("avatar")).rawJson(rawJson).build();

        // Extract information from attributes
        if (data.containsKey("attributes")) {
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            if (attributes != null) {
                if (claims.getUsername() == null) {
                    claims.setUsername((String) attributes.get("userName"));
                }
                if (claims.getEmail() == null) {
                    claims.setEmail((String) attributes.get("email"));
                }
            }
        }

        return claims;
    }

    /**
     * Encodes the state parameter.
     * <p>
     * Encodes redirectUri and state into a single state parameter using Base64 encoding.
     * </p>
     *
     * @param redirectUri the redirect URI
     * @param state       the original state
     * @return the encoded state
     */
    protected String encodeState(String redirectUri, String state) {
        Map<String, String> params = new HashMap<>();
        params.put("redirectUri", redirectUri);
        params.put("state", state);

        String json = JsonKit.toJsonString(params);
        return "oauth:" + Base64.encode(json.getBytes(Charset.UTF_8), false);
    }

    /**
     * Decodes the state parameter.
     * <p>
     * Extracts redirectUri and original state from the encoded state.
     * </p>
     *
     * @param encodedState the encoded state
     * @return a map containing redirectUri and state
     */
    protected Map<String, String> decodeState(String encodedState) {
        if (!encodedState.startsWith("oauth:")) {
            // Not an encoded state, return as is
            Map<String, String> result = new HashMap<>();
            result.put("state", encodedState);
            return result;
        }

        String encoded = encodedState.substring(6);
        String json = new String(Base64.decode(encoded), Charset.UTF_8);
        return JsonKit.toPojo(json, Map.class);
    }

    /**
     * Encodes a token with a prefix.
     * <p>
     * Adds a platform prefix to the token to distinguish different platforms.
     * </p>
     *
     * @param token  the original token
     * @param prefix the platform prefix
     * @return the encoded token
     */
    protected String encodeToken(String token, String prefix) {
        return prefix + ":" + token;
    }

    /**
     * Decodes a token by removing the prefix.
     * <p>
     * Removes the platform prefix from the token.
     * </p>
     *
     * @param encodedToken the encoded token
     * @param prefix       the platform prefix
     * @return the original token
     */
    protected String decodeToken(String encodedToken, String prefix) {
        if (encodedToken.startsWith(prefix + ":")) {
            return encodedToken.substring(prefix.length() + 1);
        }
        return encodedToken;
    }

    /**
     * Sends a POST JSON request.
     *
     * @param url  the request URL
     * @param data the request body data
     * @return the response string
     */
    protected String postJson(String url, Map<String, Object> data) {
        return postJson(url, data, null);
    }

    /**
     * Sends a POST JSON request with headers.
     *
     * @param url     the request URL
     * @param data    the request body data
     * @param headers the request headers
     * @return the response string
     */
    protected String postJson(String url, Map<String, Object> data, Map<String, String> headers) {
        String json = JsonKit.toJsonString(data);
        return Httpx.post(url, json, headers, MediaType.APPLICATION_JSON);
    }

    /**
     * Checks if a parameter is a standard OAuth2 parameter.
     *
     * @param param the parameter name
     * @return {@code true} if standard, {@code false} otherwise
     */
    protected boolean isStandardParam(String param) {
        if (StringKit.isEmpty(param)) {
            return false;
        }

        for (String standard : STANDARD_PARAMS) {
            if (standard.equalsIgnoreCase(param)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets an integer value from a map.
     *
     * @param map the map to get the value from
     * @param key the key to look up
     * @return the integer value, or 0 if not found or not a number
     */
    protected int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

}
