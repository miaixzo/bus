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
package org.miaixz.bus.core.xyz;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.net.url.UrlQuery;

/**
 * URL（Uniform Resource Locator）统一资源定位符相关工具类
 *
 * <p>
 * 统一资源定位符，描述了一台特定服务器上某资源的特定位置。
 * </p>
 * URL组成：
 * 
 * <pre>
 *   协议://主机名[:端口]/ 路径/[:参数] [?查询]#Fragment
 *   protocol :// hostname[:port] / path / [:parameters][?query]#fragment
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlKit {

    /**
     * 将{@link URI}转换为{@link URL}
     *
     * @param uri {@link URI}
     * @return URL对象
     * @throws InternalException {@link MalformedURLException}包装，URI格式有问题时抛出
     * @see URI#toURL()
     */
    public static URL url(final URI uri) throws InternalException {
        if (null == uri) {
            return null;
        }
        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 通过一个字符串形式的URL地址创建URL对象
     *
     * @param url URL
     * @return URL对象
     */
    public static URL url(final String url) {
        return url(url, null);
    }

    /**
     * 通过一个字符串形式的URL地址创建URL对象
     *
     * @param url     URL
     * @param handler {@link URLStreamHandler}
     * @return URL对象
     */
    public static URL url(String url, final URLStreamHandler handler) {
        if (null == url) {
            return null;
        }

        // 兼容Spring的ClassPath路径
        if (url.startsWith(Normal.CLASSPATH)) {
            url = url.substring(Normal.CLASSPATH.length());
            return ClassKit.getClassLoader().getResource(url);
        }

        try {
            return new URL(null, url, handler);
        } catch (final MalformedURLException e) {
            if (e.getMessage().contains("Accessing an URL protocol that was not enabled")) {
                // Graalvm打包需要手动指定参数开启协议：
                // --enable-url-protocols=http
                // --enable-url-protocols=https
                throw new InternalException(e);
            }

            // 尝试文件路径
            try {
                return new File(url).toURI().toURL();
            } catch (final MalformedURLException ex2) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * 获取string协议的URL，类似于string:///xxxxx
     *
     * @param content 正文
     * @return URL
     */
    public static URI getStringURI(final CharSequence content) {
        if (null == content) {
            return null;
        }
        return URI.create(StringKit.addPrefixIfNot(content, "string:///"));
    }

    /**
     * 将URL字符串转换为URL对象，并做必要验证
     *
     * @param urlStr URL字符串
     * @return URL
     */
    public static URL toUrlForHttp(final String urlStr) {
        return toUrlForHttp(urlStr, null);
    }

    /**
     * 将URL字符串转换为URL对象，并做必要验证
     *
     * @param urlStr  URL字符串
     * @param handler {@link URLStreamHandler}
     * @return URL
     */
    public static URL toUrlForHttp(String urlStr, final URLStreamHandler handler) {
        Assert.notBlank(urlStr, "Url is blank !");
        // 编码空白符，防止空格引起的请求异常
        urlStr = UrlEncoder.encodeBlank(urlStr);
        try {
            return new URL(null, urlStr, handler);
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得URL
     *
     * @param pathBaseClassLoader 相对路径（相对于classes）
     * @return URL
     * @see ResourceKit#getResourceUrl(String)
     */
    public static URL getURL(final String pathBaseClassLoader) {
        return ResourceKit.getResourceUrl(pathBaseClassLoader);
    }

    /**
     * 获得URL
     *
     * @param path  相对给定 class所在的路径
     * @param clazz 指定class
     * @return URL
     * @see ResourceKit#getResourceUrl(String, Class)
     */
    public static URL getURL(final String path, final Class<?> clazz) {
        return ResourceKit.getResourceUrl(path, clazz);
    }

    /**
     * 获得URL，常用于使用绝对路径时的情况
     *
     * @param file URL对应的文件对象
     * @return URL
     * @throws InternalException URL格式错误
     */
    public static URL getURL(final File file) {
        Assert.notNull(file, "File is null !");
        try {
            return file.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }
    }

    /**
     * 获取相对于给定URL的新的URL 来自：org.springframework.core.io.UrlResource#createRelativeURL
     *
     * @param url          基础URL
     * @param relativePath 相对路径
     * @return 相对于URL的子路径URL
     * @throws InternalException URL格式错误
     */
    public static URL getURL(final URL url, String relativePath) throws InternalException {
        // # 在文件路径中合法，但是在URL中非法，此处转义
        relativePath = StringKit.replace(StringKit.removePrefix(relativePath, Symbol.SLASH), Symbol.SHAPE, "%23");
        try {
            return new URL(url, relativePath);
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }
    }

    /**
     * 获得URL，常用于使用绝对路径时的情况
     *
     * @param files URL对应的文件对象
     * @return URL
     * @throws InternalException URL格式错误
     */
    public static URL[] getURLs(final File... files) {
        final URL[] urls = new URL[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        } catch (final MalformedURLException e) {
            throw new InternalException(e, "Error occurred when get URL!");
        }

        return urls;
    }

    /**
     * 获取URL中域名部分，只保留URL中的协议（Protocol）、Host，其它为null。
     *
     * @param url URL
     * @return 域名的URI
     */
    public static URI getHost(final URL url) {
        if (null == url) {
            return null;
        }

        try {
            return new URI(url.getProtocol(), url.getHost(), null, null);
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 补全相对路径
     *
     * @param baseUrl      基准URL
     * @param relativePath 相对URL
     * @return 相对路径
     * @throws InternalException MalformedURLException
     */
    public static String completeUrl(String baseUrl, final String relativePath) {
        baseUrl = normalize(baseUrl, false);
        if (StringKit.isBlank(baseUrl)) {
            return null;
        }

        try {
            final URL absoluteUrl = new URL(baseUrl);
            final URL parseUrl = new URL(absoluteUrl, relativePath);
            return parseUrl.toString();
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得path部分
     *
     * @param uriStr URI路径
     * @return path
     * @throws InternalException 包装URISyntaxException
     */
    public static String getPath(final String uriStr) {
        return toURI(uriStr).getPath();
    }

    /**
     * 从URL对象中获取不被编码的路径Path 对于本地路径，URL对象的getPath方法对于包含中文或空格时会被编码，导致本读路径读取错误。 此方法将URL转为URI后获取路径用于解决路径被编码的问题
     *
     * @param url {@link URL}
     * @return 路径
     */
    public static String getDecodedPath(final URL url) {
        if (null == url) {
            return null;
        }

        String path = null;
        try {
            // URL对象的getPath方法对于包含中文或空格的问题
            path = toURI(url).getPath();
        } catch (final InternalException e) {
            // ignore
        }
        return (null != path) ? path : url.getPath();
    }

    /**
     * 转URL为URI
     *
     * @param url URL
     * @return URI
     * @throws InternalException 包装URISyntaxException
     */
    public static URI toURI(final URL url) throws InternalException {
        return toURI(url, false);
    }

    /**
     * 转URL为URI
     *
     * @param url      URL
     * @param isEncode 是否编码参数中的特殊字符（默认UTF-8编码）
     * @return URI
     * @throws InternalException 包装URISyntaxException
     */
    public static URI toURI(final URL url, final boolean isEncode) throws InternalException {
        if (null == url) {
            return null;
        }

        return toURI(url.toString(), isEncode);
    }

    /**
     * 转字符串为URI
     *
     * @param location 字符串路径
     * @return URI
     * @throws InternalException 包装URISyntaxException
     */
    public static URI toURI(final String location) throws InternalException {
        return toURI(location, false);
    }

    /**
     * 转字符串为URI
     *
     * @param location 字符串路径
     * @param isEncode 是否编码参数中的特殊字符（默认UTF-8编码）
     * @return URI
     * @throws InternalException 包装URISyntaxException
     */
    public static URI toURI(String location, final boolean isEncode) throws InternalException {
        if (isEncode) {
            location = RFC3986.PATH.encode(location, Charset.UTF_8);
        }
        try {
            return new URI(StringKit.trim(location));
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 从URL中获取流
     *
     * @param url {@link URL}
     * @return InputStream流
     */
    public static InputStream getStream(final URL url) {
        Assert.notNull(url, "URL must be not null");
        try {
            return url.openStream();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得Reader
     *
     * @param url     {@link URL}
     * @param charset 编码
     * @return {@link BufferedReader}
     */
    public static BufferedReader getReader(final URL url, final java.nio.charset.Charset charset) {
        return IoKit.toReader(getStream(url), charset);
    }

    /**
     * 标准化URL字符串，包括：
     *
     * <ol>
     * <li>自动补齐“http://”头</li>
     * <li>去除开头的\或者/</li>
     * <li>替换\为/</li>
     * </ol>
     *
     * @param url URL字符串
     * @return 标准化后的URL字符串
     */
    public static String normalize(final String url) {
        return normalize(url, false);
    }

    /**
     * 标准化URL字符串，包括：
     *
     * <ol>
     * <li>自动补齐“http://”头</li>
     * <li>去除开头的\或者/</li>
     * <li>替换\为/</li>
     * </ol>
     *
     * @param url          URL字符串
     * @param isEncodePath 是否对URL中path部分的中文和特殊字符做转义（不包括 http:, /和域名部分）
     * @return 标准化后的URL字符串
     */
    public static String normalize(final String url, final boolean isEncodePath) {
        return normalize(url, isEncodePath, false);
    }

    /**
     * 标准化URL字符串，包括：
     *
     * <ol>
     * <li>自动补齐“http://”头</li>
     * <li>去除开头的\或者/</li>
     * <li>替换\为/</li>
     * <li>如果replaceSlash为true，则替换多个/为一个</li>
     * </ol>
     *
     * @param url          URL字符串
     * @param isEncodePath 是否对URL中path部分的中文和特殊字符做转义（不包括 http:, /和域名部分）
     * @param replaceSlash 是否替换url body中的 //
     * @return 标准化后的URL字符串
     */
    public static String normalize(final String url, final boolean isEncodePath, final boolean replaceSlash) {
        if (StringKit.isBlank(url)) {
            return url;
        }
        final int sepIndex = url.indexOf("://");
        final String protocol;
        String body;
        if (sepIndex > 0) {
            protocol = StringKit.subPre(url, sepIndex + 3);
            body = StringKit.subSuf(url, sepIndex + 3);
        } else {
            protocol = "http://";
            body = url;
        }

        final int paramsSepIndex = StringKit.indexOf(body, Symbol.C_QUESTION_MARK);
        String params = null;
        if (paramsSepIndex > 0) {
            params = StringKit.subSuf(body, paramsSepIndex);
            body = StringKit.subPre(body, paramsSepIndex);
        }

        if (StringKit.isNotEmpty(body)) {
            // 去除开头的\或者/
            body = body.replaceAll("^[\\\\/]+", Normal.EMPTY);
            // 替换\为/
            body = body.replace("\\", "/");
            if (replaceSlash) {
                // 双斜杠在URL中是允许存在的，默认不做替换
                body = body.replaceAll("//+", "/");
            }
        }

        final int pathSepIndex = StringKit.indexOf(body, '/');
        String domain = body;
        String path = null;
        if (pathSepIndex > 0) {
            domain = StringKit.subPre(body, pathSepIndex);
            path = StringKit.subSuf(body, pathSepIndex);
        }
        if (isEncodePath) {
            path = RFC3986.PATH.encode(path, Charset.UTF_8);
        }
        return protocol + domain + StringKit.toStringOrEmpty(path) + StringKit.toStringOrEmpty(params);
    }

    /**
     * 将Map形式的Form表单数据转换为Url参数形式 paramMap中如果key为空（null和""）会被忽略，如果value为null，会被做为空白符（""） 会自动url编码键和值
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap 表单数据
     * @param charset  编码，编码为null表示不编码
     * @return url参数
     */
    public static String buildQuery(final Map<String, ?> paramMap, final java.nio.charset.Charset charset) {
        return UrlQuery.of(paramMap).build(charset);
    }

    /**
     * Data URI Scheme封装，数据格式为Base64。data URI scheme 允许我们使用内联（inline-code）的方式在网页中包含数据，
     * 目的是将一些小的数据，直接嵌入到网页中，从而不用再从外部文件载入。常用于将图片嵌入网页。 Data URI的格式规范：
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType 可选项（null表示无），数据类型（image/png、text/plain等）
     * @param data     编码后的数据
     * @return Data URI字符串
     */
    public static String getDataUriBase64(final String mimeType, final String data) {
        return getDataUri(mimeType, null, "base64", data);
    }

    /**
     * Data URI Scheme封装。data URI scheme 允许我们使用内联（inline-code）的方式在网页中包含数据， 目的是将一些小的数据，直接嵌入到网页中，从而不用再从外部文件载入。常用于将图片嵌入网页。
     * Data URI的格式规范：
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType 可选项（null表示无），数据类型（image/png、text/plain等）
     * @param encoding 数据编码方式（US-ASCII，BASE64等）
     * @param data     编码后的数据
     * @return Data URI字符串
     */
    public static String getDataUri(final String mimeType, final String encoding, final String data) {
        return getDataUri(mimeType, null, encoding, data);
    }

    /**
     * Data URI Scheme封装。data URI scheme 允许我们使用内联（inline-code）的方式在网页中包含数据， 目的是将一些小的数据，直接嵌入到网页中，从而不用再从外部文件载入。常用于将图片嵌入网页。
     * Data URI的格式规范：
     * 
     * <pre>
     *     data:[&lt;mime type&gt;][;charset=&lt;charset&gt;][;&lt;encoding&gt;],&lt;encoded data&gt;
     * </pre>
     *
     * @param mimeType 可选项（null表示无），数据类型（image/png、text/plain等）
     * @param charset  可选项（null表示无），源文本的字符集编码方式
     * @param encoding 数据编码方式（US-ASCII，BASE64等）
     * @param data     编码后的数据
     * @return Data URI字符串
     */
    public static String getDataUri(final String mimeType, final java.nio.charset.Charset charset,
            final String encoding, final String data) {
        final StringBuilder builder = StringKit.builder("data:");
        if (StringKit.isNotBlank(mimeType)) {
            builder.append(mimeType);
        }
        if (null != charset) {
            builder.append(";charset=").append(charset.name());
        }
        if (StringKit.isNotBlank(encoding)) {
            builder.append(Symbol.C_SEMICOLON).append(encoding);
        }
        builder.append(Symbol.C_COMMA).append(data);

        return builder.toString();
    }

    /**
     * 获取URL对应数据长度
     * <ul>
     * <li>如果URL为文件，转换为文件获取文件长度。</li>
     * <li>其它情况获取{@link URLConnection#getContentLengthLong()}</li>
     * </ul>
     *
     * @param url URL
     * @return 长度
     */
    public static long size(final URL url) {
        if (Normal.isFileOrVfsURL(url)) {
            // 如果资源以独立文件形式存在，尝试获取文件长度
            final File file = FileKit.file(url);
            final long length = file.length();
            if (length == 0L && !file.exists()) {
                throw new InternalException("File not exist or size is zero!");
            }
            return length;
        } else {
            // 如果资源打在jar包中或来自网络，使用网络请求长度
            // 来自Spring的AbstractFileResolvingResource
            URLConnection conn = null;
            try {
                conn = url.openConnection();
                useCachesIfNecessary(conn);
                if (conn instanceof HttpURLConnection) {
                    final HttpURLConnection httpCon = (HttpURLConnection) conn;
                    httpCon.setRequestMethod("HEAD");
                }
                return conn.getContentLengthLong();
            } catch (final IOException e) {
                throw new InternalException(e);
            } finally {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        }
    }

    /**
     * 如果连接为JNLP方式，则打开缓存
     *
     * @param con {@link URLConnection}
     */
    public static void useCachesIfNecessary(final URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

    /**
     * 将Map形式的Form表单数据转换为Url参数形式，会自动url编码键和值
     *
     * @param paramMap 表单数据
     * @return url参数
     */
    public static String toQuery(final Map<String, ?> paramMap) {
        return toQuery(paramMap, Charset.UTF_8);
    }

    /**
     * 将Map形式的Form表单数据转换为Url参数形式 paramMap中如果key为空（null和""）会被忽略，如果value为null，会被做为空白符（""） 会自动url编码键和值
     * 此方法用于拼接URL中的Query部分，并不适用于POST请求中的表单
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap 表单数据
     * @param charset  编码，{@code null} 表示不encode键值对
     * @return url参数
     */
    public static String toQuery(final Map<String, ?> paramMap, final java.nio.charset.Charset charset) {
        return toQuery(paramMap, charset, null);
    }

    /**
     * 将Map形式的Form表单数据转换为Url参数形式 paramMap中如果key为空（null和""）会被忽略，如果value为null，会被做为空白符（""） 会自动url编码键和值
     *
     * <pre>
     * key1=v1&amp;key2=&amp;key3=v3
     * </pre>
     *
     * @param paramMap   表单数据
     * @param charset    编码，null表示不encode键值对
     * @param encodeMode 编码模式
     * @return url参数
     */
    public static String toQuery(final Map<String, ?> paramMap, final java.nio.charset.Charset charset,
            final UrlQuery.EncodeMode encodeMode) {
        return UrlQuery.of(paramMap, encodeMode).build(charset);
    }

    /**
     * 对URL参数做编码，只编码键和值 提供的值可以是url附带参数，但是不能只是url
     *
     * <p>
     * 注意，此方法只能标准化整个URL，并不适合于单独编码参数值
     * </p>
     *
     * @param urlWithParams url和参数，可以包含url本身，也可以单独参数
     * @param charset       编码
     * @return 编码后的url和参数
     */
    public static String encodeQuery(final String urlWithParams, final java.nio.charset.Charset charset) {
        if (StringKit.isBlank(urlWithParams)) {
            return Normal.EMPTY;
        }

        String urlPart = null; // url部分，不包括问号
        String paramPart; // 参数部分
        final int pathEndPos = urlWithParams.indexOf(Symbol.C_QUESTION_MARK);
        if (pathEndPos > -1) {
            // url + 参数
            urlPart = StringKit.subPre(urlWithParams, pathEndPos);
            paramPart = StringKit.subSuf(urlWithParams, pathEndPos + 1);
            if (StringKit.isBlank(paramPart)) {
                // 无参数，返回url
                return urlPart;
            }
        } else if (!StringKit.contains(urlWithParams, Symbol.C_EQUAL)) {
            // 无参数的URL
            return urlWithParams;
        } else {
            // 无URL的参数
            paramPart = urlWithParams;
        }

        paramPart = normalizeQuery(paramPart, charset);

        return StringKit.isBlank(urlPart) ? paramPart : urlPart + "?" + paramPart;
    }

    /**
     * 标准化参数字符串，即URL中？后的部分
     *
     * <p>
     * 注意，此方法只能标准化整个URL，并不适合于单独编码参数值
     * </p>
     *
     * @param queryPart 参数字符串
     * @param charset   编码
     * @return 标准化的参数字符串
     */
    public static String normalizeQuery(final String queryPart, final java.nio.charset.Charset charset) {
        if (StringKit.isEmpty(queryPart)) {
            return queryPart;
        }
        final StringBuilder builder = new StringBuilder(queryPart.length() + 16);
        final int len = queryPart.length();
        String name = null;
        int pos = 0; // 未处理字符开始位置
        char c; // 当前字符
        int i; // 当前字符位置
        for (i = 0; i < len; i++) {
            c = queryPart.charAt(i);
            if (c == Symbol.C_EQUAL) { // 键值对的分界点
                if (null == name) {
                    // 只有=前未定义name时被当作键值分界符，否则做为普通字符
                    name = (pos == i) ? Normal.EMPTY : queryPart.substring(pos, i);
                    pos = i + 1;
                }
            } else if (c == Symbol.C_AND) { // 参数对的分界点
                if (pos != i) {
                    if (null == name) {
                        // 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
                        name = queryPart.substring(pos, i);
                        builder.append(RFC3986.QUERY_PARAM_NAME.encode(name, charset)).append(Symbol.C_EQUAL);
                    } else {
                        builder.append(RFC3986.QUERY_PARAM_NAME.encode(name, charset)).append(Symbol.C_EQUAL)
                                .append(RFC3986.QUERY_PARAM_VALUE.encode(queryPart.substring(pos, i), charset))
                                .append(Symbol.C_AND);
                    }
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 结尾处理
        if (null != name) {
            builder.append(UrlEncoder.encodeQuery(name, charset)).append(Symbol.C_EQUAL);
        }
        if (pos != i) {
            if (null == name && pos > 0) {
                builder.append(Symbol.C_EQUAL);
            }
            builder.append(UrlEncoder.encodeQuery(queryPart.substring(pos, i), charset));
        }

        // 以&结尾则去除之
        final int lastIndex = builder.length() - 1;
        if (Symbol.C_AND == builder.charAt(lastIndex)) {
            builder.delete(lastIndex, builder.length());
        }
        return builder.toString();
    }

    /**
     * 将URL参数解析为Map（也可以解析Post中的键值对参数）
     *
     * @param paramsStr 参数字符串（或者带参数的Path）
     * @param charset   字符集
     * @return 参数Map
     */
    public static Map<String, String> decodeQuery(final String paramsStr, final java.nio.charset.Charset charset) {
        final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(paramsStr, charset).getQueryMap();
        if (MapKit.isEmpty(queryMap)) {
            return MapKit.empty();
        }
        return Convert.toMap(String.class, String.class, queryMap);
    }

    /**
     * 将URL参数解析为Map（也可以解析Post中的键值对参数）
     *
     * @param paramsStr 参数字符串（或者带参数的Path）
     * @param charset   字符集
     * @return 参数Map
     */
    public static Map<String, List<String>> decodeQueryList(final String paramsStr,
            final java.nio.charset.Charset charset) {
        final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(paramsStr, charset).getQueryMap();
        if (MapKit.isEmpty(queryMap)) {
            return MapKit.empty();
        }

        final Map<String, List<String>> params = new LinkedHashMap<>();
        queryMap.forEach((key, value) -> {
            if (null != key && null != value) {
                final List<String> values = params.computeIfAbsent(key.toString(), k -> new ArrayList<>(1));
                // 一般是一个参数
                values.add(StringKit.toStringOrNull(value));
            }
        });
        return params;
    }

}
