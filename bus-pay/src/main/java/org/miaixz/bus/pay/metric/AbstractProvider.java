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
package org.miaixz.bus.pay.metric;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PaymentException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.pay.Checker;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.magic.Message;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 相关请求提供者
 *
 * @param <T> 全局对象
 * @param <K> 公用属性
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider<T extends Material, K extends Context> implements Provider<T> {

    /**
     * 上下文对象
     */
    protected K context;
    /**
     * API地址支持
     */
    protected Complex complex;
    /**
     * 缓存支持
     */
    protected ExtendCache cache;

    /**
     * 构造
     *
     * @param context 全局信息
     */
    public AbstractProvider(K context) {
        this(context, null);
    }

    /**
     * 构造
     *
     * @param context 全局信息
     * @param complex API地址
     */
    public AbstractProvider(K context, Complex complex) {
        this(context, complex, PayCache.INSTANCE);
    }

    /**
     * 构造
     *
     * @param context 全局信息
     * @param complex API地址
     * @param cache   缓存支持
     */
    public AbstractProvider(K context, Complex complex, ExtendCache cache) {
        Assert.notNull(context, "[context] is null");
        this.context = context;
        this.complex = complex;
        this.cache = ObjectKit.isEmpty(cache) ? PayCache.INSTANCE : cache;
        if (!Checker.isSupportedPay(this.context, complex)) {
            throw new PaymentException(ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        Checker.checkConfig(this.context, complex);
    }

    /**
     * 获取 {@link PayScope} 数组中所有的被标记为 {@code default} 的 scope
     *
     * @param scopes scopes
     * @return {@link List}
     */
    public static List<String> getDefaultScopes(PayScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).filter((PayScope::isDefault)).map(PayScope::getScope).collect(Collectors.toList());
    }

    /**
     * 从 {@link PayScope} 数组中获取实际的 scope 字符串
     *
     * @param scopes 可变参数，支持传任意 {@link PayScope}
     * @return {@link List}
     */
    public static List<String> getScopes(PayScope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).map(PayScope::getScope).collect(Collectors.toList());
    }

    /**
     * get 请求
     *
     * @param url     请求url
     * @param formMap 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String get(String url, Map<String, String> formMap) {
        return Httpx.get(url, formMap);
    }

    /**
     * post 请求
     *
     * @param url  请求url
     * @param data 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data) {
        try {
            return Httpz.post().url(url).body(data).build().execute().body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url       请求url
     * @param data      请求参数
     * @param headerMap 请求头
     * @return {@link Message} 请求返回的结果
     */
    public static Message post(String url, String data, Map<String, String> headerMap) {
        try {
            Response response = postTo(url, headerMap, data);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get 请求
     *
     * @param url       请求url
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return {@link Message} 请求返回的结果
     */
    public static Message get(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        try {
            Response response = getTo(url, formMap, headerMap);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url       请求url
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return {@link Message} 请求返回的结果
     */
    public static Message post(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        try {
            Response response = postTo(url, headerMap, formMap);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url       请求url
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @param file      文件
     * @return {@link Message} 请求返回的结果
     */
    public static Message post(String url, Map<String, String> formMap, Map<String, String> headerMap, File file) {
        try {
            Response response = postTo(url, headerMap, formMap);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, String certPath, String certPass, String protocol) {
        try {
            if (StringKit.isEmpty(protocol)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(certPath, null, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.create(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            return call.execute().body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certFile 证书文件输入流
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, InputStream certFile, String certPass, String protocol) {
        try {
            if (StringKit.isEmpty(Protocol.TLSv1.name)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(null, certFile, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.create(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            return call.execute().body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * put 请求
     *
     * @param url       请求url
     * @param data      请求参数
     * @param headerMap 请求头
     * @return {@link Message} 请求返回的结果
     */
    public static Message put(String url, String data, Map<String, String> headerMap) {
        try {
            Response response = putTo(url, headerMap, data);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param filePath 上传文件路径
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public static String upload(String url, String data, String certPath, String certPass, String filePath,
            String protocol) {

        SSLSocketFactory sslSocketFactory = getSslSocketFactory(certPath, null, certPass, protocol);

        try {
            return Httpz.post().url(url).addFile(null, null, FileKit.newFile(filePath))
                    .addHeader("Content-Type", "multipart/form-data;boundary=\"boundary\"").build().execute().body()
                    .string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param filePath 上传文件路径
     * @return {@link String} 请求返回的结果
     */
    public static String upload(String url, String data, String certPath, String certPass, String filePath) {
        return upload(url, data, certPath, certPass, filePath, Protocol.TLSv1.name);
    }

    /**
     * get 请求
     *
     * @param url       请求url
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return {@link Response} 请求返回的结果
     */
    private static Response getTo(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        try {
            return Httpz.get().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url       请求url
     * @param headerMap 请求头
     * @param data      请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response postTo(String url, Map<String, String> headerMap, String data) {
        try {
            return Httpz.post().url(url).addHeader(headerMap).body(data).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url       请求url
     * @param headerMap 请求头
     * @param formMap   请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response postTo(String url, Map<String, String> headerMap, Map<String, String> formMap) {
        try {
            return Httpz.post().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * put 请求
     *
     * @param url       请求url
     * @param headerMap 请求头
     * @param data      请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response putTo(String url, Map<String, String> headerMap, String data) {
        try {
            return Httpz.put().url(url).addHeader(headerMap).body(data).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null;) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将同步通知的参数转化为Map
     *
     * @param request {@link HttpServletRequest}
     * @return 转化后的 Map
     */
    public static Map<String, String> toMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    @SneakyThrows
    private static KeyManager[] getKeyManager(String certPass, String certPath, InputStream certFile) {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        if (certFile != null) {
            clientStore.load(certFile, certPass.toCharArray());
        } else {
            clientStore.load(Files.newInputStream(Paths.get(certPath)), certPass.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, certPass.toCharArray());
        return kmf.getKeyManagers();
    }

    @SneakyThrows
    private static SSLSocketFactory getSslSocketFactory(String certPath, InputStream certFile, String certPass,
            String protocol) {
        SSLContextBuilder sslContextBuilder = SSLContextBuilder.of();
        sslContextBuilder.setProtocol(protocol);
        sslContextBuilder.setKeyManagers(getKeyManager(certPass, certPath, certFile));
        sslContextBuilder.setSecureRandom(new SecureRandom());
        return sslContextBuilder.buildChecked().getSocketFactory();
    }

    /**
     * 处理发生异常的情况，统一响应参数
     *
     * @param e 具体的异常
     * @return Message
     */
    protected Message responseError(Exception e) {
        String errorCode = ErrorCode.FAILURE.getCode();
        String errorMsg = e.getMessage();
        if (e instanceof PaymentException) {
            PaymentException authException = ((PaymentException) e);
            errorCode = authException.getErrcode();
            if (StringKit.isNotEmpty(authException.getErrmsg())) {
                errorMsg = authException.getErrmsg();
            }
        }
        return Message.builder().errcode(errorCode).errmsg(errorMsg).build();
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
        String scope = String.join(separator, scopes);
        return encode ? UrlEncoder.encodeAll(scope) : scope;
    }

    /**
     * get 请求
     *
     * @param url 请求url
     * @return {@link String} 请求返回的结果
     */
    public String get(String url) {
        return Httpx.get(url);
    }

    /**
     * post 请求
     *
     * @param url     请求url
     * @param formMap 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String post(String url, Map<String, String> formMap) {
        return Httpx.post(url, formMap);
    }

    /**
     * put 请求
     *
     * @param url       请求url
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return {@link Message} 请求返回的结果
     */
    public Message put(String url, Map<String, Object> formMap, Map<String, String> headerMap) {
        try {
            Response response = putTo(url, headerMap, formMap);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * put 请求
     *
     * @param url       请求url
     * @param headerMap 请求头
     * @param formMap   请求参数
     * @return {@link Response} 请求返回的结果
     */
    private Response putTo(String url, Map<String, String> headerMap, Map<String, Object> formMap) {
        try {
            return Httpz.put().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
