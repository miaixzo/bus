/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpz;
import org.aoju.bus.http.Response;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 京东账号登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdProvider extends AbstractProvider {

    public JdProvider(Context context) {
        super(context, Registry.JD);
    }

    public JdProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.JD, extendCache);
    }

    /**
     * 京东md5加密
     * link: https://github.com/pingjiang/jd-open-api-sdk-src/blob/master/src/main/java/com/jd/open/api/sdk/internal/util/CodecUtil.java
     *
     * @param source 加密内容
     * @return 加密信息
     */
    private static String md5(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance(Algorithm.MD5.getValue());
            byte[] bytes = md.digest(source.getBytes(Charset.UTF_8));
            StringBuilder sign = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xff);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex.toUpperCase());
            }
            return sign.toString();
        } catch (NoSuchAlgorithmException ignored) {
            throw new AuthorizedException(ignored.getMessage());
        }
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject object = null;
        try {
            Response response = Httpz.post().url(source.accessToken())
                    .addParams("app_key", context.getAppKey())
                    .addParams("app_secret", context.getAppSecret())
                    .addParams("grant_type", "authorization_code")
                    .addParams("code", callback.getCode()).build().execute();

            object = JSONObject.parseObject(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .scope(object.getString("scope"))
                .openId(object.getString("open_id"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        JSONObject jsonObject = null;
        try {
            Builder urlBuilder = Builder.fromUrl(source.userInfo())
                    .queryParam("access_token", accToken.getAccessToken())
                    .queryParam("app_key", context.getAppKey())
                    .queryParam("method", "jingdong.user.getUserInfoByOpenId")
                    .queryParam("360buy_param_json", "{\"openId\":\"" + accToken.getOpenId() + "\"}")
                    .queryParam("timestamp", DateKit.format(new Date()))
                    .queryParam("v", "2.0");
            urlBuilder.queryParam("sign", sign(urlBuilder.getReadParams()));
            Response response = Httpz.post().url(urlBuilder.build(true)).build().execute();
            jsonObject = JSONObject.parseObject(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.checkResponse(jsonObject);

        JSONObject object = this.getUserDataJsonObject(jsonObject);

        return Property.builder()
                .rawJson(object)
                .uuid(accToken.getOpenId())
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("imageUrl"))
                .gender(Normal.Gender.of(object.getString("gender")))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        JSONObject object = null;
        try {
            Response response = Httpz.post()
                    .url(source.refresh())
                    .addParams("app_key", context.getAppKey())
                    .addParams("app_secret", context.getAppSecret())
                    .addParams("grant_type", "refresh_token")
                    .addParams("refresh_token", accToken.getRefreshToken()).build().execute();

            object = JSONObject.parseObject(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.checkResponse(object);

        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .expireIn(object.getIntValue("expires_in"))
                        .refreshToken(object.getString("refresh_token"))
                        .scope(object.getString("scope"))
                        .openId(object.getString("open_id"))
                        .build())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("app_key", context.getAppKey())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, getScopes(true, OauthScope.Jd.values())))
                .queryParam("state", getRealState(state))
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("msg")) {
            throw new AuthorizedException(object.getString("msg"));
        }
        if (object.containsKey("error_response")) {
            throw new AuthorizedException(object.getJSONObject("error_response").getString("zh_desc"));
        }
    }

    /**
     * 个人用户无法申请应用
     * 暂时只能参考官网给出的返回结果解析
     * link: http://open.jd.com/home/home#/doc/api?apiCateId=106&apiId=3051&apiName=jingdong.user.getUserInfoByOpenId
     *
     * @param object 请求返回结果
     * @return data JSONObject
     */
    private JSONObject getUserDataJsonObject(JSONObject object) {
        return object.getJSONObject("jingdong_user_getUserInfoByOpenId_response")
                .getJSONObject("getuserinfobyappidandopenid_result")
                .getJSONObject("data");
    }

    /**
     * 宙斯签名规则过程如下:
     * 将所有请求参数按照字母先后顺序排列，例如将access_token,app_key,method,timestamp,v 排序为access_token,app_key,method,timestamp,v
     * 1.把所有参数名和参数值进行拼接，例如：access_tokenxxxapp_keyxxxmethodxxxxxxtimestampxxxxxxvx
     * 2.把appSecret夹在字符串的两端，例如：appSecret+XXXX+appSecret
     * 3.使用MD5进行加密，再转化成大写
     * link: http://open.jd.com/home/home#/doc/common?listId=890
     * link: https://github.com/pingjiang/jd-open-api-sdk-src/blob/master/src/main/java/com/jd/open/api/sdk/DefaultJdClient.java
     *
     * @param params 参数
     * @return 签名内容
     */
    private String sign(Map<String, Object> params) {
        // 放入 TreeMap 排序
        Map<String, Object> treeMap = new TreeMap<>(params);
        String appSecret = context.getAppSecret();
        StringBuilder signBuilder = new StringBuilder(appSecret);
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            String name = entry.getKey();
            String value = (String) entry.getValue();
            if (StringKit.isNotEmpty(name) && StringKit.isNotEmpty(value)) {
                signBuilder.append(name).append(value);
            }
        }
        signBuilder.append(appSecret);
        try {
            return md5(signBuilder.toString());
        } catch (Exception e) {
            throw new AuthorizedException("build sign to jdMd5 error");
        }
    }

}
