/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.core.toolkit.UriKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.*;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthCache;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 默认的request处理类
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
public abstract class DefaultProvider implements Provider {

    protected Context context;
    protected Complex source;
    protected ExtendCache extendCache;

    public DefaultProvider(Context context, Complex source) {
        this(context, source, OauthCache.INSTANCE);
    }

    public DefaultProvider(Context context, Complex source, ExtendCache extendCache) {
        this.context = context;
        this.source = source;
        this.extendCache = extendCache;
        if (!isSupportedAuth(context, source)) {
            throw new AuthorizedException(Builder.ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        checkContext(context, source);
    }

    /**
     * 是否支持第三方登录
     *
     * @param context context
     * @param source  source
     * @return true or false
     */
    public static boolean isSupportedAuth(Context context, Complex source) {
        boolean isSupported = StringKit.isNotEmpty(context.getAppKey()) && StringKit.isNotEmpty(context.getAppSecret());
        if (isSupported && Registry.ALIPAY == source) {
            isSupported = StringKit.isNotEmpty(context.getPublicKey());
        }
        if (isSupported && Registry.STACKOVERFLOW == source) {
            isSupported = StringKit.isNotEmpty(context.getOverflowKey());
        }
        if (isSupported && Registry.WECHAT_EE == source) {
            isSupported = StringKit.isNotEmpty(context.getAgentId());
        }
        return isSupported;
    }

    /**
     * 是否为http协议
     *
     * @param url 待验证的url
     * @return true: http协议, false: 非http协议
     */
    public static boolean isHttpProtocol(String url) {
        if (StringKit.isEmpty(url)) {
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
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(Http.HTTPS_PREFIX);
    }

    /**
     * 是否为本地主机(域名)
     *
     * @param url 待验证的url
     * @return true: 本地主机(域名), false: 非本地主机(域名)
     */
    public static boolean isLocalHost(String url) {
        return StringKit.isEmpty(url) || url.contains(Http.HTTP_HOST_IPV4) || url.contains(Http.HTTP_HOST_LOCAL);
    }

    /**
     * 编码
     *
     * @param value str
     * @return encode str
     */
    public static String urlEncode(String value) {
        if (value == null) {
            return Normal.EMPTY;
        }
        try {
            String encoded = URLEncoder.encode(value, Charset.UTF_8.displayName());
            return encoded.replace(Symbol.PLUS, "%20").replace(Symbol.STAR, "%2A").replace(Symbol.TILDE, "%7E").replace(Symbol.SLASH, "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new AuthorizedException("Failed To Encode Uri", e);
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
            return Normal.EMPTY;
        }
        try {
            return URLDecoder.decode(value, Charset.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            throw new AuthorizedException("Failed To Decode Uri", e);
        }
    }

    /**
     * 签名
     *
     * @param key       key
     * @param data      data
     * @param algorithm algorithm
     * @return byte[]
     */
    public static byte[] sign(byte[] key, byte[] data, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new AuthorizedException("Unsupported algorithm: " + algorithm, ex);
        } catch (InvalidKeyException ex) {
            throw new AuthorizedException("Invalid key: " + Arrays.toString(key), ex);
        }
    }

    /**
     * 检查配置合法性 针对部分平台, 对redirect uri有特定要求 一般来说redirect uri都是http://,而对于facebook平台, redirect uri 必须是https的链接
     *
     * @param context context
     * @param source  source
     */
    public static void checkContext(Context context, Complex source) {
        String redirectUri = context.getRedirectUri();
        if (!isHttpProtocol(redirectUri) && !isHttpsProtocol(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
        // facebook的回调地址必须为https的链接
        if (Registry.FACEBOOK == source && !isHttpsProtocol(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
        // 支付宝在创建回调地址时,不允许使用localhost或者127.0.0.1
        if (Registry.ALIPAY == source && isLocalHost(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
    }

    /**
     * 校验回调传回的code
     * <p>
     * {@code v1.10.0}版本中改为传入{@code source}和{@code callback},对于不同平台使用不同参数接受code的情况统一做处理
     *
     * @param complex  当前授权平台
     * @param callback 从第三方授权回调回来时传入的参数集合
     */
    public static void checkCode(Complex complex, Callback callback) {
        // 推特平台不支持回调 code 和 state
        if (complex == Registry.TWITTER) {
            return;
        }
        String code = callback.getCode();
        if (complex == Registry.ALIPAY) {
            code = callback.getAuth_code();
        } else if (complex == Registry.HUAWEI) {
            code = callback.getAuthorization_code();
        }
        if (StringKit.isEmpty(code)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_CODE.getCode());
        }
    }

    /**
     * 如果给定字符串{@code str}中不包含{@code appendStr},则在{@code str}后追加{@code appendStr}；
     * 如果已包含{@code appendStr},则在{@code str}后追加{@code otherwise}
     *
     * @param str       给定的字符串
     * @param appendStr 需要追加的内容
     * @param otherwise 当{@code appendStr}不满足时追加到{@code str}后的内容
     * @return 追加后的字符串
     */
    public static String appendIfNotContain(String str, String appendStr, String otherwise) {
        if (StringKit.isEmpty(str) || StringKit.isEmpty(appendStr)) {
            return str;
        }
        if (str.contains(appendStr)) {
            return str.concat(otherwise);
        }
        return str.concat(appendStr);
    }

    /**
     * string字符串转map,str格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param accessTokenStr 待转换的字符串
     * @return map
     */
    public static Map<String, String> parseStringToMap(String accessTokenStr) {
        Map<String, String> res = new HashMap<>();
        if (accessTokenStr.contains(Symbol.AND)) {
            String[] fields = accessTokenStr.split(Symbol.AND);
            for (String field : fields) {
                if (field.contains(Symbol.EQUAL)) {
                    String[] keyValue = field.split(Symbol.EQUAL);
                    res.put(urlDecode(keyValue[0]), keyValue.length == 2 ? urlDecode(keyValue[1]) : null);
                }
            }
        }
        return res;
    }

    /**
     * map转字符串,转换后的字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param params 待转换的map
     * @param encode 是否转码
     * @return str
     */
    public static String parseMapToString(Map<String, Object> params, boolean encode) {
        List<String> paramList = new ArrayList<>();
        params.forEach((k, v) -> {
            if (ObjectKit.isNull(v)) {
                paramList.add(k + Symbol.EQUAL);
            } else {
                String valueString = v.toString();
                paramList.add(k + Symbol.EQUAL + (encode ? urlEncode(valueString) : valueString));
            }
        });
        return CollKit.join(paramList, Symbol.AND);
    }

    /**
     * 将url的参数列表转换成map
     *
     * @param url 待转换的url
     * @return map
     */
    public static Map<String, Object> parseQueryToMap(String url) {
        Map<String, Object> paramMap = new HashMap<>();
        UriKit.decodeVal(url, Charset.DEFAULT_UTF_8).forEach(paramMap::put);
        return paramMap;
    }

    /**
     * 统一的登录入口 当通过{@link DefaultProvider#authorize(String)}授权成功后,会跳转到调用方的相关回调方法中
     * 方法的入参可以使用{@code AuthCallback},{@code AuthCallback}类中封装好了OAuth2授权回调所需要的参数
     *
     * @param Callback 用于接收回调参数的实体
     * @return the Message
     */
    @Override
    public Message login(Callback Callback) {
        try {
            this.checkCode(source, Callback);
            this.checkState(Callback.getState());

            AccToken token = this.getAccessToken(Callback);
            Property user = (Property) this.getUserInfo(token);
            return Message.builder().errcode(Builder.ErrorCode.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            String errorCode = Normal.EMPTY + Builder.ErrorCode.FAILURE.getCode();
            if (e instanceof AuthorizedException) {
                errorCode = ((AuthorizedException) e).getErrcode();
            }
            return Message.builder().errcode(errorCode).errmsg(e.getMessage()).build();
        }
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
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
     * 获取用户的实际性别 华为系统中,用户的性别：1表示女,0表示男
     *
     * @param object obj
     * @return AuthUserGender
     */
    protected Normal.Gender getRealGender(JSONObject object) {
        int genderCodeInt = object.getIntValue("gender");
        String genderCode = genderCodeInt == 1 ? Symbol.ZERO : (genderCodeInt == 0) ? Symbol.ONE : genderCodeInt + Normal.EMPTY;
        return Normal.Gender.getGender(genderCode);
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
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
        return Builder.fromUrl(source.refresh())
                .queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
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
        return Builder.fromUrl(source.userInfo()).queryParam("access_token", token.getAccessToken()).build();
    }

    /**
     * 返回获取revoke authorization的url
     *
     * @param token token
     * @return 返回获取revoke authorization的url
     */
    protected String revokeUrl(AccToken token) {
        return Builder.fromUrl(source.revoke()).queryParam("access_token", token.getAccessToken()).build();
    }

    /**
     * 获取state,如果为空, 则默认取当前日期的时间戳
     *
     * @param state 原始的state
     * @return 返回不为null的state
     */
    protected String getRealState(String state) {
        if (StringKit.isEmpty(state)) {
            state = ObjectID.id();
        }
        // 缓存state
        extendCache.cache(state, state);
        return state;
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return HttpResponse
     */
    protected String doPostAuthorizationCode(String code) {
        return Httpx.post(accessTokenUrl(code));
    }

    /**
     * 通用的 authorizationCode 协议
     *
     * @param code code码
     * @return HttpResponse
     */
    protected String doGetAuthorizationCode(String code) {
        return Httpx.get(accessTokenUrl(code));
    }

    /**
     * 通用的 用户信息
     *
     * @param token token封装
     * @return HttpResponse
     */
    protected String doGetUserInfo(AccToken token) {
        return Httpx.get(userInfoUrl(token));
    }

    /**
     * 通用的post形式的取消授权方法
     *
     * @param token token封装
     * @return HttpResponse
     */
    protected String doGetRevoke(AccToken token) {
        return Httpx.get(revokeUrl(token));
    }

    /**
     * 校验回调传回的state
     *
     * @param state {@code state}一定不为空
     */
    protected void checkState(String state) {
        // 推特平台不支持回调 code 和 state
        if (source == Registry.TWITTER) {
            return;
        }
        if (StringKit.isEmpty(state) || ObjectKit.isEmpty(extendCache.get(state))) {
            throw new AuthorizedException(Normal.EMPTY + Builder.ErrorCode.ILLEGAL_REQUEST);
        }
    }

    /**
     * 字符串转map，字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param str    待转换的字符串
     * @param decode 是否解码
     * @return map
     */
    public Map<String, String> parseStringToMap(String str, boolean decode) {
        if (StringKit.isNotEmpty(str)) {
            // 去除 URL 路径信息
            int beginPos = str.indexOf(Symbol.QUESTION_MARK);
            if (beginPos > -1) {
                str = str.substring(beginPos + 1);
            }

            // 去除 # 后面的内容
            int endPos = str.indexOf(Symbol.SHAPE);
            if (endPos > -1) {
                str = str.substring(0, endPos);
            }
        }

        Map<String, String> params = new HashMap<>(16);
        if (StringKit.isEmpty(str)) {
            return params;
        }

        if (!str.contains(Symbol.AND)) {
            params.put(decode ? urlDecode(str) : str, Normal.EMPTY);
            return params;
        }

        final int len = str.length();
        String name = null;
        // 未处理字符开始位置
        int pos = 0;
        // 未处理字符结束位置
        int i;
        // 当前字符
        char c;
        for (i = 0; i < len; i++) {
            c = str.charAt(i);
            // 键值对的分界点
            if (c == Symbol.C_EQUAL) {
                if (null == name) {
                    // name可以是""
                    name = str.substring(pos, i);
                }
                pos = i + 1;
            }
            // 参数对的分界点
            else if (c == Symbol.C_AND) {
                if (null == name && pos != i) {
                    // 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
                    addParam(params, str.substring(pos, i), Normal.EMPTY, decode);
                } else if (name != null) {
                    addParam(params, name, str.substring(pos, i), decode);
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 处理结尾
        if (pos != i) {
            if (name == null) {
                addParam(params, str.substring(pos, i), Normal.EMPTY, decode);
            } else {
                addParam(params, name, str.substring(pos, i), decode);
            }
        } else if (name != null) {
            addParam(params, name, Normal.EMPTY, decode);
        }

        return params;
    }

    private void addParam(Map<String, String> params, String key, String value, boolean decode) {
        key = decode ? urlDecode(key) : key;
        value = decode ? urlDecode(value) : value;
        if (params.containsKey(key)) {
            params.put(key, params.get(key) + Symbol.COMMA + value);
        } else {
            params.put(key, value);
        }
    }

}
