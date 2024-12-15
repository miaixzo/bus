/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.meituan;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
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
 * 美团 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MeituanProvider extends AbstractProvider {

    public MeituanProvider(Context context) {
        super(context, Registry.MEITUAN);
    }

    public MeituanProvider(Context context, ExtendCache cache) {
        super(context, Registry.MEITUAN, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", context.getAppKey());
        form.put("secret", context.getAppSecret());
        form.put("code", callback.getCode());
        form.put("grant_type", "authorization_code");

        String response = Httpx.post(complex.accessToken(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder().accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token")).expireIn(object.getIntValue("expires_in")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> form = new HashMap<>(5);
        form.put("app_id", context.getAppKey());
        form.put("secret", context.getAppSecret());
        form.put("access_token", accToken.getAccessToken());

        String response = Httpx.post(complex.userInfo(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Material.builder().rawJson(object).uuid(object.getString("openid"))
                .username(object.getString("nickname")).nickname(object.getString("nickname"))
                .avatar(object.getString("avatar")).gender(Gender.UNKNOWN).token(accToken).source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", context.getAppKey());
        form.put("secret", context.getAppSecret());
        form.put("refresh_token", oldToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        String response = Httpx.post(complex.refresh(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder().accessToken(object.getString("access_token"))
                        .refreshToken(object.getString("refresh_token")).expireIn(object.getIntValue("expires_in"))
                        .build())
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_code")) {
            throw new AuthorizedException(object.getString("erroe_msg"));
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("scope", "").build();
    }

}
