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
package org.miaixz.bus.oauth.metric.stackoverflow;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;

/**
 * Stack Overflow 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StackOverflowProvider extends AbstractProvider {

    public StackOverflowProvider(Context context) {
        super(context, Registry.STACK_OVERFLOW);
    }

    public StackOverflowProvider(Context context, ExtendCache cache) {
        super(context, Registry.STACK_OVERFLOW, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String accessTokenUrl = accessTokenUrl(callback.getCode());
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(accessTokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(accessTokenUrl, form, header);

        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);

        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String userInfoUrl = Builder.fromUrl(this.complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken()).queryParam("site", "stackoverflow")
                .queryParam("key", this.context.getUnionId()).build();
        String response = Httpx.get(userInfoUrl);
        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        JSONObject userObj = object.getJSONArray("items").getJSONObject(0);

        return Material.builder().rawJson(userObj).uuid(userObj.getString("user_id"))
                .avatar(userObj.getString("profile_image")).location(userObj.getString("location"))
                .nickname(userObj.getString("display_name")).blog(userObj.getString("website_url"))
                .gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope",
                        this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(StackoverflowScope.values())))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

}
