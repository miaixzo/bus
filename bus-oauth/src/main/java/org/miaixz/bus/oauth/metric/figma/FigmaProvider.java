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
package org.miaixz.bus.oauth.metric.figma;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;

/**
 * Figma 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FigmaProvider extends AbstractProvider {

    public FigmaProvider(Context context) {
        super(context, Registry.FIGMA);
    }

    public FigmaProvider(Context context, ExtendCache cache) {
        super(context, Registry.FIGMA, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(",", true, getDefaultScopes(FigmaScope.values()))).build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization",
                "Basic ".concat(Base64.encode((this.context.getAppKey().concat(":").concat(this.context.getAppSecret()))
                        .getBytes(StandardCharsets.UTF_8))));
        String response = Httpx.post(super.accessTokenUrl(callback.getCode()), headers);

        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token")).scope(accessTokenObject.getString("scope"))
                .userId(accessTokenObject.getString("user_id")).expireIn(accessTokenObject.getIntValue("expires_in"))
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(super.accessTokenUrl(accToken.getCode()), headers);
        JSONObject dataObj = JSONObject.parseObject(response);

        this.checkResponse(dataObj);

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder().accessToken(dataObj.getString("access_token"))
                        .openId(dataObj.getString("open_id")).expireIn(dataObj.getIntValue("expires_in"))
                        .refreshToken(dataObj.getString("refresh_token")).scope(dataObj.getString("scope")).build())
                .build();

    }

    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh()).queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret()).queryParam("refresh_token", refreshToken).build();
    }

    @Override
    public Material getUserInfo(AccToken AccToken) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization", "Bearer " + AccToken.getAccessToken());
        String response = Httpx.post(super.accessTokenUrl(AccToken.getCode()), headers);
        JSONObject dataObj = JSONObject.parseObject(response);
        this.checkResponse(dataObj);
        return Material.builder().rawJson(dataObj).uuid(dataObj.getString("id")).username(dataObj.getString("handle"))
                .avatar(dataObj.getString("img_url")).email(dataObj.getString("email")).token(AccToken)
                .source(complex.toString()).build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error") + ":" + object.getString("message"));
        }
    }

}
