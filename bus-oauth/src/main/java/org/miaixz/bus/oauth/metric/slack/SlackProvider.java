/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.slack;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Slack 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SlackProvider extends AbstractProvider {

    public SlackProvider(Context context) {
        super(context, Registry.SLACK);
    }

    public SlackProvider(Context context, ExtendCache cache) {
        super(context, Registry.SLACK, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.get(accessTokenUrl(callback.getCode()), null, header);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .scope(accessTokenObject.getString("scope")).tokenType(accessTokenObject.getString("token_type"))
                .uid(accessTokenObject.getJSONObject("authed_user").getString("id")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization", "Bearer ".concat(accToken.getAccessToken()));
        String userInfo = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        JSONObject user = object.getJSONObject("user");
        JSONObject profile = user.getJSONObject("profile");
        return Material.builder().rawJson(user).uuid(user.getString("id")).username(user.getString("name"))
                .nickname(user.getString("real_name")).avatar(profile.getString("image_original"))
                .email(profile.getString("email")).gender(Gender.UNKNOWN).token(accToken).source(complex.toString())
                .build();
    }

    @Override
    public Message revoke(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization", "Bearer ".concat(accToken.getAccessToken()));
        String userInfo = Httpx.get(complex.revoke(), null, header);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        // 返回1表示取消授权成功，否则失败
        ErrorCode status = object.getBooleanValue("revoked") ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (!object.getBooleanValue("ok")) {
            String errorMsg = object.getString("error");
            if (object.containsKey("response_metadata")) {
                JSONArray array = object.getJSONObject("response_metadata").getJSONArray("messages");
                if (null != array && array.size() > 0) {
                    errorMsg += "; " + StringKit.join(Symbol.COMMA, array.toArray(new String[0]));
                }
            }
            throw new AuthorizedException(errorMsg);
        }
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("user", accToken.getUid()).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("client_id", context.getAppKey())
                .queryParam("state", getRealState(state)).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, this.getDefaultScopes(SlackScope.values())))
                .build();
    }

    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}
