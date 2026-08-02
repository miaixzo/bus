/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.web.wrapper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Repeatable request body wrapper with a non-negotiable byte limit.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    /**
     * Complete bounded request body.
     */
    private final byte[] body;

    /**
     * Reads and caches the request body, discarding all bytes when the hard limit is exceeded.
     *
     * @param request source HTTP request
     * @param limit   maximum body size in bytes
     * @throws IOException when the request cannot be read or exceeds the configured limit
     */
    public CachedBodyRequestWrapper(HttpServletRequest request, long limit) throws IOException {
        super(request);
        if (limit <= 0 || limit > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Request body limit is out of range");
        }
        this.body = readBounded(request, (int) limit);
    }

    /**
     * Returns a defensive copy of the cached bytes.
     *
     * @return detached cached body
     */
    public byte[] getBody() {
        return this.body.clone();
    }

    /**
     * Returns the wrapped HTTP request with its concrete Servlet type.
     */
    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) super.getRequest();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(this.body);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.body), requestCharset()));
    }

    /**
     * Resolves the request charset with UTF-8 as the safe fallback.
     *
     * @return request charset
     */
    private Charset requestCharset() {
        String encoding = getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (RuntimeException ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * Reads a request body without retaining bytes beyond the hard limit.
     *
     * @param request source HTTP request
     * @param limit   maximum body size in bytes
     * @return complete bounded body
     * @throws IOException when reading fails or the body exceeds the limit
     */
    private static byte[] readBounded(HttpServletRequest request, int limit) throws IOException {
        int initial = request.getContentLength() > 0 ? Math.min(request.getContentLength(), Math.min(limit, 8192)) : 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream(initial);
        byte[] buffer = new byte[Math.min(8192, limit)];
        int total = 0;
        try {
            ServletInputStream input = request.getInputStream();
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (count > limit - total) {
                    output.reset();
                    throw new PayloadTooLargeException();
                }
                output.write(buffer, 0, count);
                total += count;
            }
            return output.toByteArray();
        } catch (IOException | RuntimeException failure) {
            output.reset();
            throw failure;
        }
    }

    /**
     * Signals that an unknown-length request crossed its configured body limit.
     */
    public static final class PayloadTooLargeException extends IOException {

        /**
         * Creates an exception for a body crossing its configured limit.
         */
        public PayloadTooLargeException() {
            super("Request body exceeds the configured limit");
        }
    }

    /**
     * In-memory Servlet stream over the cached body.
     */
    private static final class CachedServletInputStream extends ServletInputStream {

        /**
         * Byte stream backing this Servlet stream.
         */
        private final ByteArrayInputStream input;

        /**
         * Creates a stream over cached request bytes.
         *
         * @param body cached request body
         */
        private CachedServletInputStream(byte[] body) {
            this.input = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return this.input.read();
        }

        @Override
        public int read(byte[] bytes, int offset, int length) {
            return this.input.read(bytes, offset, length);
        }

        @Override
        public boolean isFinished() {
            return this.input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("ReadListener must not be null");
            }
            try {
                if (isFinished())
                    listener.onAllDataRead();
                else
                    listener.onDataAvailable();
            } catch (IOException exception) {
                listener.onError(exception);
            }
        }
    }

}
