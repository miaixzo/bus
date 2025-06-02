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
package org.miaixz.bus.auth.nimble.taobao;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * 淘宝 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TaobaoProvider extends AbstractProvider {

    public TaobaoProvider(Context context) {
        super(context, Registry.TAOBAO);
    }

    public TaobaoProvider(Context context, ExtendCache cache) {
        super(context, Registry.TAOBAO, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return AccToken.builder().accessCode(callback.getCode()).build();
    }

    private AccToken getAuthToken(Map<String, Object> object) {
        this.checkResponse(object);

        return AccToken.builder().accessToken((String) object.get("access_token"))
                .expireIn(((Number) object.get("expires_in")).intValue()).tokenType((String) object.get("token_type"))
                .idToken((String) object.get("id_token")).refreshToken((String) object.get("refresh_token"))
                .uid((String) object.get("taobao_user_id")).openId((String) object.get("taobao_open_uid")).build();
    }

    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException((String) object.get("error_description"));
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doPostAuthorizationCode(accToken.getAccessCode());
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        if (accessTokenObject.containsKey("error")) {
            throw new AuthorizedException((String) accessTokenObject.get("error_description"));
        }
        accToken = this.getAuthToken(accessTokenObject);

        String nick = UrlDecoder.decode((String) accessTokenObject.get("taobao_user_nick"));
        return Material.builder().rawJson(JsonKit.toJsonString(accessTokenObject))
                .uuid(StringKit.isEmpty(accToken.getUid()) ? accToken.getOpenId() : accToken.getUid()).username(nick)
                .nickname(nick).gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        String tokenUrl = refreshTokenUrl(oldToken.getRefreshToken());
        String response = Httpx.post(tokenUrl);
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(this.getAuthToken(accessTokenObject))
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
        return Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE)).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("view", "web").queryParam("state", getRealState(state)).build();
    }

}