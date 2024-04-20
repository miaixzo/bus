package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;

/**
 * @author Justubborn
 */
public class WeChatMaProvider extends AbstractProvider {

    public WeChatMaProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.WECHAT_MA, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        String url = Builder.fromUrl("https://api.weixin.qq.com/cgi-bin/token")
                .queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret())
                .queryParam("grant_type", "client_credential")
                .build();
        String response = Httpx.get(url);
        JSONObject object = this.checkResponse(response);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .build();
    }

    @Override
    protected Object getUserInfo(AccToken token) {
        return null;
    }

    @Override
    public Message login(Callback callback) {
        String response = doGetAuthorizationCode(callback.getCode());
        JSONObject object = this.checkResponse(response);
        return Message.builder().errcode(Builder.ErrorCode.SUCCESS.getCode()).data(object).build();
    }

    /**
     * 刷新access token (续期)
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken token) {
        AccToken accToken = getAccessToken(null);
        return Message.builder().errcode(Builder.ErrorCode.SUCCESS.getCode()).data(accToken).build();
    }


    /**
     * 返回获取accessToken的url
     *
     * @param code oauth的授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(source.authorize())
                .queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("js_code", code)
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param response 请求响应内容
     * @return 返回
     */
    public JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);
        if (object.containsKey("errcode")) {
            throw new AuthorizedException(StringKit.toString(object.getIntValue("errcode")), object.getString("errmsg"));
        }
        return object;
    }
}
