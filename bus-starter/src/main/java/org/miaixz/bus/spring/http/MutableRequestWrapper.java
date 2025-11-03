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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.EscapeKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Request wrapper that caches the request body content and provides XSS protection.
 * <p>
 * This class extends {@link HttpServletRequestWrapper} and primarily offers the following functionalities:
 *
 * <ul>
 * <li>Caches the request body content, allowing it to be read multiple times.</li>
 * <li>Performs XSS filtering on request parameters and headers to prevent cross-site scripting attacks.</li>
 * <li>Logs request parameters for debugging and troubleshooting.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * // In a Servlet Filter:
 * public class XSSFilter implements Filter {
 *
 *     @Override
 *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 *             throws IOException, ServletException {
 *         // Wrap the request
 *         MutableRequestWrapper wrappedRequest = new MutableRequestWrapper((HttpServletRequest) request);
 *         // Continue the filter chain
 *         chain.doFilter(wrappedRequest, response);
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableRequestWrapper extends HttpServletRequestWrapper {

    /**
     * The original HTTP servlet request.
     */
    public HttpServletRequest request;

    /**
     * The content type of the request.
     */
    public String contentType;

    /**
     * The cached request body content as a byte array.
     */
    public byte[] body;

    /**
     * Custom {@link ServletInputStream} wrapper for the cached body.
     */
    public ServletInputStreamWrapper inputStreamWrapper;

    /**
     * Constructs a new {@code MutableRequestWrapper}, initializing the request wrapper.
     * <p>
     * This constructor reads and caches the request body content, initializes a custom input stream wrapper, and logs
     * request parameters.
     * </p>
     *
     * @param request The original {@link HttpServletRequest} object.
     * @throws IOException If an I/O error occurs while reading the request body.
     */
    public MutableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.request = request;
        this.contentType = request.getContentType();

        // Read and cache the request body content
        // Read input stream
        this.body = IoKit.readBytes(request.getInputStream());
        if (this.body == null || this.body.length == 0) {
            // If the input stream is empty and there are parameters, use parameterMap
            if (MapKit.isNotEmpty(request.getParameterMap())) {
                String paramString = request.getParameterMap().entrySet().stream()
                        .map(entry -> entry.getKey() + Symbol.EQUAL + String.join(Symbol.COMMA, entry.getValue()))
                        .collect(Collectors.joining(Symbol.AND));
                this.body = paramString.getBytes(Charset.UTF_8);
                this.contentType = MediaType.APPLICATION_FORM_URLENCODED; // Update contentType
            } else {
                this.body = new byte[0]; // Ensure body is not null
            }
        }

        // Log request parameters, prioritizing parameterMap
        Object logOut = MapKit.isNotEmpty(request.getParameterMap()) ? request.getParameterMap()
                : new String(this.body, Charset.UTF_8);
        if (logOut instanceof String) {
            // Remove newlines, tabs, and extra whitespace
            logOut = UrlKit.decodeQuery(((String) logOut).replaceAll("\\s+", Normal.EMPTY), Charset.UTF_8);
        }

        Logger.info(true, "Request", "Parameters: {}", JsonKit.toJsonString(logOut));

        // Initialize custom input stream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body);
        this.inputStreamWrapper = new ServletInputStreamWrapper(byteArrayInputStream);
        this.inputStreamWrapper.setInputStream(byteArrayInputStream);
    }

    /**
     * Returns the cached request body content.
     *
     * @return The request body content as a byte array.
     */
    public byte[] getBody() {
        return this.body;
    }

    /**
     * Returns the content type of the request.
     *
     * @return The content type string.
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Returns the original HTTP servlet request.
     *
     * @return The original {@link HttpServletRequest} object.
     */
    @Override
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * Returns a custom {@link ServletInputStream} that reads from the cached request body.
     *
     * @return A custom {@link ServletInputStream}.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.inputStreamWrapper;
    }

    /**
     * Returns a {@link BufferedReader} for reading the request body.
     *
     * @return A {@link BufferedReader} for the request body.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.inputStreamWrapper, Charset.UTF_8));
    }

    /**
     * Returns an array of {@code String} values for the specified request parameter.
     * <p>
     * This method performs XSS filtering on non-JSON parameter values.
     * </p>
     *
     * @param parameter The name of the parameter.
     * @return An array of filtered parameter values, or {@code null} if the parameter does not exist.
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
     * Returns the value of a request parameter as a {@code String}.
     * <p>
     * This method performs XSS filtering on non-JSON parameter values.
     * </p>
     *
     * @param name The name of the parameter.
     * @return The filtered parameter value, or {@code null} if the parameter does not exist.
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
     * Returns the value of the specified request header as a {@code String}.
     * <p>
     * This method performs XSS filtering on non-JSON header values.
     * </p>
     *
     * @param name The name of the header.
     * @return The filtered header value, or {@code null} if the header does not exist.
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
     * A custom {@link ServletInputStream} wrapper that allows the request body content to be read multiple times.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class ServletInputStreamWrapper extends ServletInputStream {

        /**
         * The underlying input stream.
         */
        private InputStream inputStream;

        /**
         * Checks if the end of the input stream has been reached.
         *
         * @return {@code true} if the end of the stream has been reached, {@code false} otherwise.
         */
        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                Logger.error(false, "Request", "Error checking if input stream is finished", e);
                return true; // Assume finished on error
            }
        }

        /**
         * Checks if the input stream is ready to be read.
         *
         * @return {@code true} if the input stream is ready, {@code false} otherwise.
         */
        @Override
        public boolean isReady() {
            return true; // Always ready as it reads from a ByteArrayInputStream
        }

        /**
         * Sets the read listener. This method is a no-op as this stream is not asynchronous.
         *
         * @param readListener The read listener.
         */
        @Override
        public void setReadListener(ReadListener readListener) {
            // No-op for synchronous stream
        }

        /**
         * Reads the next byte of data from the input stream.
         *
         * @return The next byte of data, or -1 if the end of the stream is reached.
         * @throws IOException If an I/O error occurs.
         */
        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }
    }

}
