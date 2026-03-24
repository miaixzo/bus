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
package org.miaixz.bus.pay.metric.paypal.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.pay.magic.Callback;

/**
 * Represents a PayPal Access Token.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AccessToken implements Serializable, Callback {

    @Serial
    private static final long serialVersionUID = 2852293096397L;

    /**
     * The access token.
     */
    private String access_token;
    /**
     * The type of the token (e.g., "Bearer").
     */
    private String token_type;
    /**
     * The application ID.
     */
    private String app_id;
    /**
     * The lifetime of the token in seconds.
     */
    private Integer expires_in;
    /**
     * The calculated expiration time in milliseconds since the epoch.
     */
    private Long expiredTime;
    /**
     * The original JSON response string.
     */
    private String json;
    /**
     * The HTTP status code of the response.
     */
    private Integer status;

    /**
     * Constructs an AccessToken from a JSON response.
     *
     * @param json     The JSON response string.
     * @param httpCode The HTTP status code of the response.
     */
    public AccessToken(String json, int httpCode) {
        this.json = json;
        this.status = httpCode;
        try {
            Map<String, Object> map = JsonKit.toMap(this.json);
            this.access_token = (String) map.get("access_token");
            this.expires_in = (Integer) map.get("expires_in");
            this.app_id = (String) map.get("app_id");
            this.token_type = (String) map.get("token_type");
            this.expiredTime = (Long) map.get("expiredTime");
            this.status = (Integer) map.get("status");

            if (expires_in != null) {
                this.expiredTime = System.currentTimeMillis() + ((expires_in - 9) * 1000L);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the access token is still valid and not expired.
     *
     * @return {@code true} if the token is available, {@code false} otherwise.
     */
    public boolean isAvailable() {
        if (status != 200) {
            return false;
        }
        if (expiredTime == null) {
            return false;
        }
        if (expiredTime < System.currentTimeMillis()) {
            return false;
        }
        return StringKit.isNotEmpty(access_token);
    }

    /**
     * Generates a JSON string suitable for caching.
     *
     * @return A JSON string for caching.
     */
    public String getCacheJson() {
        Map<String, Object> temp = JsonKit.toMap(json);
        temp.put("expiredTime", expiredTime);
        temp.remove("expires_in");
        temp.remove("scope");
        temp.remove("nonce");
        return JsonKit.toJsonString(temp);
    }

    /**
     * Gets the access token.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return access_token;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken The access token.
     */
    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    /**
     * Gets the token type.
     *
     * @return The token type.
     */
    public String getTokenType() {
        return token_type;
    }

    /**
     * Sets the token type.
     *
     * @param tokenType The token type.
     */
    public void setTokenType(String tokenType) {
        this.token_type = tokenType;
    }

    /**
     * Gets the application ID.
     *
     * @return The application ID.
     */
    public String getAppId() {
        return app_id;
    }

    /**
     * Sets the application ID.
     *
     * @param appId The application ID.
     */
    public void setAppId(String appId) {
        this.app_id = appId;
    }

    /**
     * Gets the expiration time in seconds.
     *
     * @return The expiration time in seconds.
     */
    public Integer getExpiresIn() {
        return expires_in;
    }

    /**
     * Sets the expiration time in seconds.
     *
     * @param expiresIn The expiration time in seconds.
     */
    public void setExpiresIn(Integer expiresIn) {
        this.expires_in = expiresIn;
    }

    /**
     * Gets the expiration time in milliseconds.
     *
     * @return The expiration time in milliseconds.
     */
    public Long getExpiredTime() {
        return expiredTime;
    }

    /**
     * Sets the expiration time in milliseconds.
     *
     * @param expiredTime The expiration time in milliseconds.
     */
    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }

    /**
     * Checks if the response is a match, which is equivalent to checking if the token is available.
     *
     * @return {@code true} if the token is available, {@code false} otherwise.
     */
    @Override
    public boolean matching() {
        return isAvailable();
    }

    /**
     * Gets the original JSON response.
     *
     * @return The JSON string.
     */
    public String getJson() {
        return json;
    }

    /**
     * Sets the original JSON response.
     *
     * @param json The JSON string.
     */
    public void setJson(String json) {
        this.json = json;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return The HTTP status code.
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param status The HTTP status code.
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

}
