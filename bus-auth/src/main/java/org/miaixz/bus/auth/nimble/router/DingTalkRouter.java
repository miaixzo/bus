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
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;

/**
 * DingTalk OAuth2 router implementation.
 * <p>
 * Implements DingTalk OAuth2 protocol forwarding with support for DingTalk-specific parameters and workflows.
 * </p>
 * <p>
 * Supports two modes:
 * <ul>
 * <li>Scan code login mode: User scans QR code to log in</li>
 * <li>Client credentials mode: Enterprise internal application obtains access token</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DingTalkRouter extends AbstractRouter {

    /**
     * The client token endpoint URL.
     */
    private final String tokenUrl;

    /**
     * The client user info endpoint URL.
     */
    private final String userinfoUrl;

    /**
     * The client user detail endpoint URL.
     */
    private final String userdetailUrl;

    /**
     * The separator used to distinguish client mode tokens.
     */
    private static final String CODE_SEPARATOR = "__dt__";

    /**
     * Creates a DingTalk OAuth2 router.
     *
     * @param tokenUrl      the client token endpoint URL
     * @param userinfoUrl   the client user info endpoint URL
     * @param userdetailUrl the client user detail endpoint URL
     */
    public DingTalkRouter(String tokenUrl, String userinfoUrl, String userdetailUrl) {
        this.tokenUrl = tokenUrl;
        this.userinfoUrl = userinfoUrl;
        this.userdetailUrl = userdetailUrl;
    }

    @Override
    protected void addPlatformAuthorizeParams(Builder builder, Map<String, Object> extraParams) {
        // DingTalk-specific parameters
        String prompt = "consent";
        if (extraParams != null && extraParams.containsKey("prompt")) {
            prompt = StringKit.toString(extraParams.get("prompt"));
        }
        builder.queryParam("prompt", prompt);

        if (extraParams != null && extraParams.containsKey("corpId")) {
            builder.queryParam("corpId", StringKit.toString(extraParams.get("corpId")));
        }
    }

    @Override
    public Authorization getToken(
            Callback callback,
            String tokenUrl,
            String clientId,
            String clientSecret,
            String redirectUri,
            Map<String, Object> extraParams) {
        // Check if this is client mode (by checking if code contains special separator)
        boolean isClient = callback.getCode() != null && callback.getCode().contains(CODE_SEPARATOR);

        if (isClient) {
            return getClientToken(callback, tokenUrl, clientId, clientSecret, extraParams);
        } else {
            return getUserToken(callback, tokenUrl, clientId, clientSecret);
        }
    }

    /**
     * Obtains the client credentials mode token.
     * <p>
     * Used for enterprise internal applications to obtain access tokens.
     * </p>
     *
     * @param callback     the callback information containing the authorization code
     * @param tokenUrl     the token endpoint URL
     * @param clientId     the client identifier
     * @param clientSecret the client secret
     * @param extraParams  extra parameters for platform-specific extensions
     * @return an Authorization object containing the access token
     */
    private Authorization getClientToken(
            Callback callback,
            String tokenUrl,
            String clientId,
            String clientSecret,
            Map<String, Object> extraParams) {
        try {
            // Parse code (format: code__dt__corpId)
            String[] parts = callback.getCode().split(CODE_SEPARATOR);
            String code = parts[0];
            String corpId = parts.length > 1 ? parts[1] : null;

            if (corpId == null && extraParams != null && extraParams.containsKey("corpId")) {
                corpId = StringKit.toString(extraParams.get("corpId"));
            }

            Map<String, Object> params = new HashMap<>();
            params.put("grant_type", "client_credentials");
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);

            // Replace corp_id placeholder
            String url = this.tokenUrl.replace("{corp_id}", corpId);

            Logger.debug("DingTalk client token request - URL: {}, Params: {}", url, params);

            String body = postJson(url, params);
            Logger.debug("DingTalk client token response: {}", body);

            Map<String, Object> data = JsonKit.toMap(body);

            return Authorization.builder().token((String) data.get("accessToken")).expireIn(getInt(data, "expireIn"))
                    .code(code) // Save code for subsequent user info retrieval
                    .build();
        } catch (Exception e) {
            Logger.error("Failed to get DingTalk client token", e);
            throw new RuntimeException("Failed to get DingTalk client token: " + e.getMessage(), e);
        }
    }

    /**
     * Obtains the user authorization mode token.
     * <p>
     * Used for scan code login to obtain user access tokens.
     * </p>
     *
     * @param callback     the callback information containing the authorization code
     * @param tokenUrl     the token endpoint URL
     * @param clientId     the client identifier
     * @param clientSecret the client secret
     * @return an Authorization object containing the access token
     */
    private Authorization getUserToken(Callback callback, String tokenUrl, String clientId, String clientSecret) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("grantType", "authorization_code");
            params.put("clientId", clientId);
            params.put("clientSecret", clientSecret);
            params.put("code", callback.getCode());

            Logger.debug("DingTalk user token request - URL: {}, Params: {}", tokenUrl, params);

            String body = postJson(tokenUrl, params);
            Logger.debug("DingTalk user token response: {}", body);

            Map<String, Object> data = JsonKit.toMap(body);

            return Authorization.builder().token((String) data.get("accessToken")).expireIn(getInt(data, "expireIn"))
                    .build();
        } catch (Exception e) {
            Logger.error("Failed to get DingTalk user token", e);
            throw new RuntimeException("Failed to get DingTalk user token: " + e.getMessage(), e);
        }
    }

    @Override
    protected void addUserinfoHeaders(Map<String, String> headers, Authorization authorization) {
        headers.put("x-acs-dingtalk-access-token", authorization.getToken());
    }

    @Override
    public Claims getUserinfo(Authorization authorization, String userinfoUrl) {
        // Check if this is client mode (by checking if token contains code)
        boolean isClient = authorization.getCode() != null && !authorization.getCode().isEmpty();

        if (isClient) {
            return getClientUserinfo(authorization);
        } else {
            return getUserUserinfo(authorization, userinfoUrl);
        }
    }

    /**
     * Obtains client mode user information.
     * <p>
     * Two-step retrieval: first obtains userId, then obtains user details.
     * </p>
     *
     * @param authorization the authorization information
     * @return a Claims object containing user information
     */
    private Claims getClientUserinfo(Authorization authorization) {
        try {
            String accessToken = authorization.getToken();
            String code = authorization.getCode();

            if (code == null) {
                throw new IllegalArgumentException("Code is required for client userinfo");
            }

            // Step 1: Get userId
            Map<String, String> headers = new HashMap<>();
            headers.put("x-acs-dingtalk-access-token", accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("code", code);

            Logger.debug("DingTalk client userinfo (step 1) - URL: {}, Body: {}", userinfoUrl, body);

            String responseBody = postJson(userinfoUrl, body, headers);
            Logger.debug("DingTalk client userinfo (step 1) response: {}", responseBody);

            Map<String, Object> data = JsonKit.toMap(responseBody);
            Map<String, Object> result = (Map<String, Object>) data.get("result");
            String userId = (String) result.get("userid");

            // Step 2: Get user details
            Map<String, Object> detailBody = new HashMap<>();
            detailBody.put("userid", userId);

            Logger.debug("DingTalk client userinfo (step 2) - URL: {}, Body: {}", userdetailUrl, detailBody);

            String detailBodyStr = postJson(userdetailUrl, detailBody, headers);
            Logger.debug("DingTalk client userinfo (step 2) response: {}", detailBodyStr);

            Map<String, Object> detailData = JsonKit.toMap(detailBodyStr);
            Map<String, Object> detailResult = (Map<String, Object>) detailData.get("result");

            return Claims.builder().uuid(userId).username((String) detailResult.get("name"))
                    .email((String) detailResult.get("email")).avatar((String) detailResult.get("avatar"))
                    .rawJson(detailBodyStr).build();
        } catch (Exception e) {
            Logger.error("Failed to get DingTalk client userinfo", e);
            throw new RuntimeException("Failed to get DingTalk client userinfo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtains user mode user information.
     * <p>
     * Retrieves user information after scan code login.
     * </p>
     *
     * @param authorization the authorization information
     * @param userinfoUrl   the user info endpoint URL
     * @return a Claims object containing user information
     */
    private Claims getUserUserinfo(Authorization authorization, String userinfoUrl) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-acs-dingtalk-access-token", authorization.getToken());

            Logger.debug("DingTalk user userinfo - URL: {}", userinfoUrl);

            String body = Httpx.get(userinfoUrl, null, headers);
            Logger.debug("DingTalk user userinfo response: {}", body);

            Map<String, Object> data = JsonKit.toMap(body);

            return Claims.builder().uuid((String) data.get("unionId")).rawJson(body).build();
        } catch (Exception e) {
            Logger.error("Failed to get DingTalk user userinfo", e);
            throw new RuntimeException("Failed to get DingTalk user userinfo: " + e.getMessage(), e);
        }
    }

}
