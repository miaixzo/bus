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
package org.miaixz.bus.auth.nimble.qq;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
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
 * QQ 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqProvider extends AbstractProvider {

    public QqProvider(Context context) {
        super(context, Registry.QQ);
    }

    public QqProvider(Context context, ExtendCache cache) {
        super(context, Registry.QQ, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(callback.getCode());
        return getAuthToken(response);
    }

    @Override
    public Message refresh(AccToken accToken) {
        String response = Httpx.get(refreshTokenUrl(accToken.getRefreshToken()));
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String openId = this.getOpenId(accToken);
        String response = doGetUserInfo(accToken);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        if (!"0".equals(object.get("ret"))) {
            throw new AuthorizedException((String) object.get("msg"));
        }
        String avatar = (String) object.get("figureurl_qq_2");
        if (StringKit.isEmpty(avatar)) {
            avatar = (String) object.get("figureurl_qq_1");
        }

        String location = String.format("%s-%s", object.get("province"), object.get("city"));
        return Material.builder().rawJson(JsonKit.toJsonString(object)).username((String) object.get("nickname"))
                .nickname((String) object.get("nickname")).avatar(avatar).location(location).uuid(openId)
                .gender(Gender.of((String) object.get("gender"))).token(accToken).source(complex.toString()).build();
    }

    /**
     * 获取QQ用户的OpenId，支持自定义是否启用查询unionid的功能，如果启用查询unionid的功能， 那就需要开发者先通过邮件申请unionid功能，参考链接
     * {@see <url id="d0a3btkc75r1mimetbp0" type="url" status="parsed" title="UnionID介绍 — QQ互联WIKI" wc=
     * "4771">http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D</url> }
     *
     * @param accToken 通过{@link QqProvider#getAccessToken(Callback)}获取到的{@code accToken}
     * @return openId
     */
    private String getOpenId(AccToken accToken) {
        String response = Httpx.get(Builder.fromUrl("https://graph.qq.com/oauth2.0/me")
                .queryParam("access_token", accToken.getAccessToken()).queryParam("unionid", context.isFlag() ? 1 : 0)
                .build());
        String removePrefix = response.replace("callback(", "");
        String removeSuffix = removePrefix.replace(");", "");
        String openId = removeSuffix.trim();
        Map<String, Object> object = JsonKit.toPojo(openId, Map.class);
        if (object.containsKey("error")) {
            throw new AuthorizedException(
                    (String) object.get("error") + Symbol.COLON + (String) object.get("error_description"));
        }
        accToken.setOpenId((String) object.get("openid"));
        if (object.containsKey("unionid")) {
            accToken.setUnionId((String) object.get("unionid"));
        }
        return StringKit.isEmpty(accToken.getUnionId()) ? accToken.getOpenId() : accToken.getUnionId();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("oauth_consumer_key", context.getAppKey()).queryParam("openid", accToken.getOpenId())
                .build();
    }

    private AccToken getAuthToken(String response) {
        Map<String, String> accessTokenObject = Builder.parseStringToMap(response);
        if (!accessTokenObject.containsKey("access_token") || accessTokenObject.containsKey("code")) {
            throw new AuthorizedException(accessTokenObject.get("msg"));
        }
        return AccToken.builder().accessToken(accessTokenObject.get("access_token"))
                .expireIn(Integer.parseInt(accessTokenObject.getOrDefault("expires_in", "0")))
                .refreshToken(accessTokenObject.get("refresh_token")).build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(QqScope.values())))
                .build();
    }

}