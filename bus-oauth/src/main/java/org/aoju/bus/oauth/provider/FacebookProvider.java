/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * Facebook登录
 *
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public class FacebookProvider extends DefaultProvider {

    public FacebookProvider(Context context) {
        super(context, Registry.FACEBOOK);
    }

    public FacebookProvider(Context context, StateCache stateCache) {
        super(context, Registry.FACEBOOK, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        JSONObject object = JSONObject.parseObject(doPostAuthorizationCode(Callback.getCode()));
        this.checkResponse(object);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .tokenType(object.getString("token_type"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));
        this.checkResponse(object);
        return Property.builder()
                .uuid(object.getString("id"))
                .username(object.getString("name"))
                .nickname(object.getString("name"))
                .avatar(getUserPicture(object))
                .location(object.getString("locale"))
                .email(object.getString("email"))
                .gender(Normal.Gender.getGender(object.getString("gender")))
                .token(token)
                .source(source.toString())
                .build();
    }

    private String getUserPicture(JSONObject object) {
        String picture = null;
        if (object.containsKey("picture")) {
            JSONObject pictureObj = object.getJSONObject("picture");
            pictureObj = pictureObj.getJSONObject("data");
            if (null != pictureObj) {
                picture = pictureObj.getString("url");
            }
        }
        return picture;
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("fields", "id,name,birthday,gender,hometown,email,devices,picture.width(400)")
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new InstrumentException(object.getJSONObject("error").getString("message"));
        }
    }

}
