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
package org.miaixz.bus.auth.nimble.eleme;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Ele.me login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ElemeProvider extends AbstractProvider {

    /**
     * Constructs an {@code ElemeProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public ElemeProvider(Context context) {
        super(context, Registry.ELEME);
    }

    /**
     * Constructs an {@code ElemeProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public ElemeProvider(Context context, CacheX cache) {
        super(context, Registry.ELEME, cache);
    }

    /**
     * Generates the signature for an Ele.me request.
     *
     * @param appKey     the application key of the platform
     * @param secret     the application secret of the platform
     * @param timestamp  the timestamp in seconds. The API server allows a maximum time difference of plus or minus 5
     *                   minutes.
     * @param action     the API method of the Ele.me request
     * @param token      the user's authorization token
     * @param parameters the parameters to be included in the signature
     * @return the generated signature
     */
    public static String sign(
            String appKey,
            String secret,
            long timestamp,
            String action,
            String token,
            Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>(parameters);
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append(Symbol.EQUAL).append(JsonKit.toJsonString(entry.getValue()));
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        String calculatedSignature = org.miaixz.bus.crypto.Builder.md5Hex(splice);
        return calculatedSignature.toUpperCase();
    }

    /**
     * Retrieves the access token from Ele.me's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getClientId());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("code", callback.getCode());
        form.put("grant_type", "authorization_code");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);

        String response = Httpx.post(this.complex.token(), form, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refresh = (String) object.get("refresh_token");
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Authorization.builder().token(token).refresh(refresh).token_type(tokenType)
                                    .expireIn(expiresIn).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if parsing the response fails or an error occurs during token refresh
     */
    @Override
    public Message refresh(Authorization authorization) {
        Map<String, String> form = new HashMap<>(4);
        form.put("refresh_token", authorization.getRefresh());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);
        String response = Httpx.post(this.complex.refresh(), form, header);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }

            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refresh = (String) object.get("refresh_token");
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Authorization.builder().token(token).refresh(refresh).token_type(tokenType)
                                    .expireIn(expiresIn).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Ele.me's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, Object> parameters = new HashMap<>(4);
        // API method name for getting merchant account information
        String action = "eleme.user.getUser";
        // Timestamp in seconds. The API server allows a maximum time error of plus or minus 5 minutes.
        final long timestamp = System.currentTimeMillis();
        // Common parameters
        Map<String, Object> metasHashMap = new HashMap<>(4);
        metasHashMap.put("app_key", context.getClientId());
        metasHashMap.put("timestamp", timestamp);
        String signature = sign(
                context.getClientId(),
                context.getClientSecret(),
                timestamp,
                action,
                authorization.getToken(),
                parameters);

        String requestId = this.getRequestId();

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("nop", "1.0.0");
        paramsMap.put("id", requestId);
        paramsMap.put("action", action);
        paramsMap.put("token", authorization.getToken());
        paramsMap.put("metas", metasHashMap);
        paramsMap.put("params", parameters);
        paramsMap.put("signature", signature);

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_JSON, requestId, false);
        String response = Httpx
                .post(this.complex.userinfo(), JsonKit.toJsonString(paramsMap), header, MediaType.APPLICATION_JSON);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            // Validate request
            if (object.containsKey("name")) {
                String message = (String) object.get("message");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }
            if (object.containsKey("error") && object.get("error") != null) {
                Map<String, Object> error = (Map<String, Object>) object.get("error");
                String errorMessage = (String) error.get("message");
                throw new AuthorizedException(errorMessage != null ? errorMessage : "Unknown error");
            }

            Map<String, Object> result = (Map<String, Object>) object.get("result");
            if (result == null) {
                throw new AuthorizedException("Missing result field in user info response");
            }

            String userId = (String) result.get("userId");
            if (userId == null) {
                throw new AuthorizedException("Missing userId in user info response");
            }
            String userName = (String) result.get("userName");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(result)).uuid(userId).username(userName)
                                    .nickname(userName).gender(Gender.UNKNOWN).token(authorization)
                                    .source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Generates the Basic Authorization header value.
     *
     * @param appKey    the application key
     * @param appSecret the application secret
     * @return the Basic Authorization header string
     */
    private String getBasic(String appKey, String appSecret) {
        StringBuilder sb = new StringBuilder();
        String encodeToString = Base64.encode((appKey + Symbol.COLON + appSecret).getBytes());
        sb.append("Basic").append(Symbol.SPACE).append(encodeToString);
        return sb.toString();
    }

    /**
     * Builds the HTTP headers for a request.
     *
     * @param contentType the Content-Type header value
     * @param requestId   the X-Eleme-Requestid header value
     * @param auth        whether to include the Authorization header
     * @return a map of HTTP headers
     */
    private Map<String, String> buildHeader(String contentType, String requestId, boolean auth) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.ACCEPT, "text/xml,text/javascript,text/html");
        header.put(HTTP.CONTENT_TYPE, contentType);
        header.put(HTTP.ACCEPT_ENCODING, "gzip");
        header.put(HTTP.USER_AGENT, "eleme-openapi-java-sdk");
        header.put("x-eleme-requestid", requestId);

        if (auth) {
            header.put("Authorization", this.getBasic(context.getClientId(), context.getClientSecret()));
        }
        return header;
    }

    /**
     * Generates a unique request ID.
     *
     * @return a unique request ID string
     */
    private String getRequestId() {
        return (ID.objectId() + "|" + System.currentTimeMillis()).toUpperCase();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Builder.fromUrl((String) super.build(state).getData()).queryParam("scope", "all").build())
                .build();
    }

}
