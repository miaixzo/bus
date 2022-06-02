package org.aoju.bus.pay.provider.paypal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.pay.metric.RetryKit;

import java.io.Serializable;
import java.util.Map;

/**
 * 授权令牌
 */
public class AccessToken implements Serializable, RetryKit.ResultCheck {

    private static final long serialVersionUID = 1L;
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
            JSONObject jsonObject = JSON.parseObject(json);
            this.access_token = jsonObject.getString("access_token");
            this.expires_in = jsonObject.getInteger("expires_in");
            this.app_id = jsonObject.getString("app_id");
            this.token_type = jsonObject.getString("token_type");
            if (expires_in != null) {
                this.expiredTime = System.currentTimeMillis() + ((expires_in - 9) * 1000);
            }
            if (jsonObject.containsKey("expiredTime")) {
                this.expiredTime = jsonObject.getLong("expiredTime");
            }
            if (jsonObject.containsKey("status")) {
                this.status = jsonObject.getInteger("status");
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
