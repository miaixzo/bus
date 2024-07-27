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
package org.miaixz.bus.oauth.metric.okta;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Okta 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OktaProvider extends AbstractProvider {

    public OktaProvider(Context context) {
        super(context, Registry.OKTA);
    }

    public OktaProvider(Context context, ExtendCache cache) {
        super(context, Registry.OKTA, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        String tokenUrl = accessTokenUrl(callback.getCode());
        return getAuthToken(tokenUrl);
    }

    private AccToken getAuthToken(String tokenUrl) {
        Map header = new HashMap();
        header.put("accept", MediaType.APPLICATION_JSON);
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        header.put("Authorization",
                "Basic " + Base64.encode(context.getAppKey().concat(Symbol.COLON).concat(context.getAppSecret())));
        String response = Httpx.post(tokenUrl, null, header);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .tokenType(accessTokenObject.getString("token_type"))
                .expireIn(accessTokenObject.getIntValue("expires_in")).scope(accessTokenObject.getString("scope"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .idToken(accessTokenObject.getString("id_token")).build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        if (null == accToken.getRefreshToken()) {
            return Message.builder().errcode(ErrorCode.ILLEGAL_TOKEN.getCode())
                    .errmsg(ErrorCode.ILLEGAL_TOKEN.getDesc()).build();
        }
        String refreshUrl = refreshTokenUrl(accToken.getRefreshToken());
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(this.getAuthToken(refreshUrl)).build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + accToken.getAccessToken());
        String response = Httpx.post(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        JSONObject address = object.getJSONObject("address");
        return Material.builder().rawJson(object).uuid(object.getString("sub")).username(object.getString("name"))
                .nickname(object.getString("nickname")).email(object.getString("email"))
                .location(null == address ? null : address.getString("street_address"))
                .gender(Gender.of(object.getString("sex"))).token(accToken).source(complex.toString()).build();
    }

    @Override
    public Message revoke(AccToken accToken) {
        Map<String, String> params = new HashMap<>(4);
        params.put("token", accToken.getAccessToken());
        params.put("token_type_hint", "access_token");

        Map<String, String> header = new HashMap<>();
        header.put("Authorization",
                "Basic " + Base64.encode(context.getAppKey().concat(Symbol.COLON).concat(context.getAppSecret())));
        Httpx.post(revokeUrl(accToken), params, header);
        ErrorCode status = ErrorCode.SUCCESS;
        return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

    @Override
    public String authorize(String state) {
        return Builder
                .fromUrl(String.format(complex.authorize(), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("response_type", "code").queryParam("prompt", "consent")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(OktaScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

    @Override
    public String accessTokenUrl(String code) {
        return Builder
                .fromUrl(String.format(complex.accessToken(), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("code", code).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder
                .fromUrl(String.format(complex.refresh(), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token").build();
    }

    @Override
    protected String revokeUrl(AccToken accToken) {
        return String.format(complex.revoke(), context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return String.format(complex.userInfo(), context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

}
