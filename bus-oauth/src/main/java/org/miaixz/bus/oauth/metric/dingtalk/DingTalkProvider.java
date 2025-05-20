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
package org.miaixz.bus.oauth.metric.dingtalk;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;

/**
 * 钉钉 二维码登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DingTalkProvider extends AbstractDingtalkProvider {

    public DingTalkProvider(Context context) {
        super(context, Registry.DINGTALK);
    }

    public DingTalkProvider(Context context, ExtendCache cache) {
        super(context, Registry.DINGTALK, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, getDefaultScopes(DingTalkScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("prompt", "consent")
                .queryParam("org_type", context.getType()).queryParam("corpId", context.getUnionId())
                .queryParam("exclusiveLogin", context.getLoginType()).queryParam("exclusiveCorpId", context.getExtId())
                .queryParam("state", getRealState(state)).build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("grantType", "authorization_code");
        params.put("clientId", context.getAppKey());
        params.put("clientSecret", context.getAppSecret());
        params.put("code", callback.getCode());
        String response = Httpx.get(this.complex.accessToken(), JsonKit.toJsonString(params));
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            if (!accessTokenObject.containsKey("accessToken")) {
                throw new AuthorizedException(
                        "Missing accessToken in response: " + JsonKit.toJsonString(accessTokenObject));
            }

            String accessToken = (String) accessTokenObject.get("accessToken");
            String refreshToken = (String) accessTokenObject.get("refreshToken");
            Object expireInObj = accessTokenObject.get("expireIn");
            int expireIn = expireInObj instanceof Number ? ((Number) expireInObj).intValue() : 0;
            String corpId = (String) accessTokenObject.get("corpId");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expireIn)
                    .unionId(corpId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", authToken.getAccessToken());
        String response = Httpx.get(this.complex.userInfo(), new HashMap<>(0), header);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            String openId = (String) object.get("openId");
            String unionId = (String) object.get("unionId");
            if (unionId == null) {
                throw new AuthorizedException("Missing unionId in user info response");
            }
            String nick = (String) object.get("nick");
            String avatarUrl = (String) object.get("avatarUrl");
            Object visitorObj = object.get("visitor");
            boolean visitor = visitorObj instanceof Boolean ? (Boolean) visitorObj : false;

            authToken.setOpenId(openId);
            authToken.setUnionId(unionId);

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionId).username(nick).nickname(nick)
                    .avatar(avatarUrl).snapshotUser(visitor).token(authToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code)
                .queryParam("clientId", context.getAppKey()).queryParam("clientSecret", context.getAppSecret())
                .queryParam("grantType", "authorization_code").build();
    }

}