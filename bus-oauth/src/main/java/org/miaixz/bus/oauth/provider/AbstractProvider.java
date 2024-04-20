/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.oauth.provider;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.key.ObjectID;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.UriKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.*;
import org.miaixz.bus.oauth.*;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.OauthCache;
import org.miaixz.bus.oauth.metric.OauthScope;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认的request处理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements Provider {

    public Context context;
    public Complex source;
    public ExtendCache extendCache;

    public AbstractProvider(Context context, Complex source) {
        this(context, source, OauthCache.INSTANCE);
    }

    public AbstractProvider(Context context, Complex source, ExtendCache extendCache) {
        this.context = context;
        this.source = source;
        this.extendCache = extendCache;
        if (!isSupport(context, source)) {
            throw new AuthorizedException(Builder.ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        checkContext(context, source);
    }

    /**
     * 是否支持第三方登录
     *
     * @param context 上下文信息
     * @param complex 当前授权平台
     * @return true or false
     */
    public static boolean isSupport(Context context, Complex complex) {
        boolean isSupported = StringKit.isNotEmpty(context.getAppKey())
                && StringKit.isNotEmpty(context.getAppSecret());
        if (isSupported && Registry.ALIPAY == complex) {
            isSupported = StringKit.isNotEmpty(context.getPublicKey());
        }
        if (isSupported && Registry.STACKOVERFLOW == complex) {
            isSupported = StringKit.isNotEmpty(context.getOverflowKey());
        }
        if (isSupported && Registry.WECHAT_EE == complex) {
            isSupported = StringKit.isNotEmpty(context.getAgentId());
        }
        if (isSupported && Registry.CODING == complex) {
            isSupported = StringKit.isNotEmpty(context.getPrefix());
        }
        if (isSupported && Registry.XMLY == complex) {
            isSupported = StringKit.isNotEmpty(context.getDeviceId()) && null != context.getClientOsType();
            if (isSupported) {
                isSupported = context.getClientOsType() == 3 || StringKit.isNotEmpty(context.getPackId());
            }
        }
        return isSupported;
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
     * @param context 上下文信息
     * @param complex 当前授权平台
     */
    public static void checkContext(Context context, Complex complex) {
        String redirectUri = context.getRedirectUri();
        if (context.isIgnoreCheckRedirectUri()) {
            return;
        }
        if (StringKit.isEmpty(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
        if (!Http.isHttp(redirectUri) && !Http.isHttps(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
        // Facebook的回调地址必须为https的链接
        if (Registry.FACEBOOK == complex && !Http.isHttps(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }
        // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1
        if (Registry.ALIPAY == complex && Http.isLocalHost(redirectUri)) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_REDIRECT_URI.getCode());
        }

    }

    /**
     * 校验回调传回的code
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
     * 校验回调传回的{@code state}，为空或者不存在
     * {@code state}不存在的情况只有两种：
     * 1. {@code state}已使用，被正常清除
     * 2. {@code state}为前端伪造，本身就不存在
     *
     * @param state      一定不为空
     * @param complex    当前授权平台
     * @param oauthCache 缓存实现
     */
    public static void checkState(String state, Complex complex, ExtendCache oauthCache) {
        // 推特平台不支持回调 code 和 state
        if (complex == Registry.TWITTER) {
            return;
        }
        if (StringKit.isEmpty(state) || ObjectKit.isEmpty(oauthCache.get(state))) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_STATUS.getCode());
        }
    }

    /**
     * 从 {@link  OauthScope.Scope} 数组中获取实际的 scope 字符串
     *
     * @param defaultScope 默认参数
     * @param scopes       可变参数，支持传任意 {@link  OauthScope.Scope}
     * @return List
     */
    public static List<String> getScopes(boolean defaultScope, OauthScope.Scope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }

        if (defaultScope) {
            return Arrays.stream(scopes)
                    .filter(OauthScope.Scope::isDefault)
                    .map(OauthScope.Scope::getScope)
                    .collect(Collectors.toList());
        }

        return Arrays.stream(scopes).map(OauthScope.Scope::getScope).collect(Collectors.toList());
    }

    /**
     * 统一的登录入口 当通过{@link AbstractProvider#authorize(String)}授权成功后,会跳转到调用方的相关回调方法中
     * 方法的入参可以使用{@code AuthCallback},{@code AuthCallback}类中封装好了OAuth2授权回调所需要的参数
     *
     * @param callback 用于接收回调参数的实体
     * @return the Message
     */
    @Override
    public Message login(Callback callback) {
        try {
            checkCode(source, callback);

            if (!context.isIgnoreCheckState()) {
                checkState(callback.getState(), source, extendCache);
            }

            AccToken token = this.getAccessToken(callback);
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
     * @see AbstractProvider#authorize(String)
     */
    protected abstract AccToken getAccessToken(Callback Callback);

    /**
     * 使用token换取用户信息
     *
     * @param token token信息
     * @return 用户信息
     * @see AbstractProvider#getAccessToken(Callback)
     */
    protected abstract Object getUserInfo(AccToken token);

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
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo()).queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 返回获取revoke authorization的url
     *
     * @param accToken token
     * @return 返回获取revoke authorization的url
     */
    protected String revokeUrl(AccToken accToken) {
        return Builder.fromUrl(source.revoke()).queryParam("access_token", accToken.getAccessToken()).build();
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
    public String doGetAuthorizationCode(String code) {
        return Httpx.get(accessTokenUrl(code));
    }

    /**
     * 通用的 用户信息
     *
     * @param accToken token封装
     * @return HttpResponse
     */
    protected String doGetUserInfo(AccToken accToken) {
        return Httpx.get(userInfoUrl(accToken));
    }

    /**
     * 通用的post形式的取消授权方法
     *
     * @param accToken token封装
     * @return HttpResponse
     */
    protected String doGetRevoke(AccToken accToken) {
        return Httpx.get(revokeUrl(accToken));
    }

    /**
     * 获取以 {@code separator}分割过后的 scope 信息
     *
     * @param separator     多个 {@code scope} 间的分隔符
     * @param encode        是否 encode 编码
     * @param defaultScopes 默认的 scope， 当客户端没有配置 {@code scopes} 时启用
     * @return String
     */
    protected String getScopes(String separator, boolean encode, List<String> defaultScopes) {
        List<String> scopes = context.getScopes();
        if (null == scopes || scopes.isEmpty()) {
            if (null == defaultScopes || defaultScopes.isEmpty()) {
                return Normal.EMPTY;
            }
            scopes = defaultScopes;
        }
        if (null == separator) {
            // 默认为空格
            separator = Symbol.SPACE;
        }
        String scopeStr = String.join(separator, scopes);
        return encode ? UriKit.encode(scopeStr) : scopeStr;
    }

    /**
     * 字符串转map，字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param text   待转换的字符串
     * @param decode 是否解码
     * @return map
     */
    public Map<String, String> parseStringToMap(String text, boolean decode) {
        if (StringKit.isNotEmpty(text)) {
            // 去除 URL 路径信息
            int beginPos = text.indexOf(Symbol.QUESTION_MARK);
            if (beginPos > -1) {
                text = text.substring(beginPos + 1);
            }

            // 去除 # 后面的内容
            int endPos = text.indexOf(Symbol.SHAPE);
            if (endPos > -1) {
                text = text.substring(0, endPos);
            }
        }

        Map<String, String> params = new HashMap<>(Normal._16);
        if (StringKit.isEmpty(text)) {
            return params;
        }

        if (!text.contains(Symbol.AND)) {
            params.put(decode ? UriKit.decode(text) : text, Normal.EMPTY);
            return params;
        }

        final int len = text.length();
        String name = null;
        // 未处理字符开始位置
        int pos = 0;
        // 未处理字符结束位置
        int i;
        // 当前字符
        char c;
        for (i = 0; i < len; i++) {
            c = text.charAt(i);
            // 键值对的分界点
            if (c == Symbol.C_EQUAL) {
                if (null == name) {
                    // name可以是""
                    name = text.substring(pos, i);
                }
                pos = i + 1;
            }
            // 参数对的分界点
            else if (c == Symbol.C_AND) {
                if (null == name && pos != i) {
                    // 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
                    addParam(params, text.substring(pos, i), Normal.EMPTY, decode);
                } else if (null != name) {
                    addParam(params, name, text.substring(pos, i), decode);
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 处理结尾
        if (pos != i) {
            if (null == name) {
                addParam(params, text.substring(pos, i), Normal.EMPTY, decode);
            } else {
                addParam(params, name, text.substring(pos, i), decode);
            }
        } else if (null != name) {
            addParam(params, name, Normal.EMPTY, decode);
        }

        return params;
    }

    private void addParam(Map<String, String> params, String key, String value, boolean decode) {
        key = decode ? UriKit.decode(key) : key;
        value = decode ? UriKit.decode(value) : value;
        if (params.containsKey(key)) {
            params.put(key, params.get(key) + Symbol.COMMA + value);
        } else {
            params.put(key, value);
        }
    }

}
