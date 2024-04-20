package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.OauthScope;

import java.util.HashMap;
import java.util.Map;

public class LineProvider extends AbstractProvider {

    public LineProvider(Context config) {
        super(config, Registry.LINE);
    }

    public LineProvider(Context config, ExtendCache extendCache) {
        super(config, Registry.LINE, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        JSONObject object = null;
        try {
            Response response = Httpz.post().url(source.accessToken())
                    .addParams("grant_type", "authorization_code")
                    .addParams("code", callback.getCode())
                    .addParams("redirect_uri", context.getRedirectUri())
                    .addParams("client_id", context.getAppKey())
                    .addParams("client_secret", context.getAppSecret()).build().execute();
            object = JSONObject.parseObject(response.body().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .expireIn(object.getIntValue("expires_in"))
                .idToken(object.getString("id_token"))
                .scope(object.getString("scope"))
                .tokenType(object.getString("token_type"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(Header.AUTHORIZATION, "Bearer ".concat(authToken.getAccessToken()));
        JSONObject object = JSONObject.parseObject(Httpx.post(source.revoke(), null, header));

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("userId"))
                .username(object.getString("displayName"))
                .nickname(object.getString("displayName"))
                .avatar(object.getString("pictureUrl"))
                .remark(object.getString("statusMessage"))
                .gender(Normal.Gender.UNKNOWN)
                .token(authToken)
                .source(source.toString())
                .build();
    }

    @Override
    public Message revoke(AccToken authToken) {
        Map<String, Object> params = new HashMap<>(5);
        params.put("access_token", authToken.getAccessToken());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        JSONObject object = JSONObject.parseObject(Httpx.post(source.revoke(), params));

        // 返回1表示取消授权成功，否则失败
        Builder.ErrorCode status = object.getBooleanValue("revoked") ? Builder.ErrorCode.SUCCESS : Builder.ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getMsg()).build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(this.getToken(refreshTokenUrl(accToken.getRefreshToken())))
                .build();
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("user", accToken.getUid())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("nonce", state)
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, getScopes(true, OauthScope.Line.values())))
                .build();
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        JSONObject object = JSONObject.parseObject(Httpx.post(accessTokenUrl));
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .expireIn(object.getIntValue("expires_in"))
                .idToken(object.getString("id_token"))
                .scope(object.getString("scope"))
                .tokenType(object.getString("token_type"))
                .build();
    }

}