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
package org.miaixz.bus.pay.metric.paypal.entity;

import java.io.Serializable;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.pay.magic.Callback;

/**
 * AccessToken
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AccessToken implements Serializable, Callback {

    private static final long serialVersionUID = -1L;
    private String access_token;
    private String token_type;
    private String app_id;
    private Integer expires_in;
    private Long expiredTime;
    private String json;
    /**
     * http 请求状态码
     */
    private Integer status;

    public AccessToken(String json, int httpCode) {
        this.json = json;
        this.status = httpCode;
        try {
            this.access_token = JsonKit.getValue(this.json, "access_token");
            this.expires_in = JsonKit.getValue(this.json, "expires_in");
            this.app_id = JsonKit.getValue(this.json, "app_id");
            this.token_type = JsonKit.getValue(this.json, "token_type");
            this.expiredTime = JsonKit.getValue(this.json, "expiredTime");
            this.status = JsonKit.getValue(this.json, "status");

            if (expires_in != null) {
                this.expiredTime = System.currentTimeMillis() + ((expires_in - 9) * 1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public String getCacheJson() {
        Map<String, Object> temp = JsonKit.toPojo(json, Map.class);
        temp.put("expiredTime", expiredTime);
        temp.remove("expires_in");
        temp.remove("scope");
        temp.remove("nonce");
        return JsonKit.toJsonString(temp);
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String tokenType) {
        this.token_type = tokenType;
    }

    public String getAppId() {
        return app_id;
    }

    public void setAppId(String appId) {
        this.app_id = appId;
    }

    public Integer getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expires_in = expiresIn;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }

    @Override
    public boolean matching() {
        return isAvailable();
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
