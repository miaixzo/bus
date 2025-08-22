/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.wrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.EscapeKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求包装器，用于缓存请求体内容并防止XSS攻击。
 *
 * <p>
 * 该类继承自{@link HttpServletRequestWrapper}，主要功能包括：
 * </p>
 * <ul>
 * <li>缓存请求体内容，使得请求体可以被多次读取</li>
 * <li>对请求参数和请求头进行XSS过滤，防止跨站脚本攻击</li>
 * <li>记录请求参数日志，便于调试和问题排查</li>
 * </ul>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * // 在过滤器中使用
 * public class XSSFilter implements Filter {
 *
 *     &#64;Override
 *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 *             throws IOException, ServletException {
 *         // 包装请求
 *         CacheRequestWrapper wrappedRequest = new CacheRequestWrapper((HttpServletRequest) request);
 *         // 继续过滤器链
 *         chain.doFilter(wrappedRequest, response);
 *     }
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 缓存的请求体内容
     */
    private byte[] body;

    /**
     * 自定义的Servlet输入流包装器
     */
    private ServletInputStreamWrapper inputStreamWrapper;

    /**
     * 构造方法，初始化请求包装器。
     *
     * <p>
     * 该方法会读取并缓存请求体内容，初始化自定义的输入流包装器， 并记录请求参数日志。
     * </p>
     *
     * @param request 原始HTTP请求对象
     * @throws IOException 如果读取请求体时发生I/O错误
     */
    CacheRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 从InputStream获取参数，并保存以便多次获取
        this.body = IoKit.readBytes(request.getInputStream());
        if (this.body == null) {
            this.body = new byte[0]; // 防止空指针
        }

        // 记录请求参数日志
        Object logOut = MapKit.isNotEmpty(request.getParameterMap()) ? request.getParameterMap()
                : new String(this.body, Charset.UTF_8);
        if (logOut instanceof String) {
            // 移除换行符、制表符和多余空白
            logOut = ((String) logOut).replaceAll("\\s+", Normal.EMPTY);
        } else {
            logOut = JsonKit.toJsonString(logOut);
        }
        Logger.info("==> Parameters:{}", logOut);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                null != this.body ? this.body : Normal.EMPTY_BYTE_ARRAY);
        // 初始 ServletInputStreamWrapper
        this.inputStreamWrapper = new ServletInputStreamWrapper(byteArrayInputStream);
        // 设置 InputStream 到我们自己的包装类中
        this.inputStreamWrapper.setInputStream(byteArrayInputStream);
    }

    /**
     * 获取缓存的请求体内容。
     *
     * @return 请求体内容的字节数组
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * 获取自定义的Servlet输入流。
     *
     * @return 自定义的Servlet输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        return this.inputStreamWrapper;
    }

    /**
     * 获取请求体的BufferedReader。
     *
     * @return 请求体的BufferedReader
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.inputStreamWrapper, StandardCharsets.UTF_8));
    }

    /**
     * 获取指定参数名的所有参数值，并对非JSON格式的参数值进行XSS过滤。
     *
     * @param parameter 参数名
     * @return 过滤后的参数值数组
     */
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (null == values || values.length <= 0) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = values[i];
            if (!JsonKit.isJson(values[i])) {
                encodedValues[i] = EscapeKit.escapeHtml4(values[i]);
            }
        }
        return encodedValues;
    }

    /**
     * 获取指定参数名的参数值，并对非JSON格式的参数值进行XSS过滤。
     *
     * @param name 参数名
     * @return 过滤后的参数值
     */
    @Override
    public String getParameter(String name) {
        String content = super.getParameter(name);
        if (!JsonKit.isJson(content)) {
            content = EscapeKit.escapeHtml4(content);
        }
        return content;
    }

    /**
     * 获取指定请求头的值，并对非JSON格式的请求头值进行XSS过滤。
     *
     * @param name 请求头名
     * @return 过滤后的请求头值
     */
    @Override
    public String getHeader(String name) {
        String content = super.getHeader(name);
        if (!JsonKit.isJson(content)) {
            content = EscapeKit.escapeHtml4(content);
        }
        return content;
    }

    /**
     * 自定义的Servlet输入流包装器，用于支持请求体内容的多次读取。
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class ServletInputStreamWrapper extends ServletInputStream {

        /**
         * 输入流
         */
        private InputStream inputStream;

        /**
         * 判断输入流是否已经读取完毕。
         *
         * @return 如果输入流已经读取完毕则返回true，否则返回false
         */
        @Override
        public boolean isFinished() {
            return true;
        }

        /**
         * 判断输入流是否准备好被读取。
         *
         * @return 如果输入流准备好被读取则返回true，否则返回false
         */
        @Override
        public boolean isReady() {
            return false;
        }

        /**
         * 设置读取监听器。
         *
         * @param readListener 读取监听器
         */
        @Override
        public void setReadListener(ReadListener readListener) {
            // 空实现
        }

        /**
         * 从输入流中读取一个字节的数据。
         *
         * @return 读取的字节，如果到达流的末尾则返回-1
         * @throws IOException 如果发生I/O错误
         */
        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }
    }

}