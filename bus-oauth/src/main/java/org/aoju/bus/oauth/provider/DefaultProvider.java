/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.UriUtils;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.*;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.DefaultStateCache;
import org.aoju.bus.oauth.metric.StateCache;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 默认的request处理类
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public abstract class DefaultProvider implements Provider {

    private static final String ALGORITHM = "HmacSHA256";
    protected Context context;
    protected Complex source;
    protected StateCache stateCache;

    public DefaultProvider(Context context, Complex source) {
        this(context, source, DefaultStateCache.INSTANCE);
    }

    public DefaultProvider(Context context, Complex source, StateCache stateCache) {
        this.context = context;
        this.source = source;
        this.stateCache = stateCache;
        if (!isSupportedAuth(context, source)) {
            throw new InstrumentException(Builder.Status.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        checkcontext(context, source);
    }

    /**
     * 生成钉钉请求的Signature
     *
     * @param secretKey 平台应用的授权密钥
     * @param timestamp 时间戳
     * @return Signature
     */
    public static String generateDingTalkSignature(String secretKey, String timestamp) {
        byte[] signData = sign(secretKey.getBytes(org.aoju.bus.core.consts.Charset.UTF_8), timestamp.getBytes(org.aoju.bus.core.consts.Charset.UTF_8));
        return urlEncode(new String(Base64.encode(signData, false)));
    }

    /**
     * 签名
     *
     * @param key  key
     * @param data data
     * @return byte[]
     */
    private static byte[] sign(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new InstrumentException("Unsupported algorithm: " + ALGORITHM, ex);
        } catch (InvalidKeyException ex) {
            throw new InstrumentException("Invalid key: " + Arrays.toString(key), ex);
        }
    }

    /**
     * 编码
     *
     * @param value str
     * @return encode str
     */
    public static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, org.aoju.bus.core.consts.Charset.UTF_8.displayName());
            return encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException("Failed To Encode Uri", e);
        }
    }

    /**
     * 解码
     *
     * @param value str
     * @return decode str
     */
    public static String urlDecode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLDecoder.decode(value, org.aoju.bus.core.consts.Charset.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException("Failed To Decode Uri", e);
        }
    }

    /**
     * string字符串转map，str格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param accessTokenStr 待转换的字符串
     * @return map
     */
    public static Map<String, String> parseStringToMap(String accessTokenStr) {
        Map<String, String> res = new HashMap<>();
        if (accessTokenStr.contains("&")) {
            String[] fields = accessTokenStr.split("&");
            for (String field : fields) {
                if (field.contains("=")) {
                    String[] keyValue = field.split("=");
                    res.put(urlDecode(keyValue[0]), keyValue.length == 2 ? urlDecode(keyValue[1]) : null);
                }
            }
        }
        return res;
    }

    /**
     * 将url的参数列表转换成map
     *
     * @param url 待转换的url
     * @return map
     */
    public static Map<String, Object> parseQueryToMap(String url) {
        Map<String, Object> paramMap = new HashMap<>();
        UriUtils.decodeVal(url, Charset.DEFAULT_UTF_8).forEach(paramMap::put);
        return paramMap;
    }

    /**
     * 是否为http协议
     *
     * @param url 待验证的url
     * @return true: http协议, false: 非http协议
     */
    public static boolean isHttpProtocol(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("http://");
    }

    /**
     * 是否为https协议
     *
     * @param url 待验证的url
     * @return true: https协议, false: 非https协议
     */
    public static boolean isHttpsProtocol(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("https://");
    }

    /**
     * 是否为本地主机（域名）
     *
     * @param url 待验证的url
     * @return true: 本地主机（域名）, false: 非本地主机（域名）
     */
    public static boolean isLocalHost(String url) {
        return StringUtils.isEmpty(url) || url.contains("127.0.0.1") || url.contains("localhost");
    }

    /**
     * 生成饿了么请求的Signature
     * <p>
     * 代码copy并修改自：https://coding.net/u/napos_openapi/p/eleme-openapi-java-sdk/git/blob/master/src/main/java/eleme/openapi/sdk/utils/SignatureUtil.java
     *
     * @param appKey     平台应用的授权key
     * @param secret     平台应用的授权密钥
     * @param timestamp  时间戳，单位秒。API服务端允许客户端请求最大时间误差为正负5分钟。
     * @param action     饿了么请求的api方法
     * @param token      用户授权的token
     * @param parameters 加密参数
     * @return Signature
     */
    public static String generateElemeSignature(String appKey, String secret, long timestamp, String action, String token, Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append("=").append(JSON.toJSONString(entry.getValue()));
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        String calculatedSignature = md5(splice);
        return calculatedSignature.toUpperCase();
    }

    /**
     * MD5加密饿了么请求的Signature
     * <p>
     * 代码copy并修改自：https://coding.net/u/napos_openapi/p/eleme-openapi-java-sdk/git/blob/master/src/main/java/eleme/openapi/sdk/utils/SignatureUtil.java
     *
     * @param str 饿了么请求的Signature
     * @return md5 str
     */
    private static String md5(String str) {
        MessageDigest md;
        StringBuilder buffer = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] byteData = md.digest();
            buffer = new StringBuilder();
            for (byte byteDatum : byteData) {
                buffer.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception ignored) {
        }

        return null == buffer ? "" : buffer.toString();
    }

    /**
     * 是否支持第三方登录
     *
     * @param context context
     * @param source source
     * @return true or false
     * @since 1.6.2
     */
    public static boolean isSupportedAuth(Context context, Complex source) {
        boolean isSupported = StringUtils.isNotEmpty(context.getClientId()) && StringUtils.isNotEmpty(context.getClientSecret()) && StringUtils.isNotEmpty(context.getRedirectUri());
        if (isSupported && Registry.ALIPAY == source) {
            isSupported = StringUtils.isNotEmpty(context.getAlipayPublicKey());
        }
        if (isSupported && Registry.STACK == source) {
            isSupported = StringUtils.isNotEmpty(context.getStackOverflowKey());
        }
        if (isSupported && Registry.WECHAT_EE == source) {
            isSupported = StringUtils.isNotEmpty(context.getAgentId());
        }
        return isSupported;
    }

    /**
     * 检查配置合法性。针对部分平台， 对redirect uri有特定要求。一般来说redirect uri都是http://，而对于facebook平台， redirect uri 必须是https的链接
     *
     * @param context context
     * @param source source
     * @since 1.6.2
     */
    public static void checkcontext(Context context, Complex source) {
        String redirectUri = context.getRedirectUri();
        if (!isHttpProtocol(redirectUri) && !isHttpsProtocol(redirectUri)) {
            throw new InstrumentException(Builder.Status.ILLEGAL_REDIRECT_URI.getCode());
        }
        // facebook的回调地址必须为https的链接
        if (Registry.FACEBOOK == source && !isHttpsProtocol(redirectUri)) {
            throw new InstrumentException(Builder.Status.ILLEGAL_REDIRECT_URI.getCode());
        }
        // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1
        if (Registry.ALIPAY == source && isLocalHost(redirectUri)) {
            throw new InstrumentException(Builder.Status.ILLEGAL_REDIRECT_URI.getCode());
        }
    }

    /**
     * 校验回调传回的code
     * <p>
     * {@code v1.10.0}版本中改为传入{@code source}和{@code callback}，对于不同平台使用不同参数接受code的情况统一做处理
     *
     * @param complex   当前授权平台
     * @param callback 从第三方授权回调回来时传入的参数集合
     * @since 1.8.0
     */
    public static void checkCode(Complex complex, Callback callback) {
        String code = callback.getCode();
        if (complex == Registry.ALIPAY) {
            code = callback.getAuth_code();
        } else if (complex == Registry.HUAWEI) {
            code = callback.getAuthorization_code();
        }
        if (StringUtils.isEmpty(code)) {
            throw new InstrumentException(Builder.Status.ILLEGAL_CODE.getCode());
        }
    }

    /**
     * 获取access token
     *
     * @param Callback 授权成功后的回调参数
     * @return token
     * @see DefaultProvider#authorize(String)
     */
    protected abstract AccToken getAccessToken(Callback Callback);

    /**
     * 使用token换取用户信息
     *
     * @param token token信息
     * @return 用户信息
     * @see DefaultProvider#getAccessToken(Callback)
     */
    protected abstract Object getUserInfo(AccToken token);

    /**
     * 统一的登录入口。当通过{@link DefaultProvider#authorize(String)}授权成功后，会跳转到调用方的相关回调方法中
     * 方法的入参可以使用{@code AuthCallback}，{@code AuthCallback}类中封装好了OAuth2授权回调所需要的参数
     *
     * @param Callback 用于接收回调参数的实体
     * @return AuthResponse
     */
    @Override
    public Message login(Callback Callback) {
        try {
            this.checkCode(source, Callback);
            this.checkState(Callback.getState());

            AccToken token = this.getAccessToken(Callback);
            Property user = (Property) this.getUserInfo(token);
            return Message.builder().errcode(Builder.Status.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            return this.responseError(e);
        }
    }

    /**
     * 处理{@link DefaultProvider#login(Callback)} 发生异常的情况，统一响应参数
     *
     * @param e 具体的异常
     * @return AuthResponse
     */
    private Message responseError(Exception e) {
        String errorCode = "" + Builder.Status.FAILURE.getCode();
        if (e instanceof InstrumentException) {
            errorCode = ((InstrumentException) e).getErrcode();
        }
        return Message.builder().errcode(errorCode).errmsg(e.getMessage()).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getClientId())
                .queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken refreshToken
     * @return 返回获取accessToken的url
     */
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromBaseUrl(source.refresh())
                .queryParam("client_id", context.getClientId())
                .queryParam("client_secret", context.getClientSecret())
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token token
     * @return 返回获取userInfo的url
     */
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo()).queryParam("access_token", token.getAccessToken()).build();
    }

    /**
     * 返回获取revoke authorization的url
     *
     * @param token token
     * @return 返回获取revoke authorization的url
     */
    protected String revokeUrl(AccToken token) {
        return Builder.fromBaseUrl(source.revoke()).queryParam("access_token", token.getAccessToken()).build();
    }

    /**
     * 获取state，如果为空， 则默认取当前日期的时间戳
     *
     * @param state 原始的state
     * @return 返回不为null的state
     */
    protected String getRealState(String state) {
        if (StringUtils.isEmpty(state)) {
            state = ObjectID.id();
        }
        // 缓存state
        stateCache.cache(state, state);
        return state;
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return HttpResponse
     */
    protected String doPostAuthorizationCode(String code) {
        return HttpClient.post(accessTokenUrl(code));
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return HttpResponse
     */
    protected String doGetAuthorizationCode(String code) {
        return HttpClient.get(accessTokenUrl(code));
    }

    /**
     * 通用的 用户信息
     *
     * @param token token封装
     * @return HttpResponse
     */
    protected String doGetUserInfo(AccToken token) {
        return HttpClient.get(userInfoUrl(token));
    }

    /**
     * 通用的post形式的取消授权方法
     *
     * @param token token封装
     * @return HttpResponse
     */
    protected String doGetRevoke(AccToken token) {
        return HttpClient.get(revokeUrl(token));
    }

    /**
     * 校验回调传回的state
     *
     * @param state {@code state}一定不为空
     */
    protected void checkState(String state) {
        if (StringUtils.isEmpty(state) || !stateCache.containsKey(state)) {
            throw new InstrumentException("" + Builder.Status.ILLEGAL_REQUEST);
        }
    }

}
