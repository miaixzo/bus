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
package org.miaixz.bus.spring.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.miaixz.bus.core.lang.MediaType;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * 可重复读取响应内容的包装器，支持缓存响应内容，便于日志记录和后续处理（不缓存SSE）。
 *
 * <p>
 * 该类继承自{@link HttpServletResponseWrapper}，主要功能包括：
 * </p>
 * <ul>
 * <li>缓存响应内容，使得响应内容可以被多次读取</li>
 * <li>自动识别并处理流式响应（如SSE），对流式响应不进行缓存</li>
 * <li>提供获取响应内容的方法，便于日志记录和后续处理</li>
 * </ul>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * // 在过滤器中使用
 * public class ResponseCacheFilter implements Filter {
 *
 *     &#64;Override
 *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 *             throws IOException, ServletException {
 *         // 包装响应
 *         CacheResponseWrapper wrappedResponse = new CacheResponseWrapper((HttpServletResponse) response);
 *         // 继续过滤器链
 *         chain.doFilter(request, wrappedResponse);
 *         // 获取响应内容
 *         byte[] responseBody = wrappedResponse.getBody();
 *         // 记录日志或进行其他处理
 *         logResponse(responseBody);
 *     }
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableResponseWrapper extends HttpServletResponseWrapper {

    /**
     * 字节数组输出流，用于缓存响应内容
     */
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    /**
     * 打印写入器，用于缓存响应内容
     */
    private PrintWriter writer = new PrintWriter(byteArrayOutputStream);

    /**
     * 是否为流式响应的标志
     */
    private boolean isStreaming = false;

    /**
     * 构造方法，初始化响应包装器。
     *
     * @param response 原始HTTP响应对象
     */
    public MutableResponseWrapper(HttpServletResponse response) {
        super(response);
        this.streaming();
    }

    /**
     * 获取打印写入器，用于写入响应内容。
     *
     * <p>
     * 对于流式响应（如SSE），直接返回原始响应的打印写入器，不做额外处理； 对于非流式响应，返回一个包装后的打印写入器，可以同时写入原始响应和缓存。
     * </p>
     *
     * @return 打印写入器
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        this.streaming();
        if (isStreaming) {
            // 对于 SSE 流式响应，直接返回原始响应写入器，不做额外处理
            return super.getWriter();
        }
        return new ServletPrintWriter(super.getWriter(), writer);
    }

    /**
     * 设置响应的内容类型。
     *
     * <p>
     * 该方法在设置内容类型的同时，会根据内容类型判断是否为流式响应（如SSE）。
     * </p>
     *
     * @param type 内容类型
     */
    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        // 根据 Content-Type 判断是否为流式响应
        if (type != null) {
            String lowerType = type.toLowerCase();
            isStreaming = lowerType.contains(MediaType.SERVER_SENT_EVENTS);
        }
    }

    /**
     * 获取Servlet输出流，用于写入响应内容。
     *
     * <p>
     * 对于流式响应（如SSE），直接返回原始响应的输出流，不做额外处理； 对于非流式响应，返回一个包装后的输出流，可以同时写入原始响应和缓存。
     * </p>
     *
     * @return Servlet输出流
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        this.streaming();
        // 对于 SSE 流式响应，直接返回原始响应流，不做额外处理
        if (isStreaming) {
            return super.getOutputStream();
        }
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // 空实现
            }

            @Override
            public void write(int b) throws IOException {
                TeeOutputStream write = new TeeOutputStream(MutableResponseWrapper.super.getOutputStream(),
                        byteArrayOutputStream);
                write.write(b);
            }
        };
    }

    /**
     * 获取缓存的响应内容。
     *
     * @return 响应内容的字节数组
     */
    public byte[] getBody() {
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 检查并设置是否为流式响应。
     *
     * <p>
     * 该方法会根据当前响应的内容类型判断是否为流式响应（如SSE）， 并设置{@link #isStreaming}标志。
     * </p>
     */
    public void streaming() {
        String contentType = getContentType();
        if (contentType != null) {
            String lowerType = contentType.toLowerCase();
            isStreaming = lowerType.contains(MediaType.SERVER_SENT_EVENTS);
        }
    }

    /**
     * 判断是否为流式响应。
     *
     * @return 如果为流式响应则返回true，否则返回false
     */
    public boolean isStreaming() {
        return isStreaming;
    }

    /**
     * 自定义的打印写入器，用于同时写入原始响应和缓存。
     */
    private static class ServletPrintWriter extends PrintWriter {

        /**
         * 缓存的打印写入器
         */
        PrintWriter printWriter;

        /**
         * 构造方法，初始化打印写入器。
         *
         * @param main        原始响应的打印写入器
         * @param printWriter 缓存的打印写入器
         */
        ServletPrintWriter(PrintWriter main, PrintWriter printWriter) {
            super(main, true);
            this.printWriter = printWriter;
        }

        @Override
        public void write(char[] buff, int off, int len) {
            super.write(buff, off, len);
            super.flush();
            printWriter.write(buff, off, len);
            printWriter.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            printWriter.write(s, off, len);
            printWriter.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            printWriter.write(c);
            printWriter.flush();
        }

        @Override
        public void flush() {
            super.flush();
            printWriter.flush();
        }
    }

    /**
     * 分支输出流，用于同时将数据写入两个输出流。
     */
    class TeeOutputStream extends OutputStream {

        /**
         * 第一个输出流
         */
        private OutputStream oneOut;

        /**
         * 第二个输出流
         */
        private OutputStream twoOut;

        /**
         * 构造方法，初始化分支输出流。
         *
         * @param oneOut 第一个输出流，通常是原始响应的输出流
         * @param twoOut 第二个输出流，通常是缓存输出流
         */
        public TeeOutputStream(OutputStream oneOut, OutputStream twoOut) {
            this.oneOut = oneOut;
            this.twoOut = twoOut;
        }

        /**
         * 写入字节数组。
         *
         * @param buf 要写入的字节数组
         * @throws IOException 如果发生I/O错误
         */
        public void write(byte[] buf) throws IOException {
            this.oneOut.write(buf);
            this.twoOut.write(buf);
        }

        /**
         * 写入字节数组的指定部分。
         *
         * @param buf 要写入的字节数组
         * @param off 起始偏移量
         * @param len 要写入的长度
         * @throws IOException 如果发生I/O错误
         */
        public void write(byte[] buf, int off, int len) throws IOException {
            this.oneOut.write(buf, off, len);
            this.twoOut.write(buf, off, len);
        }

        /**
         * 写入单个字节。
         *
         * @param b 要写入的字节
         * @throws IOException 如果发生I/O错误
         */
        public void write(int b) throws IOException {
            this.oneOut.write(b);
            this.twoOut.write(b);
        }

        /**
         * 刷新输出流。
         *
         * @throws IOException 如果发生I/O错误
         */
        public void flush() throws IOException {
            this.oneOut.flush();
            this.twoOut.flush();
        }

        /**
         * 关闭输出流。
         *
         * @throws IOException 如果发生I/O错误
         */
        public void close() throws IOException {
            this.oneOut.close();
            this.twoOut.close();
        }
    }

}