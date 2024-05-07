/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.metric.xmly;

import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.HttpUtil;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 喜马拉雅 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlyProvider extends DefaultProvider {

    public XmlyProvider(Context context) {
        super(context, Registry.XMLY);
    }

    public XmlyProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.XMLY, authorizeCache);
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see DefaultProvider#authorize(String)
     */
    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, String> map = new HashMap<>(9);
        map.put("code", authCallback.getCode());
        map.put("client_id", context.getAppKey());
        map.put("client_secret", context.getAppSecret());
        map.put("device_id", context.getDeviceId());
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", context.getRedirectUri());
        String response = HttpUtil.post(complex.accessToken(), map, true).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);

        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .uid(accessTokenObject.getString("uid"))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(complex.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .queryParam("client_os_type", "3")
                .queryParam("device_id", context.getDeviceId())
                .build();
    }

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see DefaultProvider#getAccessToken(Callback)
     */
    @Override
    public Property getUserInfo(AccToken accToken) {
        Map<String, String> map = new TreeMap<>();
        map.put("app_key", context.getAppKey());
        map.put("client_os_type", Optional.ofNullable(context.getClientOsType()).orElse(3).toString());
        map.put("device_id", context.getDeviceId());
        map.put("pack_id", context.getPackId());
        map.put("access_token", accToken.getAccessToken());
        map.put("sig", Builder.generateXmlySignature(map, context.getAppSecret()));
        String rawUserInfo = HttpUtil.get(complex.userInfo(), map, false).getBody();
        JSONObject object = JSONObject.parseObject(rawUserInfo);
        checkResponse(object);
        return Property.builder()
                .uuid(object.getString("id"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("avatar_url"))
                .rawJson(object)
                .source(complex.toString())
                .token(accToken)
                .gender(Gender.UNKNOWN)
                .build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("errcode")) {
            throw new AuthorizedException(object.getString("error_no"), object.getString("error_desc"));
        }
    }

}
