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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.FileType;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarFile;

/**
 * 统一资源定位符相关工具类
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public class UriUtils {

    /**
     * 通过一个字符串形式的URL地址创建URL对象
     *
     * @param url URL
     * @return URL对象
     */
    public static URL url(String url) {
        return url(url, null);
    }

    /**
     * 通过一个字符串形式的URL地址创建URL对象
     *
     * @param url     URL
     * @param handler {@link URLStreamHandler}
     * @return URL对象
     */
    public static URL url(String url, URLStreamHandler handler) {
        Assert.notNull(url, "URL must not be null");

        // 兼容Spring的ClassPath路径
        if (url.startsWith(Normal.CLASSPATH_URL_PREFIX)) {
            url = url.substring(Normal.CLASSPATH_URL_PREFIX.length());
            return ClassUtils.getClassLoader().getResource(url);
        }

        try {
            return new URL(null, url, handler);
        } catch (MalformedURLException e) {
            // 尝试文件路径
            try {
                return new File(url).toURI().toURL();
            } catch (MalformedURLException ex2) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 将URL字符串转换为URL对象，并做必要验证
     *
     * @param urlStr URL字符串
     * @return URL
     */
    public static URL toUrlForHttp(String urlStr) {
        return toUrlForHttp(urlStr, null);
    }

    /**
     * 将URL字符串转换为URL对象，并做必要验证
     *
     * @param urlStr  URL字符串
     * @param handler {@link URLStreamHandler}
     * @return URL
     */
    public static URL toUrlForHttp(String urlStr, URLStreamHandler handler) {
        Assert.notBlank(urlStr, "Url is blank !");
        // 去掉url中的空白符，防止空白符导致的异常
        urlStr = StringUtils.cleanBlank(urlStr);
        return UriUtils.url(urlStr, handler);
    }

    /**
     * 获得URL
     *
     * @param pathBaseClassLoader 相对路径（相对于classes）
     * @return URL
     * @see ResourceUtils#getResource(String)
     */
    public static URL getURL(String pathBaseClassLoader) {
        return ResourceUtils.getResource(pathBaseClassLoader);
    }

    /**
     * 获得URL
     *
     * @param path  相对给定 class所在的路径
     * @param clazz 指定class
     * @return URL
     * @see ResourceUtils#getResource(String, Class)
     */
    public static URL getURL(String path, Class<?> clazz) {
        return ResourceUtils.getResource(path, clazz);
    }

    /**
     * 获得URL，常用于使用绝对路径时的情况
     *
     * @param file URL对应的文件对象
     * @return URL
     * @throws InstrumentException MalformedURLException
     */
    public static URL getURL(File file) {
        Assert.notNull(file, "File is null !");
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InstrumentException("Error occured when get URL!");
        }
    }

    /**
     * 获得URL，常用于使用绝对路径时的情况
     *
     * @param files URL对应的文件对象
     * @return URL
     * @throws InstrumentException MalformedURLException
     */
    public static URL[] getURLs(File... files) {
        final URL[] urls = new URL[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        } catch (MalformedURLException e) {
            throw new InstrumentException("Error occured when get URL!");
        }

        return urls;
    }

    /**
     * 格式化URL链接
     *
     * @param url 需要格式化的URL
     * @return 格式化后的URL，如果提供了null或者空串，返回null
     * @see #normalize(String)
     */
    public static String formatUrl(String url) {
        return normalize(url);
    }

    /**
     * 补全相对路径
     *
     * @param baseUrl      基准URL
     * @param relativePath 相对URL
     * @return 相对路径
     * @throws InstrumentException MalformedURLException
     */
    public static String complateUrl(String baseUrl, String relativePath) {
        baseUrl = formatUrl(baseUrl);
        if (StringUtils.isBlank(baseUrl)) {
            return null;
        }

        try {
            final URL absoluteUrl = new URL(baseUrl);
            final URL parseUrl = new URL(absoluteUrl, relativePath);
            return parseUrl.toString();
        } catch (MalformedURLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 编码URL，默认使用UTF-8编码
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
     *
     * @param url URL
     * @return 编码后的URL
     * @throws InstrumentException UnsupportedEncodingException
     * @since 3.1.9
     */
    public static String encode(String url) throws InstrumentException {
        return encode(url, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 编码URL，默认使用UTF-8编码
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
     *
     * @param url URL
     * @return 编码后的URL
     */
    public static String encodeAll(String url) {
        return encodeAll(url, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 编码URL
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
     *
     * @param url     URL
     * @param charset 编码
     * @return 编码后的URL
     */
    public static String encodeAll(String url, Charset charset) {
        try {
            return java.net.URLEncoder.encode(url, charset.toString());
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得path部分
     *
     * @param uriStr URI路径
     * @return path
     * @throws InstrumentException 包装URISyntaxException
     */
    public static String getPath(String uriStr) {
        URI uri;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new InstrumentException(e);
        }
        return uri.getPath();
    }

    /**
     * 从URL对象中获取不被编码的路径Path
     * 对于本地路径，URL对象的getPath方法对于包含中文或空格时会被编码，导致本读路径读取错误。
     * 此方法将URL转为URI后获取路径用于解决路径被编码的问题
     *
     * @param url {@link URL}
     * @return 路径
     */
    public static String getDecodedPath(URL url) {
        if (null == url) {
            return null;
        }

        String path = null;
        try {
            // URL对象的getPath方法对于包含中文或空格的问题
            path = UriUtils.toURI(url).getPath();
        } catch (InstrumentException e) {
            // ignore
        }
        return (null != path) ? path : url.getPath();
    }

    /**
     * 转URL为URI
     *
     * @param url URL
     * @return URI
     * @throws InstrumentException 包装URISyntaxException
     */
    public static URI toURI(URL url) throws InstrumentException {
        if (null == url) {
            return null;
        }
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转字符串为URI
     *
     * @param location 字符串路径
     * @return URI
     * @throws InstrumentException 包装URISyntaxException
     */
    public static URI toURI(String location) throws InstrumentException {
        try {
            return new URI(location.replace(" ", "%20"));
        } catch (URISyntaxException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 提供的URL是否为文件
     * 文件协议包括"file", "vfsfile" 或 "vfs".
     *
     * @param url {@link URL}
     * @return 是否为文件
     * @since 3.1.9
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return (Normal.URL_PROTOCOL_FILE.equals(protocol) || //
                Normal.URL_PROTOCOL_VFSFILE.equals(protocol) || //
                Normal.URL_PROTOCOL_VFS.equals(protocol));
    }

    /**
     * 提供的URL是否为jar包URL 协议包括： "jar", "zip", "vfszip" 或 "wsjar".
     *
     * @param url {@link URL}
     * @return 是否为jar包URL
     */
    public static boolean isJarURL(URL url) {
        final String protocol = url.getProtocol();
        return (Normal.URL_PROTOCOL_JAR.equals(protocol) || //
                Normal.URL_PROTOCOL_ZIP.equals(protocol) || //
                Normal.URL_PROTOCOL_VFSZIP.equals(protocol) || //
                Normal.URL_PROTOCOL_WSJAR.equals(protocol));
    }

    /**
     * 提供的URL是否为Jar文件URL 判断依据为file协议且扩展名为.jar
     *
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR file URL
     */
    public static boolean isJarFileURL(URL url) {
        return (Normal.URL_PROTOCOL_FILE.equals(url.getProtocol()) && //
                url.getPath().toLowerCase().endsWith(FileType.JAR));
    }

    /**
     * 从URL中获取流
     *
     * @param url {@link URL}
     * @return InputStream流
     * @since 5.0.0
     */
    public static InputStream getStream(URL url) {
        Assert.notNull(url);
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得Reader
     *
     * @param url     {@link URL}
     * @param charset 编码
     * @return {@link BufferedReader}
     * @since 5.0.0
     */
    public static BufferedReader getReader(URL url, Charset charset) {
        return IoUtils.getReader(getStream(url), charset);
    }

    /**
     * 从URL中获取JarFile
     *
     * @param url URL
     * @return JarFile
     */
    public static JarFile getJarFile(URL url) {
        try {
            JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
            return urlConnection.getJarFile();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 标准化URL字符串，包括：
     * <pre>
     * 1. 多个/替换为一个
     * </pre>
     *
     * @param url URL字符串
     * @return 标准化后的URL字符串
     */
    public static String normalize(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        final int sepIndex = url.indexOf("://");
        String pre;
        String body;
        if (sepIndex > 0) {
            pre = StringUtils.subPre(url, sepIndex + 3);
            body = StringUtils.subSuf(url, sepIndex + 3);
        } else {
            pre = "http://";
            body = url;
        }

        int paramsSepIndex = url.indexOf("?");
        String params = null;
        if (paramsSepIndex > 0) {
            params = StringUtils.subSuf(body, paramsSepIndex);
            body = StringUtils.subPre(body, paramsSepIndex);
        }

        //去除开头的\或者/
        body = body.replaceAll("^[\\/]+", Normal.EMPTY);
        //替换多个\或/为单个/
        body = body.replace("\\", "/").replaceAll("//+", "/");
        return pre + body + StringUtils.nullToEmpty(params);
    }

    /**
     * Encode the given URI scheme with the given encoding.
     *
     * @param scheme   the scheme to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded scheme
     */
    public static String encodeScheme(String scheme, String encoding) {
        return encode(scheme, encoding, Type.SCHEME);
    }

    /**
     * Encode the given URI scheme with the given encoding.
     *
     * @param scheme  the scheme to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded scheme
     * @since 5.0
     */
    public static String encodeScheme(String scheme, Charset charset) {
        return encode(scheme, charset, Type.SCHEME);
    }

    /**
     * Encode the given URI authority with the given encoding.
     *
     * @param authority the authority to be encoded
     * @param encoding  the character encoding to encode to
     * @return the encoded authority
     */
    public static String encodeAuthority(String authority, String encoding) {
        return encode(authority, encoding, Type.AUTHORITY);
    }

    /**
     * Encode the given URI authority with the given encoding.
     *
     * @param authority the authority to be encoded
     * @param charset   the character encoding to encode to
     * @return the encoded authority
     * @since 5.0
     */
    public static String encodeAuthority(String authority, Charset charset) {
        return encode(authority, charset, Type.AUTHORITY);
    }

    /**
     * Encode the given URI user info with the given encoding.
     *
     * @param userInfo the user info to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded user info
     */
    public static String encodeUserInfo(String userInfo, String encoding) {
        return encode(userInfo, encoding, Type.USER_INFO);
    }

    /**
     * Encode the given URI user info with the given encoding.
     *
     * @param userInfo the user info to be encoded
     * @param charset  the character encoding to encode to
     * @return the encoded user info
     * @since 5.0
     */
    public static String encodeUserInfo(String userInfo, Charset charset) {
        return encode(userInfo, charset, Type.USER_INFO);
    }

    /**
     * Encode the given URI host with the given encoding.
     *
     * @param host     the host to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded host
     */
    public static String encodeHost(String host, String encoding) {
        return encode(host, encoding, Type.HOST_IPV4);
    }

    /**
     * Encode the given URI host with the given encoding.
     *
     * @param host    the host to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded host
     * @since 5.0
     */
    public static String encodeHost(String host, Charset charset) {
        return encode(host, charset, Type.HOST_IPV4);
    }

    /**
     * Encode the given URI port with the given encoding.
     *
     * @param port     the port to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded port
     */
    public static String encodePort(String port, String encoding) {
        return encode(port, encoding, Type.PORT);
    }

    /**
     * Encode the given URI port with the given encoding.
     *
     * @param port    the port to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded port
     * @since 5.0
     */
    public static String encodePort(String port, Charset charset) {
        return encode(port, charset, Type.PORT);
    }

    /**
     * Encode the given URI path with the given encoding.
     *
     * @param path     the path to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded path
     */
    public static String encodePath(String path, String encoding) {
        return encode(path, encoding, Type.PATH);
    }

    /**
     * Encode the given URI path with the given encoding.
     *
     * @param path    the path to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded path
     * @since 5.0
     */
    public static String encodePath(String path, Charset charset) {
        return encode(path, charset, Type.PATH);
    }

    /**
     * Encode the given URI path segment with the given encoding.
     *
     * @param segment  the segment to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded segment
     */
    public static String encodePathSegment(String segment, String encoding) {
        return encode(segment, encoding, Type.PATH_SEGMENT);
    }

    /**
     * Encode the given URI path segment with the given encoding.
     *
     * @param segment the segment to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded segment
     * @since 5.0
     */
    public static String encodePathSegment(String segment, Charset charset) {
        return encode(segment, charset, Type.PATH_SEGMENT);
    }

    /**
     * Encode the given URI query with the given encoding.
     *
     * @param query    the query to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded query
     */
    public static String encodeQuery(String query, String encoding) {
        return encode(query, encoding, Type.QUERY);
    }

    /**
     * Encode the given URI query with the given encoding.
     *
     * @param query   the query to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded query
     * @since 5.0
     */
    public static String encodeQuery(String query, Charset charset) {
        return encode(query, charset, Type.QUERY);
    }

    /**
     * Encode the given URI query parameter with the given encoding.
     *
     * @param queryParam the query parameter to be encoded
     * @param encoding   the character encoding to encode to
     * @return the encoded query parameter
     */
    public static String encodeQueryParam(String queryParam, String encoding) {

        return encode(queryParam, encoding, Type.QUERY_PARAM);
    }

    /**
     * Encode the given URI query parameter with the given encoding.
     *
     * @param queryParam the query parameter to be encoded
     * @param charset    the character encoding to encode to
     * @return the encoded query parameter
     * @since 5.0
     */
    public static String encodeQueryParam(String queryParam, Charset charset) {
        return encode(queryParam, charset, Type.QUERY_PARAM);
    }

    /**
     * Encode the given URI fragment with the given encoding.
     *
     * @param fragment the fragment to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded fragment
     */
    public static String encodeFragment(String fragment, String encoding) {
        return encode(fragment, encoding, Type.FRAGMENT);
    }

    /**
     * Encode the given URI fragment with the given encoding.
     *
     * @param fragment the fragment to be encoded
     * @param charset  the character encoding to encode to
     * @return the encoded fragment
     * @since 5.0
     */
    public static String encodeFragment(String fragment, Charset charset) {
        return encode(fragment, charset, Type.FRAGMENT);
    }

    /**
     * Variant of {@link #decode(String, Charset)} with a String charset.
     *
     * @param source   the String to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded String
     */
    public static String encode(String source, String encoding) {
        return encode(source, encoding, Type.URI);
    }

    /**
     * Encode all characters that are either illegal, or have any reserved
     * meaning, anywhere within a URI, as defined in
     * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>.
     * This is useful to ensure that the given String will be preserved as-is
     * and will not have any o impact on the structure or meaning of the URI.
     *
     * @param source  the String to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded String
     * @since 5.0
     */
    public static String encode(String source, Charset charset) {
        return encode(source, charset, Type.URI);
    }

    /**
     * Convenience method to apply {@link #encode(String, Charset)} to all
     * given URI variable values.
     *
     * @param uriVariables the URI variable values to be encoded
     * @return the encoded String
     * @since 5.0
     */
    public static Map<String, String> encodeUriVariables(Map<String, ?> uriVariables) {
        Map<String, String> result = new LinkedHashMap<String, String>(uriVariables.size());
        for (Map.Entry<String, ?> entry : uriVariables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String stringValue = (value != null ? value.toString() : "");
            result.put(key, encode(stringValue, Charset.forName("UTF-8")));
        }
        return result;
    }

    /**
     * Convenience method to apply {@link #encode(String, Charset)} to all
     * given URI variable values.
     *
     * @param uriVariables the URI variable values to be encoded
     * @return the encoded String
     * @since 5.0
     */
    public static Object[] encodeUriVariables(Object... uriVariables) {
        List<String> result = new ArrayList<String>();
        for (Object value : uriVariables) {
            String stringValue = (value != null ? value.toString() : "");
            result.add(encode(stringValue, Charset.forName("UTF-8")));
        }
        return result.toArray();
    }

    private static String encode(String scheme, String encoding, Type type) {
        return encodeUriComponent(scheme, encoding, type);
    }

    private static String encode(String scheme, Charset charset, Type type) {
        return encodeUriComponent(scheme, charset, type);
    }

    /**
     * Encode the given source into an encoded String using the rules specified
     * by the given component and with the given options.
     *
     * @param source   the source String
     * @param encoding the encoding of the source String
     * @param type     the URI component for the source
     * @return the encoded URI
     * @throws IllegalArgumentException when the given value is not a valid URI component
     */
    static String encodeUriComponent(String source, String encoding, Type type) {
        return encodeUriComponent(source, Charset.forName(encoding), type);
    }

    /**
     * Encode the given source into an encoded String using the rules specified
     * by the given component and with the given options.
     *
     * @param source  the source String
     * @param charset the encoding of the source String
     * @param type    the URI component for the source
     * @return the encoded URI
     * @throws IllegalArgumentException when the given value is not a valid URI component
     */
    static String encodeUriComponent(String source, Charset charset, Type type) {
        if (!(source != null && source.length() > 0)) {
            return source;
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }

        byte[] bytes;
        try {
            bytes = source.getBytes(charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
        boolean changed = false;
        for (byte b : bytes) {
            if (b < 0) {
                b += 256;
            }
            if (type.isAllowed(b)) {
                bos.write(b);
            } else {
                bos.write('%');
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                bos.write(hex1);
                bos.write(hex2);
                changed = true;
            }
        }
        try {
            return (changed ? new String(bos.toByteArray(), charset.name()) : source);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String uriDecode(String source, Charset charset) {
        int length = source.length();
        if (length == 0) {
            return source;
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int ch = source.charAt(i);
            if (ch == '%') {
                if (i + 2 < length) {
                    char hex1 = source.charAt(i + 1);
                    char hex2 = source.charAt(i + 2);
                    int u = Character.digit(hex1, 16);
                    int l = Character.digit(hex2, 16);
                    if (u == -1 || l == -1) {
                        throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                    }
                    bos.write((char) ((u << 4) + l));
                    i += 2;
                    changed = true;
                } else {
                    throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                }
            } else {
                bos.write(ch);
            }
        }
        try {
            return (changed ? new String(bos.toByteArray(), charset.name()) : source);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Decode the given encoded URI component.
     * <p>See {@link #uriDecode(String, Charset)} for the decoding rules.
     *
     * @param source   the encoded String
     * @param encoding the character encoding to use
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see #uriDecode(String, Charset)
     * @see java.net.URLDecoder#decode(String, String)
     */
    public static String decode(String source, String encoding) {
        return uriDecode(source, Charset.forName(encoding));
    }

    /**
     * Decode the given encoded URI component.
     * <p>See {@link #uriDecode(String, Charset)} for the decoding rules.
     *
     * @param source  the encoded String
     * @param charset the character encoding to use
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see #uriDecode(String, Charset)
     * @see java.net.URLDecoder#decode(String, String)
     * @since 5.0
     */
    public static String decode(String source, Charset charset) {
        return uriDecode(source, charset);
    }

    /**
     * Extract the file extension from the given URI path.
     *
     * @param path the URI path (e.g. "/products/index.html")
     * @return the extracted file extension (e.g. "html")
     */

    public static String extractFileExtension(String path) {
        int end = path.indexOf('?');
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex != -1 && (end == -1 || fragmentIndex < end)) {
            end = fragmentIndex;
        }
        if (end == -1) {
            end = path.length();
        }
        int begin = path.lastIndexOf('/', end) + 1;
        int paramIndex = path.indexOf(';', begin);
        end = (paramIndex != -1 && paramIndex < end ? paramIndex : end);
        int extIndex = path.lastIndexOf('.', end);
        if (extIndex != -1 && extIndex > begin) {
            return path.substring(extIndex + 1, end);
        }
        return null;
    }

    /**
     * Enumeration used to identify the allowed characters per URI component.
     * <p>Contains methods to indicate whether a given character is valid in a specific URI component.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
     */
    public enum Type {

        SCHEME {
            @Override
            public boolean isAllowed(int c) {
                return isAlpha(c) || isDigit(c) || '+' == c || '-' == c || '.' == c;
            }
        },
        AUTHORITY {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c;
            }
        },
        USER_INFO {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || ':' == c;
            }
        },
        HOST_IPV4 {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c);
            }
        },
        HOST_IPV6 {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || '[' == c || ']' == c || ':' == c;
            }
        },
        PORT {
            @Override
            public boolean isAllowed(int c) {
                return isDigit(c);
            }
        },
        PATH {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c;
            }
        },
        PATH_SEGMENT {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c);
            }
        },
        QUERY {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }
        },
        QUERY_PARAM {
            @Override
            public boolean isAllowed(int c) {
                if ('=' == c || '&' == c) {
                    return false;
                } else {
                    return isPchar(c) || '/' == c || '?' == c;
                }
            }
        },
        FRAGMENT {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }
        },
        URI {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c);
            }
        };

        public abstract boolean isAllowed(int c);

        protected boolean isAlpha(int c) {
            return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
        }

        protected boolean isDigit(int c) {
            return (c >= '0' && c <= '9');
        }

        protected boolean isGenericDelimiter(int c) {
            return (':' == c || '/' == c || '?' == c || '#' == c || '[' == c || ']' == c || '@' == c);
        }

        protected boolean isSubDelimiter(int c) {
            return ('!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || '*' == c || '+' == c ||
                    ',' == c || ';' == c || '=' == c);
        }

        protected boolean isReserved(int c) {
            return (isGenericDelimiter(c) || isSubDelimiter(c));
        }

        protected boolean isUnreserved(int c) {
            return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
        }

        protected boolean isPchar(int c) {
            return (isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c);
        }
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
    public static String encodeVal(String paramsStr, Charset charset) {
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

        paramPart = normalize(paramPart, charset);

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
    public static String normalize(String paramPart, Charset charset) {
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
    public static String decodeMap(Map<String, ?> paramMap, Charset charset) {
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
    public static Map<String, String> decodeVal(String params, String charset) {
        final Map<String, List<String>> paramsMap = decodeObj(params, charset);
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
    public static Map<String, List<String>> decodeObj(String params, String charset) {
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
    public static String withForm(String url, Map<String, Object> form, Charset charset, boolean isEncodeParams) {
        if (isEncodeParams && StringUtils.contains(url, '?')) {
            url = encodeVal(url, charset);
        }

        return withForm(url, decodeMap(form, charset), charset, false);
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
    public static String withForm(String url, String queryString, Charset charset, boolean isEncode) {
        if (StringUtils.isBlank(queryString)) {
            if (StringUtils.contains(url, '?')) {
                return isEncode ? encodeVal(url, charset) : url;
            }
            return url;
        }

        final TextUtils textUtils = TextUtils.create(url.length() + queryString.length() + 16);
        int qmIndex = url.indexOf('?');
        if (qmIndex > 0) {
            textUtils.append(isEncode ? encodeVal(url, charset) : url);
            if (false == StringUtils.endWith(url, '&')) {
                textUtils.append('&');
            }
        } else {
            textUtils.append(url);
            if (qmIndex < 0) {
                textUtils.append('?');
            }
        }
        textUtils.append(isEncode ? encodeVal(queryString, charset) : queryString);
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

}
