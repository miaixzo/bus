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
package org.miaixz.bus.auth.nimble.vk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * VK 登录提供者
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class VKProvider extends AbstractProvider {

    /**
     * 使用默认缓存构造 Twitter 提供者。
     *
     * @param context 上下文配置
     */
    public VKProvider(Context context) {
        super(context, Registry.TWITTER);
    }

    /**
     * 使用指定缓存构造 Twitter 提供者。
     *
     * @param context 上下文配置
     * @param cache   缓存实现
     */
    public VKProvider(Context context, CacheX cache) {
        super(context, Registry.TWITTER, cache);
    }

    /**
     * 获取授权 URL，附带 state 参数，防止 CSRF 攻击
     *
     * @param state 用于验证授权流程的参数
     * @return 授权 URL
     */
    @Override
    public String authorize(String state) {
        String realState = getRealState(state);

        Builder builder = Builder.fromUrl(super.authorize(state)).queryParam("scope",
                this.getScopes(" ", false, this.getDefaultScopes(VKScope.values())));
        if (this.context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(realState);
            String codeVerifier = Builder.codeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = Builder.codeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge).queryParam("code_challenge_method",
                    codeChallengeMethod);
            // 缓存 codeVerifier 十分钟
            this.cache.write(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }
        return builder.build();
    }

    /**
     * 获取授权后的 access token
     * 
     * @param callback 回调数据
     * @return 访问令牌对象
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        // 使用授权码获取access_token
        String response = doPostAuthorizationCode(callback);
        Map<String, String> object = JsonKit.toMap(response);
        // 验证响应结果
        this.checkResponse(object);

        // 返回 token
        return AuthToken.builder().idToken(object.get("id_token")).accessToken(object.get("access_token"))
                .refreshToken(object.get("refresh_token")).tokenType(object.get("token_type"))
                .scope(object.get("scope")).deviceId(callback.getDevice_id()).userId(object.get("user_id")).build();
    }

    /**
     * 获取用户信息。
     *
     * @param authToken 访问令牌
     * @return 用户信息对象
     * @throws IllegalArgumentException 如果解析用户信息失败
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String body = doGetUserInfo(authToken);
        Map<String, String> object = JsonKit.toMap(body);

        // 验证响应结果
        this.checkResponse(object);

        // 提取嵌套的user对象
        Map<String, String> userObj = JsonKit.toMap(object.get("user"));

        // 提取用户信息
        return Material.builder().uuid(userObj.get("user_id")).username(userObj.get("first_name"))
                .nickname(userObj.get("first_name") + " " + userObj.get("last_name")).avatar(userObj.get("avatar"))
                .email(userObj.get("email")).token(authToken).rawJson(JsonKit.toJsonString(userObj))
                .source(this.complex.toString()).build();
    }

    @Override
    public Message refresh(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authToken.getRefreshToken());
        form.put("state", ID.objectId());
        form.put("device_id", authToken.getDeviceId());
        form.put("client_id", this.context.getAppKey());
        form.put("ip", "10.10.10.10");
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getToken(form, this.complex.refresh()))
                .build();

    }

    @Override
    public Message revoke(AuthToken authToken) {
        String response = doPostRevoke(authToken);
        Map<String, String> object = JsonKit.toMap(response);
        this.checkResponse(object);
        // 返回1表示取消授权成功，否则失败

        Errors errors = object.get("response").equals("1") ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
        return Message.builder().errcode(errors.getKey()).errmsg(errors.getValue()).build();
    }

    /**
     * 使用授权码获取 access_token 的 POST 请求
     *
     * @return 获取的响应体
     */
    protected String doPostAuthorizationCode(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", this.context.getRedirectUri());
        form.put("client_id", this.context.getAppKey());
        form.put("code", callback.getCode());
        form.put("state", callback.getState());
        form.put("device_id", callback.getDevice_id());

        if (this.context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(callback.getState());
            String codeVerifier = this.cache.read(cacheKey).toString();
            form.put("code_verifier", codeVerifier);
        }

        return Httpx.post(this.complex.accessToken(), form, this.buildHeader());
    }

    private AuthToken getToken(Map<String, String> param, String url) {
        String response = Httpx.post(url, param, this.buildHeader());
        Map<String, String> object = JsonKit.toMap(response);
        this.checkResponse(object);
        return AuthToken.builder().accessToken(object.get("access_token")).tokenType(object.get("token_type"))
                .expireIn(Integer.parseInt(object.get("expires_in"))).refreshToken(object.get("refresh_token"))
                .deviceId(param.get("device_id")).build();
    }

    /**
     * 获取用户信息的 POST 请求
     *
     * @param authToken access token
     * @return 获取的响应体
     */
    protected String doGetUserInfo(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("access_token", authToken.getAccessToken());
        form.put("client_id", this.context.getAppKey());
        return Httpx.post(this.complex.userinfo(), form, this.buildHeader());
    }

    private void checkResponse(Map<String, String> object) {
        // 如果响应包含 error，说明出现问题
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.get("error_description"));
        }
        // 如果响应包含 message，说明用户信息获取失败
        if (object.containsKey("message")) {
            throw new AuthorizedException(object.get("message"));
        }
    }

    private Map<String, String> buildHeader() {
        return Map.of("Content-Type", "application/x-www-form-urlencoded");
    }

    protected String doPostRevoke(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("access_token", authToken.getAccessToken());
        form.put("client_id", this.context.getAppKey());

        return Httpx.post(this.complex.revoke(), form, this.buildHeader());
    }

}