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
package org.miaixz.bus.extra.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.core.beans.copier.CopyOptions;
import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.collection.ArrayIterator;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.map.CaseInsensitiveMap;
import org.miaixz.bus.core.toolkit.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Servlet 工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ServletKit {

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), ArrayKit.join(entry.getValue(), Symbol.COMMA));
        }
        return params;
    }

    /**
     * 获取请求体
     * 调用该方法后，getParam方法将失效
     *
     * @param request {@link ServletRequest}
     * @return 获得请求体
     */
    public static String getBody(ServletRequest request) {
        try (final BufferedReader reader = request.getReader()) {
            return IoKit.read(reader);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取请求体byte[]
     * 调用该方法后，getParam方法将失效
     *
     * @param request {@link ServletRequest}
     * @return 获得请求体byte[]
     */
    public static byte[] getBodyBytes(ServletRequest request) {
        try {
            return IoKit.readBytes(request.getInputStream());
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>         Bean类型
     * @param request     ServletRequest
     * @param bean        Bean
     * @param copyOptions 注入时的设置
     * @return the bean
     */
    public static <T> T fillBean(final ServletRequest request, T bean, CopyOptions copyOptions) {
        final String beanName = StringKit.lowerFirst(bean.getClass().getSimpleName());
        return BeanKit.fillBean(bean, new ValueProvider<String>() {
            @Override
            public Object value(String key, Type valueType) {
                String[] values = request.getParameterValues(key);
                if (ArrayKit.isEmpty(values)) {
                    values = request.getParameterValues(beanName + Symbol.DOT + key);
                    if (ArrayKit.isEmpty(values)) {
                        return null;
                    }
                }

                if (1 == values.length) {
                    // 单值表单直接返回这个值
                    return values[0];
                } else {
                    // 多值表单返回数组
                    return values;
                }
            }

            @Override
            public boolean containsKey(String key) {
                // 对于Servlet来说，返回值null意味着无此参数
                return (null != request.getParameter(key)) || (null != request.getParameter(beanName + Symbol.DOT + key));
            }
        }, copyOptions);
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>           Bean类型
     * @param request       {@link ServletRequest}
     * @param bean          Bean
     * @param isIgnoreError 是否忽略注入错误
     * @return the bean
     */
    public static <T> T fillBean(ServletRequest request, T bean, boolean isIgnoreError) {
        return fillBean(request, bean, CopyOptions.of().setIgnoreError(isIgnoreError));
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>           Bean类型
     * @param request       ServletRequest
     * @param beanClass     Bean Class
     * @param isIgnoreError 是否忽略注入错误
     * @return the bean
     */
    public static <T> T toBean(ServletRequest request, Class<T> beanClass, boolean isIgnoreError) {
        return fillBean(request, ReflectKit.newInstanceIfPossible(beanClass), isIgnoreError);
    }

    /**
     * 获取客户端IP
     *
     * <p>
     * 默认检测的Header:
     *
     * <pre>
     * 1、X-Forwarded-For
     * 2、X-Real-IP
     * 3、Proxy-Client-IP
     * 4、WL-Proxy-Client-IP
     * </pre>
     *
     * <p>
     * otherHeaderNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 其他自定义头文件，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIP(HttpServletRequest request, String... headerNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayKit.isNotEmpty(headerNames)) {
            headers = ArrayKit.addAll(headers, headerNames);
        }

        return getClientIPByHeader(request, headers);
    }

    /**
     * 获取客户端IP
     *
     * <p>
     * headerNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 自定义头，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (false == NetKit.isUnknown(ip)) {
                return NetKit.getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return NetKit.getMultistageReverseProxyIp(ip);
    }

    /**
     * 获取请求所有的头（header）信息
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return header值
     */
    public static Map<String, String> getHeader(HttpServletRequest request) {
        final Map<String, String> headerMap = new HashMap<>();

        final Enumeration<String> names = request.getHeaderNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            headerMap.put(name, request.getHeader(name));
        }

        return headerMap;
    }

    /**
     * 忽略大小写获得请求header中的信息
     *
     * @param request        请求对象{@link HttpServletRequest}
     * @param nameIgnoreCase 忽略大小写头信息的KEY
     * @return header值
     */
    public static String getHeader(HttpServletRequest request, String nameIgnoreCase) {
        final Enumeration<String> names = request.getHeaderNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            if (null != name && name.equalsIgnoreCase(nameIgnoreCase)) {
                return request.getHeader(name);
            }
        }

        return null;
    }

    /**
     * 获得请求header中的信息
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param name        头信息的KEY
     * @param charsetName 字符集
     * @return header值
     */
    public static String getHeader(HttpServletRequest request, String name, String charsetName) {
        return getHeader(request, name, Charset.charset(charsetName));
    }

    /**
     * 获得请求header中的信息
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @param name    头信息的KEY
     * @param charset 字符集
     * @return header值
     */
    public static String getHeader(HttpServletRequest request, String name, java.nio.charset.Charset charset) {
        final String header = request.getHeader(name);
        if (null != header) {
            return Charset.convert(header, Charset.ISO_8859_1, charset);
        }
        return null;
    }

    /**
     * 设置响应的Header
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param name     名
     * @param value    值，可以是String，Date， int
     */
    public static void setHeader(HttpServletResponse response, String name, Object value) {
        if (value instanceof String) {
            response.setHeader(name, (String) value);
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            response.setDateHeader(name, ((Date) value).getTime());
        } else if (value instanceof Integer || "int".equals(value.getClass().getSimpleName().toLowerCase())) {
            response.setIntHeader(name, (int) value);
        } else {
            response.setHeader(name, value.toString());
        }
    }

    /**
     * 客户浏览器是否为IE
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 客户浏览器是否为IE
     */
    public static boolean isIE(HttpServletRequest request) {
        String userAgent = getHeader(request, Header.USER_AGENT);
        if (StringKit.isNotBlank(userAgent)) {
            userAgent = userAgent.toUpperCase();
            return userAgent.contains("MSIE") || userAgent.contains("TRIDENT");
        }
        return false;
    }

    /**
     * 是否为GET请求
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为GET请求
     */
    public static boolean isGetMethod(HttpServletRequest request) {
        return Http.GET.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否为POST请求
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为POST请求
     */
    public static boolean isPostMethod(HttpServletRequest request) {
        return Http.POST.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否为Multipart类型表单，此类型表单用于文件上传
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为Multipart类型表单，此类型表单用于文件上传
     */
    public static boolean isMultipart(HttpServletRequest request) {
        if (false == isPostMethod(request)) {
            return false;
        }

        String contentType = request.getContentType();
        if (StringKit.isBlank(contentType)) {
            return false;
        }
        return contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return Cookie map
     */
    public static Map<String, Cookie> getCookie(HttpServletRequest httpServletRequest) {
        final Cookie[] cookies = httpServletRequest.getCookies();
        if (ArrayKit.isEmpty(cookies)) {
            return MapKit.empty();
        }

        return IterKit.toMap(
                new ArrayIterator<>(httpServletRequest.getCookies()),
                new CaseInsensitiveMap<>(),
                Cookie::getName);
    }

    /**
     * 获得指定的Cookie
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @param name               cookie名称
     * @return Cookie对象
     */
    public static Cookie getCookie(HttpServletRequest httpServletRequest, String name) {
        return getCookie(httpServletRequest).get(name);
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param cookie   Servlet Cookie对象
     */
    public static void addCookie(HttpServletResponse response, Cookie cookie) {
        response.addCookie(cookie);
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param name     Cookie名
     * @param value    Cookie值
     */
    public static void addCookie(HttpServletResponse response, String name, String value) {
        response.addCookie(new Cookie(name, value));
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. &gt;0 : Cookie存在的秒数.
     * @param path            Cookie的有效路径
     * @param domain          the domain name within which this cookie is visible; form is according to RFC 2109
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds, String path, String domain) {
        Cookie cookie = new Cookie(name, value);
        if (null != domain) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAgeInSeconds);
        cookie.setPath(path);
        addCookie(response, cookie);
    }

    /**
     * 设定返回给客户端的Cookie
     * Path: "/"
     * No Domain
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. &gt;0 : Cookie存在的秒数.
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        addCookie(response, name, value, maxAgeInSeconds, Symbol.SLASH, null);
    }

    /**
     * 获得PrintWriter
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @return 获得PrintWriter
     * @throws InternalException IO异常
     */
    public static PrintWriter getWriter(HttpServletResponse response) throws InternalException {
        try {
            return response.getWriter();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param text        返回的内容
     * @param contentType 返回的类型
     */
    public static void write(HttpServletResponse response, String text, String contentType) {
        response.setContentType(contentType);
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(writer);
        }
    }

    /**
     * 返回文件给客户端
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param file     写出的文件对象
     */
    public static void write(HttpServletResponse response, File file) {
        final String fileName = file.getName();
        final String contentType = ObjectKit.defaultIfNull(FileKit.getMediaType(fileName), MediaType.APPLICATION_OCTET_STREAM);
        BufferedInputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            write(response, in, contentType, fileName);
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param in          需要返回客户端的内容
     * @param contentType 返回的类型
     * @param fileName    文件名
     */
    public static void write(HttpServletResponse response, InputStream in, String contentType, String fileName) {
        final String charset = ObjectKit.defaultIfNull(response.getCharacterEncoding(), Charset.DEFAULT_UTF_8);
        final String encodeText = UriKit.encodeAll(fileName, Charset.charset(charset));
        response.setHeader("Content-Disposition",
                StringKit.format("attachment;filename=\"{}\";filename*={}''{}", encodeText, charset, encodeText));
        response.setContentType(contentType);
        write(response, in);
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param in          需要返回客户端的内容
     * @param contentType 返回的类型
     */
    public static void write(HttpServletResponse response, InputStream in, String contentType) {
        response.setContentType(contentType);
        write(response, in);
    }

    /**
     * 返回数据给客户端
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param in       需要返回客户端的内容
     */
    public static void write(HttpServletResponse response, InputStream in) {
        write(response, in, IoKit.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 返回数据给客户端
     *
     * @param response   响应对象{@link HttpServletResponse}
     * @param in         需要返回客户端的内容
     * @param bufferSize 缓存大小
     */
    public static void write(HttpServletResponse response, InputStream in, int bufferSize) {
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            IoKit.copy(in, out, bufferSize);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(out);
            IoKit.close(in);
        }
    }

}
