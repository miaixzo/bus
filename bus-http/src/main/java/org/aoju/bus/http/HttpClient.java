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
package org.aoju.bus.http;

import org.aoju.bus.core.consts.Httpd;
import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;
import org.aoju.bus.http.accord.ConnectionPool;
import org.aoju.bus.http.bodys.MultipartBody;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.offers.Dispatcher;
import org.aoju.bus.http.offers.Dns;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Http 辅助类
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class HttpClient extends Client {

    /**
     * 懒汉安全加同步
     * 私有的静态成员变量 只声明不创建
     * 私有的构造方法
     * 提供返回实例的静态方法
     */
    private static Client client;

    static {
        client = new HttpClient(new X509TrustManager());
    }

    /**
     * 提供返回实例的静态方法
     */
    public HttpClient() {
        this(30, 30, 30);
    }

    /**
     * 提供返回实例的静态方法
     *
     * @param x509TrustManager 信任管理器
     */
    public HttpClient(X509TrustManager x509TrustManager) {
        this(null, null, 30, 30, 30, 64, 5, 5, 5, createTrustAllSSLFactory(x509TrustManager), x509TrustManager, createTrustAllHostnameVerifier());
    }

    /**
     * 构建一个自定义配置的 HTTP Client 类
     *
     * @param connTimeout  连接
     * @param readTimeout  读取
     * @param writeTimeout 输出
     */
    public HttpClient(int connTimeout,
                      int readTimeout,
                      int writeTimeout) {
        this(null, null, connTimeout, readTimeout, writeTimeout, 64, 5, 5, 5);
    }

    /**
     * 构建一个自定义配置的 HTTP Client 类
     *
     * @param connTimeout        连接
     * @param readTimeout        读取
     * @param writeTimeout       输出
     * @param maxRequests        最大请求
     * @param maxRequestsPerHost 主机最大请求
     * @param maxIdleConnections 最大连接
     * @param keepAliveDuration  链接时长
     */
    public HttpClient(int connTimeout,
                      int readTimeout,
                      int writeTimeout,
                      int maxRequests,
                      int maxRequestsPerHost,
                      int maxIdleConnections,
                      int keepAliveDuration) {
        this(null, null, connTimeout, readTimeout, writeTimeout, maxRequests, maxRequestsPerHost, maxIdleConnections, keepAliveDuration);
    }

    /**
     * 构建一个自定义配置的 HTTP Client 类
     *
     * @param dns                DNS 信息
     * @param proxy              代理信息
     * @param connTimeout        连接
     * @param readTimeout        读取
     * @param writeTimeout       输出
     * @param maxRequests        最大请求
     * @param maxRequestsPerHost 主机最大请求
     * @param maxIdleConnections 最大连接
     * @param keepAliveDuration  链接时长
     */
    public HttpClient(Dns dns,
                      Proxy proxy,
                      int connTimeout,
                      int readTimeout,
                      int writeTimeout,
                      int maxRequests,
                      int maxRequestsPerHost,
                      int maxIdleConnections,
                      int keepAliveDuration
    ) {
        this(dns, proxy, connTimeout, readTimeout, writeTimeout, maxRequests, maxRequestsPerHost, maxIdleConnections, keepAliveDuration, null, null, null);
    }

    /**
     * 构建一个自定义配置的 HTTP Client 类
     *
     * @param dns                DNS 信息
     * @param proxy              代理信息
     * @param connTimeout        连接
     * @param readTimeout        读取
     * @param writeTimeout       输出
     * @param maxRequests        最大请求
     * @param maxRequestsPerHost 主机最大请求
     * @param maxIdleConnections 最大连接
     * @param keepAliveDuration  链接时长
     * @param sslSocketFactory   抽象类，扩展自SocketFactory, SSLSocket的工厂
     * @param x509TrustManager   证书信任管理器
     * @param hostnameVerifier   主机名校验信息
     */
    public HttpClient(final Dns dns,
                      final Proxy proxy,
                      int connTimeout,
                      int readTimeout,
                      int writeTimeout,
                      int maxRequests,
                      int maxRequestsPerHost,
                      int maxIdleConnections,
                      int keepAliveDuration,
                      SSLSocketFactory sslSocketFactory,
                      javax.net.ssl.X509TrustManager x509TrustManager,
                      HostnameVerifier hostnameVerifier
    ) {
        synchronized (HttpClient.class) {
            if (ObjectUtils.isEmpty(client)) {
                Dispatcher dispatcher = new Dispatcher();
                dispatcher.setMaxRequests(maxRequests);
                dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);
                ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections,
                        keepAliveDuration, TimeUnit.MINUTES);
                Client.Builder builder = new Client.Builder();

                builder.dispatcher(dispatcher);
                builder.connectionPool(connectionPool);
                builder.addNetworkInterceptor(chain -> {
                    Request request = chain.request();
                    return chain.proceed(request);
                });
                if (ObjectUtils.isNotEmpty(dns)) {
                    builder.dns(hostname -> {
                        try {
                            return dns.lookup(hostname);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Dns.SYSTEM.lookup(hostname);
                    });
                }
                if (ObjectUtils.isNotEmpty(proxy)) {
                    builder.proxy(proxy.proxy());
                    if (proxy.user != null && proxy.password != null) {
                        builder.proxyAuthenticator(proxy.authenticator());
                    }
                }
                builder.connectTimeout(connTimeout, TimeUnit.SECONDS);
                builder.readTimeout(readTimeout, TimeUnit.SECONDS);
                builder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
                if (ObjectUtils.isNotEmpty(sslSocketFactory)) {
                    builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
                }
                if (ObjectUtils.isNotEmpty(hostnameVerifier)) {
                    builder.hostnameVerifier(hostnameVerifier);
                }
                client = builder.build();
            }
        }
    }

    /**
     * 简单的 GET 请求 使用默认编码 UTF-8
     *
     * @param url URL地址 String
     * @return String
     */
    public static String get(final String url) {
        return get(url, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 简单的 GET 请求 使用自定义编码
     *
     * @param url     URL地址 String
     * @param charset 自定义编码 String
     * @return String
     */
    public static String get(final String url, final String charset) {
        return execute(Builder.builder().url(url).requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * 异步get请求，回调
     *
     * @param url     URL地址
     * @param isAsync 是否异步
     * @return String
     */
    public static String get(final String url, final boolean isAsync) {
        if (isAsync) {
            return enqueue(Builder.builder().url(url).method(Httpd.GET).build());
        }
        return get(url);
    }

    /**
     * 带查询参数 GET 请求 使用默认编码 UTF-8
     *
     * @param url      URL地址 String
     * @param queryMap 查询参数 Map
     * @return String
     */
    public static String get(final String url, final Map<String, Object> queryMap) {
        return get(url, queryMap, null, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 GET 请求 使用默认编码 UTF-8
     *
     * @param url       URL地址 String
     * @param queryMap  查询参数 Map
     * @param headerMap Header参数 Map
     * @return String
     */
    public static String get(final String url, final Map<String, Object> queryMap, Map<String, String> headerMap) {
        return get(url, queryMap, headerMap, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 GET 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param queryMap  查询参数 Map
     * @param headerMap Header参数 Map
     * @param charset   自定义编码 String
     * @return String
     */
    public static String get(final String url, final Map<String, Object> queryMap, Map<String, String> headerMap,
                             final String charset) {
        return execute(Builder.builder().url(url).headerMap(headerMap).queryMap(queryMap)
                .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * form 方式 POST 请求
     *
     * @param url URL地址 String
     * @return String
     */
    public static String post(final String url) {
        return post(url, null);
    }

    /**
     * form 方式 POST 请求
     * application/x-www-form-urlencoded
     *
     * @param url     URL地址 String
     * @param formMap 查询参数 Map
     * @return String
     */
    public static String post(final String url, final Map<String, Object> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
        }
        return post(url, data, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * 带查询参数 POST 请求 使用默认编码 UTF-8
     *
     * @param url       URL地址 String
     * @param data      请求数据 String
     * @param mediaType 类型 String
     * @return String
     */
    public static String post(final String url, final String data,
                              final String mediaType) {
        return post(url, data, mediaType, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 POST 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param data      请求数据 String
     * @param mediaType 类型 String
     * @param charset   自定义编码 String
     * @return String
     */
    public static String post(final String url, final String data, final String mediaType,
                              final String charset) {
        return execute(Builder.builder().url(url).method(Httpd.POST).data(data).mediaType(mediaType)
                .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * 带查询参数 POST 请求 使用默认编码 UTF-8
     *
     * @param url       URL地址 String
     * @param queryMap  请求数据 Map
     * @param mediaType 类型 String
     * @return String
     */
    public static String post(final String url, final Map<String, Object> queryMap,
                              final String mediaType) {
        return post(url, queryMap, mediaType, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 POST 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param headerMap 头部数据 Map
     * @param queryMap  请求数据 Map
     * @return String
     */
    public static String post(final String url, final Map<String, Object> queryMap,
                              final Map<String, String> headerMap) {
        return post(url, queryMap, headerMap, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 POST 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param queryMap  请求数据 Map
     * @param mediaType 类型 String
     * @param charset   自定义编码 String
     * @return String
     */
    public static String post(final String url, final Map<String, Object> queryMap,
                              final String mediaType, final String charset) {
        return execute(Builder.builder().url(url).method(Httpd.POST).queryMap(queryMap).mediaType(mediaType)
                .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * 带查询参数 POST 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param headerMap 头部数据 Map
     * @param queryMap  请求数据 Map
     * @param mediaType 类型 String
     * @return String
     */
    public static String post(final String url, final Map<String, Object> queryMap,
                              final Map<String, String> headerMap, final String mediaType) {
        return post(url, queryMap, headerMap, mediaType, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 带查询参数 POST 请求 使用自定义编码
     *
     * @param url       URL地址 String
     * @param headerMap 头部数据 Map
     * @param queryMap  请求数据 Map
     * @param mediaType 类型 String
     * @param charset   自定义编码 String
     * @return String
     */
    public static String post(final String url, final Map<String, Object> queryMap,
                              final Map<String, String> headerMap, final String mediaType,
                              final String charset) {
        return execute(Builder.builder().url(url).method(Httpd.POST).headerMap(headerMap).queryMap(queryMap)
                .mediaType(mediaType).requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * 表单提交带文件上传
     *
     * @param url      请求地址 String
     * @param params   请求参数 Map
     * @param pathList 上传文件 List
     * @return String
     */
    public static String post(final String url, final Map<String, Object> params,
                              final List<String> pathList) {
        MediaType mediaType = MediaType
                .get(MediaType.APPLICATION_FORM_URLENCODED + ";" + org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
        RequestBody bodyParams = RequestBody.create(mediaType, params.toString());
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE)
                .addFormDataPart("params", "", bodyParams);

        File file;
        for (String path : pathList) {
            file = new File(path);
            requestBodyBuilder.addFormDataPart("file", file.getName(), RequestBody.create(mediaType, new File(path)));
        }
        RequestBody requestBody = requestBodyBuilder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        String result = "";
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                byte[] bytes = response.body().bytes();
                result = new String(bytes, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
            }
        } catch (Exception e) {
            Logger.error(">>>>>>>>error requesting HTTP upload file form request<<<<<<<<", e);
        }
        return result;
    }

    /**
     * Process the Http request Map
     *
     * @param map map
     * @return The result is output<code>String</code>。
     */
    public static String getParameterMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        if (ObjectUtils.isNotEmpty(map)) {
            Set<String> keys = map.keySet();
            for (String key : keys) {
                sb.append(key).append("=").append(map.get(key)).append("&");
            }
        }
        return sb.toString();
    }

    /**
     * 对URL参数做编码，只编码键和值<br>
     * 提供的值可以是url附带参数，但是不能只是url
     *
     * <p>注意，此方法只能标准化整个URL，并不适合于单独编码参数值</p>
     *
     * @param paramsStr url参数，可以包含url本身
     * @param charset   编码
     * @return 编码后的url和参数
     */
    public static String encode(String paramsStr, java.nio.charset.Charset charset) {
        if (StringUtils.isBlank(paramsStr)) {
            return Normal.EMPTY;
        }

        String urlPart = null; // url部分，不包括问号
        String paramPart; // 参数部分
        int pathEndPos = paramsStr.indexOf('?');
        if (pathEndPos > -1) {
            // url + 参数
            urlPart = StringUtils.subPre(paramsStr, pathEndPos);
            paramPart = StringUtils.subSuf(paramsStr, pathEndPos + 1);
            if (StringUtils.isBlank(paramPart)) {
                // 无参数，返回url
                return urlPart;
            }
        } else {
            // 无URL
            paramPart = paramsStr;
        }

        paramPart = normalizeParams(paramPart, charset);

        return StringUtils.isBlank(urlPart) ? paramPart : urlPart + "?" + paramPart;
    }

    /**
     * 标准化参数字符串，即URL中？后的部分
     *
     * <p>注意，此方法只能标准化整个URL，并不适合于单独编码参数值</p>
     *
     * @param paramPart 参数字符串
     * @param charset   编码
     * @return 标准化的参数字符串
     */
    public static String normalizeParams(String paramPart, java.nio.charset.Charset charset) {
        final TextUtils builder = TextUtils.create(paramPart.length() + 16);
        final int len = paramPart.length();
        String name = null;
        int pos = 0; // 未处理字符开始位置
        char c; // 当前字符
        int i; // 当前字符位置
        for (i = 0; i < len; i++) {
            c = paramPart.charAt(i);
            if (c == '=') { // 键值对的分界点
                if (null == name) {
                    // 只有=前未定义name时被当作键值分界符，否则做为普通字符
                    name = (pos == i) ? Normal.EMPTY : paramPart.substring(pos, i);
                    pos = i + 1;
                }
            } else if (c == '&') { // 参数对的分界点
                if (pos != i) {
                    if (null == name) {
                        // 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
                        name = paramPart.substring(pos, i);
                        builder.append(UriUtils.encodeQuery(name, charset)).append('=');
                    } else {
                        builder.append(UriUtils.encodeQuery(name, charset)).append('=').append(UriUtils.encodeQuery(paramPart.substring(pos, i), charset)).append('&');
                    }
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 结尾处理
        if (null != name) {
            builder.append(UriUtils.encodeQuery(name, charset)).append('=');
        }
        if (pos != i) {
            if (null == name && pos > 0) {
                builder.append('=');
            }
            builder.append(UriUtils.encodeQuery(paramPart.substring(pos, i), charset));
        }

        // 以&结尾则去除之
        int lastIndex = builder.length() - 1;
        if ('&' == builder.charAt(lastIndex)) {
            builder.delTo(lastIndex);
        }
        return builder.toString();
    }

    /**
     * 将Map形式的Form表单数据转换为Url参数形式<br>
     * paramMap中如果key为空（null和""）会被忽略，如果value为null，会被做为空白符（""）<br>
     * 会自动url编码键和值
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap 表单数据
     * @param charset  编码
     * @return url参数
     */
    public static String toParams(Map<String, ?> paramMap, java.nio.charset.Charset charset) {
        if (CollUtils.isEmpty(paramMap)) {
            return Normal.EMPTY;
        }
        if (null == charset) {// 默认编码为系统编码
            charset = org.aoju.bus.core.consts.Charset.UTF_8;
        }

        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        String key;
        Object value;
        String valueStr;
        for (Map.Entry<String, ?> item : paramMap.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("&");
            }
            key = item.getKey();
            value = item.getValue();
            if (value instanceof Iterable) {
                value = CollUtils.join((Iterable<?>) value, ",");
            } else if (value instanceof Iterator) {
                value = CollUtils.join((Iterator<?>) value, ",");
            }
            valueStr = Convert.toString(value);
            if (StringUtils.isNotEmpty(key)) {
                sb.append(UriUtils.encodeAll(key, charset)).append("=");
                if (StringUtils.isNotEmpty(valueStr)) {
                    sb.append(UriUtils.encodeAll(valueStr, charset));
                }
            }
        }
        return sb.toString();
    }


    /**
     * 将URL参数解析为Map（也可以解析Post中的键值对参数）
     *
     * @param params  参数字符串（或者带参数的Path）
     * @param charset 字符集
     * @return 参数Map
     */
    public static Map<String, String> decodeMap(String params, String charset) {
        final Map<String, List<String>> paramsMap = decode(params, charset);
        final Map<String, String> result = MapUtils.newHashMap(paramsMap.size());
        List<String> list;
        for (Map.Entry<String, List<String>> entry : paramsMap.entrySet()) {
            list = entry.getValue();
            result.put(entry.getKey(), CollUtils.isEmpty(list) ? null : list.get(0));
        }
        return result;
    }

    /**
     * 将URL参数解析为Map（也可以解析Post中的键值对参数）
     *
     * @param params  参数字符串（或者带参数的Path）
     * @param charset 字符集
     * @return 参数Map
     */
    public static Map<String, List<String>> decode(String params, String charset) {
        if (StringUtils.isBlank(params)) {
            return Collections.emptyMap();
        }

        // 去掉Path部分
        int pathEndPos = params.indexOf('?');
        if (pathEndPos > -1) {
            params = StringUtils.subSuf(params, pathEndPos + 1);
        }

        final Map<String, List<String>> map = new LinkedHashMap<>();
        final int len = params.length();
        String name = null;
        int pos = 0; // 未处理字符开始位置
        int i; // 未处理字符结束位置
        char c; // 当前字符
        for (i = 0; i < len; i++) {
            c = params.charAt(i);
            if (c == '=') { // 键值对的分界点
                if (null == name) {
                    // name可以是""
                    name = params.substring(pos, i);
                }
                pos = i + 1;
            } else if (c == '&') { // 参数对的分界点
                if (null == name && pos != i) {
                    // 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
                    addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
                } else if (name != null) {
                    addParam(map, name, params.substring(pos, i), charset);
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 处理结尾
        if (pos != i) {
            if (name == null) {
                addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
            } else {
                addParam(map, name, params.substring(pos, i), charset);
            }
        } else if (name != null) {
            addParam(map, name, Normal.EMPTY, charset);
        }

        return map;
    }

    /**
     * 将表单数据加到URL中（用于GET表单提交）<br>
     * 表单的键值对会被url编码，但是url中原参数不会被编码
     *
     * @param url            URL
     * @param form           表单数据
     * @param charset        编码
     * @param isEncodeParams 是否对键和值做转义处理
     * @return 合成后的URL
     */
    public static String withForm(String url, Map<String, Object> form, java.nio.charset.Charset charset, boolean isEncodeParams) {
        if (isEncodeParams && StringUtils.contains(url, '?')) {
            url = encode(url, charset);
        }

        return withForm(url, toParams(form, charset), charset, false);
    }

    /**
     * 将表单数据字符串加到URL中（用于GET表单提交）
     *
     * @param url         URL
     * @param queryString 表单数据字符串
     * @param charset     编码
     * @param isEncode    是否对键和值做转义处理
     * @return 拼接后的字符串
     */
    public static String withForm(String url, String queryString, java.nio.charset.Charset charset, boolean isEncode) {
        if (StringUtils.isBlank(queryString)) {
            if (StringUtils.contains(url, '?')) {
                return isEncode ? encode(url, charset) : url;
            }
            return url;
        }

        final TextUtils textUtils = TextUtils.create(url.length() + queryString.length() + 16);
        int qmIndex = url.indexOf('?');
        if (qmIndex > 0) {
            textUtils.append(isEncode ? encode(url, charset) : url);
            if (false == StringUtils.endWith(url, '&')) {
                textUtils.append('&');
            }
        } else {
            textUtils.append(url);
            if (qmIndex < 0) {
                textUtils.append('?');
            }
        }
        textUtils.append(isEncode ? encode(queryString, charset) : queryString);
        return textUtils.toString();
    }

    /**
     * 将键值对加入到值为List类型的Map中
     *
     * @param params  参数
     * @param name    key
     * @param value   value
     * @param charset 编码
     */
    private static void addParam(Map<String, List<String>> params, String name, String value, String charset) {
        name = UriUtils.decode(name, charset);
        value = UriUtils.decode(value, charset);
        List<String> values = params.get(name);
        if (values == null) {
            values = new ArrayList<>(1);
            params.put(name, values);
        }
        values.add(value);
    }

    /**
     * 通用同步执行方法
     *
     * @param builder Builder
     * @return String
     */
    private static String execute(final Builder builder) {
        if (StringUtils.isBlank(builder.requestCharset)) {
            builder.requestCharset = org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8;
        }
        if (StringUtils.isBlank(builder.responseCharset)) {
            builder.responseCharset = org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8;
        }
        if (StringUtils.isBlank(builder.method)) {
            builder.method = Httpd.GET;
        }
        if (StringUtils.isBlank(builder.mediaType)) {
            builder.mediaType = MediaType.APPLICATION_FORM_URLENCODED;
        }
        if (builder.tracer) {
            Logger.info(">>>>>>>>Builder[{}]<<<<<<<<", builder.toString());
        }
        String url = builder.url;
        Request.Builder request = new Request.Builder();

        if (MapUtils.isNotEmpty(builder.queryMap)) {
            String queryParams = builder.queryMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
            url = String.format("%s%s%s", url, url.contains("?") ? "&" : "?", queryParams);
        }
        request.url(url);
        if (MapUtils.isNotEmpty(builder.headerMap)) {
            builder.headerMap.forEach(request::addHeader);
        }
        String method = builder.method.toUpperCase();
        String mediaType = String.format("%s;charset=%s", builder.mediaType, builder.requestCharset);
        if (StringUtils.equals(method, Httpd.GET)) {
            request.get();
        } else if (ArrayUtils.contains(new String[]{Httpd.POST, Httpd.PUT, Httpd.DELETE, Httpd.PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.get(mediaType), builder.data);
            request.method(method, requestBody);
        } else {
            throw new InstrumentException(String.format(">>>>>>>>request method not found [%s]<<<<<<<<", method));
        }
        String result = "";
        try {
            Request build = request.build();
            Response response = client.newCall(build).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                byte[] bytes = response.body().bytes();
                result = new String(bytes, builder.responseCharset);
            }
            if (builder.tracer) {
                Logger.info(">>>>>>>>Url[{}],response[{}]<<<<<<<<", url, result);
            }
        } catch (Exception e) {
            Logger.error(">>>>>>>>Builder[{}] error<<<<<<<<", builder.toString(), e);
        }
        return result;
    }

    /**
     * 通用异步执行方法
     *
     * @param builder Builder
     * @return String
     */
    private static String enqueue(final Builder builder) {
        if (StringUtils.isBlank(builder.requestCharset)) {
            builder.requestCharset = org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8;
        }
        if (StringUtils.isBlank(builder.responseCharset)) {
            builder.responseCharset = org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8;
        }
        if (StringUtils.isBlank(builder.method)) {
            builder.method = Httpd.GET;
        }
        if (StringUtils.isBlank(builder.mediaType)) {
            builder.mediaType = MediaType.APPLICATION_FORM_URLENCODED;
        }
        if (builder.tracer) {
            Logger.info(builder.toString());
        }
        String url = builder.url;
        Request.Builder request = new Request.Builder();
        if (MapUtils.isNotEmpty(builder.queryMap)) {
            String queryParams = builder.queryMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
            url = String.format("%s%s%s", url, url.contains("?") ? "&" : "?", queryParams);
        }
        request.url(url);
        if (MapUtils.isNotEmpty(builder.headerMap)) {
            builder.headerMap.forEach(request::addHeader);
        }
        String method = builder.method.toUpperCase();
        String mediaType = String.format("%s;charset=%s", builder.mediaType, builder.requestCharset);
        if (StringUtils.equals(method, Httpd.GET)) {
            request.get();
        } else if (ArrayUtils.contains(new String[]{Httpd.POST, Httpd.PUT, Httpd.DELETE, Httpd.PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.get(mediaType), builder.data);
            request.method(method, requestBody);
        } else {
            throw new InstrumentException(String.format(">>>>>>>>request method not found[%s]<<<<<<<<", method));
        }
        final String[] result = {""};
        try {
            String finalUrl = url;
            Call call = client.newCall(request.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.info(String.format(">>>>>>>>Url[%s]failure<<<<<<<<", finalUrl));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        byte[] bytes = response.body().bytes();
                        result[0] = new String(bytes, builder.responseCharset);
                        if (builder.tracer) {
                            Logger.info(">>>>>>>>Url[{}],response[{}]<<<<<<<<", finalUrl, result[0]);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Logger.error(builder.toString(), e);
        }
        return result[0];
    }

    /**
     * Https SSL证书
     *
     * @param X509TrustManager
     * @return SSLSocketFactory
     */
    private static SSLSocketFactory createTrustAllSSLFactory(X509TrustManager X509TrustManager) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{X509TrustManager}, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 HostnameVerifier
     *
     * @return
     */
    private static HostnameVerifier createTrustAllHostnameVerifier() {
        return (hostname, session) -> true;
    }

    private static class X509TrustManager implements javax.net.ssl.X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

    @lombok.Builder
    @lombok.ToString
    private static class Builder {
        /**
         * 请求 url
         */
        private String url;
        /**
         * 方法类型
         */
        private String method;
        /**
         * 请求参数
         */
        private String data;
        /**
         * 数据格式类型
         */
        private String mediaType;
        /**
         * 请求参数
         */
        private Map<String, Object> queryMap;
        /**
         * 头部参数
         */
        private Map<String, String> headerMap;

        /**
         * 请求编码
         */
        private String requestCharset;
        /**
         * 响应编码
         */
        private String responseCharset;
        /**
         * 日志追踪
         */
        private boolean tracer;
    }

}