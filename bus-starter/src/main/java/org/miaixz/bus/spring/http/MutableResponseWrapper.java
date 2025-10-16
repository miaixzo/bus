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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.miaixz.bus.core.lang.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A repeatable-read response wrapper that supports caching response content for logging and further processing.
 * <p>
 * This class extends {@link HttpServletResponseWrapper} and primarily offers the following functionalities:
 *
 * <ul>
 * <li>Caches the response content, allowing it to be read multiple times.</li>
 * <li>Automatically identifies and handles streaming responses (e.g., Server-Sent Events - SSE), and does not cache
 * them.</li>
 * <li>Provides methods to retrieve the cached response content for logging or other post-processing.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * // In a Servlet Filter:
 * public class ResponseCacheFilter implements Filter {
 *
 *     @Override
 *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 *             throws IOException, ServletException {
 *         // Wrap the response
 *         MutableResponseWrapper wrappedResponse = new MutableResponseWrapper((HttpServletResponse) response);
 *         // Continue the filter chain
 *         chain.doFilter(request, wrappedResponse);
 *         // Get the response content
 *         byte[] responseBody = wrappedResponse.getBody();
 *         // Log or perform other processing
 *         logResponse(responseBody);
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableResponseWrapper extends HttpServletResponseWrapper {

    /**
     * A {@link ByteArrayOutputStream} used to cache the response content.
     */
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    /**
     * A {@link PrintWriter} used to write character data to the cached output stream.
     */
    private PrintWriter writer = new PrintWriter(byteArrayOutputStream);

    /**
     * A flag indicating whether the response is a streaming response (e.g., SSE). Streaming responses are not cached.
     */
    private boolean isStreaming = false;

    /**
     * Constructs a new {@code MutableResponseWrapper}, initializing the response wrapper.
     *
     * @param response The original {@link HttpServletResponse} object.
     */
    public MutableResponseWrapper(HttpServletResponse response) {
        super(response);
        this.streaming();
    }

    /**
     * Returns a {@link PrintWriter} for writing response content.
     * <p>
     * For streaming responses (e.g., SSE), it directly returns the original response's {@link PrintWriter} without
     * caching. For non-streaming responses, it returns a wrapped {@link PrintWriter} that writes to both the original
     * response and the cache.
     * </p>
     *
     * @return A {@link PrintWriter} for the response.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        this.streaming();
        if (isStreaming) {
            // For SSE streaming responses, directly return the original response writer without additional processing.
            return super.getWriter();
        }
        return new ServletPrintWriter(super.getWriter(), writer);
    }

    /**
     * Sets the content type of the response.
     * <p>
     * This method also checks the content type to determine if it's a streaming response (e.g., SSE).
     * </p>
     *
     * @param type The content type string.
     */
    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        // Determine if it's a streaming response based on Content-Type
        if (type != null) {
            String lowerType = type.toLowerCase();
            isStreaming = lowerType.contains(MediaType.SERVER_SENT_EVENTS);
        }
    }

    /**
     * Returns a {@link ServletOutputStream} for writing response content.
     * <p>
     * For streaming responses (e.g., SSE), it directly returns the original response's {@link ServletOutputStream}
     * without caching. For non-streaming responses, it returns a wrapped {@link ServletOutputStream} that writes to
     * both the original response and the cache.
     * </p>
     *
     * @return A {@link ServletOutputStream} for the response.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        this.streaming();
        // For SSE streaming responses, directly return the original response stream without additional processing.
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
                // Empty implementation
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
     * Returns the cached response content as a byte array.
     *
     * @return The response content as a byte array.
     */
    public byte[] getBody() {
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Checks and sets whether the response is a streaming response.
     * <p>
     * This method determines if the response is streaming (e.g., SSE) based on its content type and updates the
     * {@link #isStreaming} flag.
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
     * Checks if the response is a streaming response.
     *
     * @return {@code true} if it is a streaming response, {@code false} otherwise.
     */
    public boolean isStreaming() {
        return isStreaming;
    }

    /**
     * A custom {@link PrintWriter} that writes to both the original response's {@link PrintWriter} and a cached
     * {@link PrintWriter}.
     */
    private static class ServletPrintWriter extends PrintWriter {

        /**
         * The cached {@link PrintWriter}.
         */
        PrintWriter printWriter;

        /**
         * Constructs a new {@code ServletPrintWriter}.
         *
         * @param main        The original response's {@link PrintWriter}.
         * @param printWriter The cached {@link PrintWriter}.
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
     * A {@link OutputStream} that writes all data to two underlying {@link OutputStream}s.
     */
    class TeeOutputStream extends OutputStream {

        /**
         * The first underlying output stream, typically the original response's output stream.
         */
        private OutputStream oneOut;

        /**
         * The second underlying output stream, typically the cache output stream.
         */
        private OutputStream twoOut;

        /**
         * Constructs a new {@code TeeOutputStream}.
         *
         * @param oneOut The first output stream.
         * @param twoOut The second output stream.
         */
        public TeeOutputStream(OutputStream oneOut, OutputStream twoOut) {
            this.oneOut = oneOut;
            this.twoOut = twoOut;
        }

        /**
         * Writes the specified byte array to both output streams.
         *
         * @param buf The byte array to write.
         * @throws IOException If an I/O error occurs.
         */
        public void write(byte[] buf) throws IOException {
            this.oneOut.write(buf);
            this.twoOut.write(buf);
        }

        /**
         * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to both output streams.
         *
         * @param buf The byte array to write from.
         * @param off The start offset in the data.
         * @param len The number of bytes to write.
         * @throws IOException If an I/O error occurs.
         */
        public void write(byte[] buf, int off, int len) throws IOException {
            this.oneOut.write(buf, off, len);
            this.twoOut.write(buf, off, len);
        }

        /**
         * Writes the specified byte to both output streams.
         *
         * @param b The byte to write.
         * @throws IOException If an I/O error occurs.
         */
        public void write(int b) throws IOException {
            this.oneOut.write(b);
            this.twoOut.write(b);
        }

        /**
         * Flushes both output streams.
         *
         * @throws IOException If an I/O error occurs.
         */
        public void flush() throws IOException {
            this.oneOut.flush();
            this.twoOut.flush();
        }

        /**
         * Closes both output streams.
         *
         * @throws IOException If an I/O error occurs.
         */
        public void close() throws IOException {
            this.oneOut.close();
            this.twoOut.close();
        }
    }

}
