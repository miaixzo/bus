package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.util.HashMap;
import java.util.Map;

public class SlackProvider extends AbstractProvider {

    public SlackProvider(Context context) {
        super(context, Registry.SLACK);
    }

    public SlackProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.SLACK, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        JSONObject accessTokenObject = JSONObject.parseObject(Httpx.post(accessTokenUrl(callback.getCode()), null, header));
        this.checkResponse(accessTokenObject);
        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .scope(accessTokenObject.getString("scope"))
                .tokenType(accessTokenObject.getString("token_type"))
                .uid(accessTokenObject.getJSONObject("authed_user").getString("id"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(Header.AUTHORIZATION, "Bearer ".concat(authToken.getAccessToken()));

        JSONObject object = JSONObject.parseObject(Httpx.post(userInfoUrl(authToken), null, header));
        this.checkResponse(object);
        JSONObject user = object.getJSONObject("user");
        JSONObject profile = user.getJSONObject("profile");
        return Property.builder()
                .rawJson(user)
                .uuid(user.getString("id"))
                .username(user.getString("name"))
                .nickname(user.getString("real_name"))
                .avatar(profile.getString("image_original"))
                .email(profile.getString("email"))
                .gender(Normal.Gender.UNKNOWN)
                .token(authToken)
                .source(source.toString())
                .build();
    }

    @Override
    public Message revoke(AccToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(Header.AUTHORIZATION, "Bearer ".concat(authToken.getAccessToken()));

        JSONObject object = JSONObject.parseObject(Httpx.post(source.revoke(), null, header));
        this.checkResponse(object);
        // 返回1表示取消授权成功，否则失败
        Builder.ErrorCode status = object.getBooleanValue("revoked") ? Builder.ErrorCode.SUCCESS : Builder.ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getMsg()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (!object.getBooleanValue("ok")) {
            String errorMsg = object.getString("error");
            if (object.containsKey("response_metadata")) {
                JSONArray array = object.getJSONObject("response_metadata").getJSONArray("messages");
                if (null != array && array.size() > 0) {
                    errorMsg += "; " + String.join(",", (CharSequence[]) array.toArray(new String[0]));
                }
            }

            throw new AuthorizedException(errorMsg);
        }
    }

    @Override
    public String userInfoUrl(AccToken authToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("user", authToken.getUid())
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
        return Builder.fromUrl(source.authorize())
                .queryParam("client_id", context.getAppKey())
                .queryParam("state", getRealState(state))
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, getScopes(true, OauthScope.Slack.values())))
                .build();
    }

    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

}