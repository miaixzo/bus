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
package org.miaixz.bus.oauth.metric.gitlab;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;

/**
 * Gitlab 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GitlabProvider extends AbstractProvider {

    public GitlabProvider(Context context) {
        super(context, Registry.GITLAB);
    }

    public GitlabProvider(Context context, ExtendCache cache) {
        super(context, Registry.GITLAB, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder().accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token")).idToken(object.getString("id_token"))
                .tokenType(object.getString("token_type")).scope(object.getString("scope")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Material.builder().rawJson(object).uuid(object.getString("id")).username(object.getString("username"))
                .nickname(object.getString("name")).avatar(object.getString("avatar_url"))
                .blog(object.getString("web_url")).company(object.getString("organization"))
                .location(object.getString("location")).email(object.getString("email")).remark(object.getString("bio"))
                .gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
    }

    private void checkResponse(JSONObject object) {
        // oauth/token 验证异常
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
        // user 验证异常
        if (object.containsKey("message")) {
            throw new AuthorizedException(object.getString("message"));
        }
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
                .queryParam("scope", this.getScopes(Symbol.PLUS, false, this.getDefaultScopes(GitlabScope.values())))
                .build();
    }

}
