/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.oauth.metric.jd;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 京东 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdProvider extends AbstractProvider {

    public JdProvider(Context context) {
        super(context, Registry.JD);
    }

    public JdProvider(Context context, ExtendCache cache) {
        super(context, Registry.JD, cache);
    }

    /**
     * 京东宙斯平台的签名字符串 宙斯签名规则过程如下: 将所有请求参数按照字母先后顺序排列，例如将access_token,app_key,method,timestamp,v
     * 排序为access_token,app_key,method,timestamp,v
     * 1.把所有参数名和参数值进行拼接，例如：access_tokenxxxapp_keyxxxmethodxxxxxxtimestampxxxxxxvx
     * 2.把appSecret夹在字符串的两端，例如：appSecret+XXXX+appSecret 3.使用MD5进行加密，再转化成大写 link:
     * http://open.jd.com/home/home#/doc/common?listId=890 link:
     * https://github.com/pingjiang/jd-open-api-sdk-src/blob/master/src/main/java/com/jd/open/api/sdk/DefaultJdClient.java
     *
     * @param appSecret 京东应用密钥
     * @param params    签名参数
     * @return 签名后的字符串
     */
    public static String sign(String appSecret, Map<String, Object> params) {
        Map<String, Object> treeMap = new TreeMap<>(params);
        StringBuilder signBuilder = new StringBuilder(appSecret);
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            String name = entry.getKey();
            String value = String.valueOf(entry.getValue());
            if (StringKit.isNotEmpty(name) && StringKit.isNotEmpty(value)) {
                signBuilder.append(name).append(value);
            }
        }
        signBuilder.append(appSecret);
        return org.miaixz.bus.crypto.Builder.md5Hex(signBuilder.toString()).toUpperCase();
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        String response = Httpx.post(complex.accessToken(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder().accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in")).refreshToken(object.getString("refresh_token"))
                .scope(object.getString("scope")).openId(object.getString("open_id")).build();
    }

    /**
     * 个人用户无法申请应用 暂时只能参考官网给出的返回结果解析
     *
     * @param object 请求返回结果
     * @return data JSONObject
     */
    private JSONObject getUserDataJsonObject(JSONObject object) {
        return object.getJSONObject("jingdong_user_getUserInfoByOpenId_response")
                .getJSONObject("getuserinfobyappidandopenid_result").getJSONObject("data");
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", oldToken.getRefreshToken());
        String response = Httpx.post(complex.refresh(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder().accessToken(object.getString("access_token"))
                        .expireIn(object.getIntValue("expires_in")).refreshToken(object.getString("refresh_token"))
                        .scope(object.getString("scope")).openId(object.getString("open_id")).build())
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_response")) {
            throw new AuthorizedException(object.getJSONObject("error_response").getString("zh_desc"));
        }
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        Builder urlBuilder = Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken())
                .queryParam("app_key", context.getAppKey()).queryParam("method", "jingdong.user.getUserInfoByOpenId")
                .queryParam("360buy_param_json", "{\"openId\":\"" + accToken.getOpenId() + "\"}")
                .queryParam("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("v", "2.0");
        urlBuilder.queryParam("sign", sign(context.getAppSecret(), urlBuilder.getReadOnlyParams()));
        String response = Httpx.post(urlBuilder.build(true));
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        JSONObject data = this.getUserDataJsonObject(object);

        return Material.builder().rawJson(data).uuid(accToken.getOpenId()).username(data.getString("nickName"))
                .nickname(data.getString("nickName")).avatar(data.getString("imageUrl"))
                .gender(Gender.of(data.getString("gendar"))).token(accToken).source(complex.toString()).build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("app_key", context.getAppKey())
                .queryParam("response_type", "code").queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(JdScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

}
