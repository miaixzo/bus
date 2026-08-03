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
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.MediaType;

/**
 * Pass-through response wrapper that retains at most a configured number of diagnostic bytes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachedBodyResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Bounded diagnostic byte cache.
     */
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    /**
     * Maximum retained diagnostic bytes.
     */
    private final int limit;
    /**
     * Lazily created pass-through Servlet stream.
     */
    private ServletOutputStream outputStream;
    /**
     * Lazily created pass-through response writer.
     */
    private PrintWriter writer;
    /**
     * Character encoder writing only to the bounded cache.
     */
    private Writer cachedWriter;
    /**
     * Whether callers selected character output.
     */
    private boolean writerAccessed;
    /**
     * Whether callers selected byte output.
     */
    private boolean outputStreamAccessed;
    /**
     * Whether diagnostic caching has been permanently disabled.
     */
    private boolean passthrough;

    /**
     * Creates a response cache that switches permanently to direct pass-through above its limit.
     *
     * @param response target HTTP response
     * @param limit    maximum retained diagnostic bytes
     */
    public CachedBodyResponseWrapper(HttpServletResponse response, long limit) {
        super(response);
        if (limit <= 0 || limit > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Response body limit is out of range");
        }
        this.limit = (int) limit;
        refreshPassthroughState();
    }

    /**
     * Returns the response character writer while mirroring bounded diagnostic content.
     *
     * @return pass-through response writer
     * @throws IOException when the underlying response cannot provide a writer
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.outputStreamAccessed) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        this.writerAccessed = true;
        if (this.writer == null) {
            this.cachedWriter = new OutputStreamWriter(new CacheOutputStream(), responseCharset());
            this.writer = new PrintWriter(new CachingWriter(super.getWriter(), this.cachedWriter));
        }
        return this.writer;
    }

    /**
     * Returns the response byte stream while mirroring bounded diagnostic content.
     *
     * @return pass-through response stream
     * @throws IOException when the underlying response cannot provide a stream
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writerAccessed) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        this.outputStreamAccessed = true;
        if (this.outputStream == null) {
            this.outputStream = new CachingServletOutputStream(super.getOutputStream());
        }
        return this.outputStream;
    }

    /**
     * Updates the content type and disables caching for streaming media types.
     *
     * @param type response content type
     */
    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        refreshPassthroughState();
    }

    /**
     * Replaces a response header and reevaluates streaming disposition.
     *
     * @param name  header name
     * @param value header value
     */
    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        refreshPassthroughState();
    }

    /**
     * Adds a response header and reevaluates streaming disposition.
     *
     * @param name  header name
     * @param value header value
     */
    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        refreshPassthroughState();
    }

    /**
     * Clears both the underlying response buffer and retained diagnostic bytes.
     */
    @Override
    public void resetBuffer() {
        flushCachedWriter();
        super.resetBuffer();
        this.cachedBody.reset();
        this.passthrough = false;
        refreshPassthroughState();
    }

    /**
     * Resets response metadata, output state, and retained diagnostic bytes.
     */
    @Override
    public void reset() {
        flushCachedWriter();
        super.reset();
        this.cachedBody.reset();
        this.passthrough = false;
    }

    /**
     * Returns the complete cached body, or empty when streaming or over limit.
     *
     * @return detached complete cached body, or an empty array in pass-through mode
     */
    public byte[] getBody() {
        refreshPassthroughState();
        flushCachedWriter();
        enforceLimit();
        return this.passthrough ? new byte[0] : this.cachedBody.toByteArray();
    }

    /**
     * Returns whether the wrapper is in permanent pass-through mode.
     *
     * @return {@code true} when body caching is disabled for this response
     */
    public boolean isStreaming() {
        refreshPassthroughState();
        return this.passthrough;
    }

    /**
     * Resolves the response charset with UTF-8 as the safe fallback.
     *
     * @return response charset
     */
    private Charset responseCharset() {
        String encoding = getCharacterEncoding();
        try {
            return encoding == null || encoding.isBlank() ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        } catch (RuntimeException ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * Updates pass-through mode from content type and disposition headers.
     */
    private void refreshPassthroughState() {
        String contentType = getContentType();
        String disposition = getHeader("Content-Disposition");
        String normalized = contentType == null ? Normal.EMPTY : contentType.toLowerCase(Locale.ROOT);
        if (normalized.contains(MediaType.SERVER_SENT_EVENTS)
                || normalized.startsWith(MediaType.APPLICATION_OCTET_STREAM)
                || disposition != null && disposition.toLowerCase(Locale.ROOT).contains("attachment")) {
            switchToPassthrough();
        }
    }

    /**
     * Flushes pending encoded characters into the bounded cache.
     */
    private void flushCachedWriter() {
        if (this.cachedWriter != null && !this.passthrough) {
            try {
                this.cachedWriter.flush();
            } catch (IOException ignored) {
                switchToPassthrough();
            }
        }
    }

    /**
     * Switches to pass-through mode when cached bytes exceed the limit.
     */
    private void enforceLimit() {
        if (this.cachedBody.size() > this.limit) {
            switchToPassthrough();
        }
    }

    /**
     * Retains one diagnostic byte when space remains.
     *
     * @param value byte value to retain
     */
    private void cache(int value) {
        if (this.passthrough) {
            return;
        }
        if (this.cachedBody.size() == this.limit) {
            switchToPassthrough();
        } else {
            this.cachedBody.write(value);
        }
    }

    /**
     * Retains a diagnostic byte range when the complete range fits.
     *
     * @param bytes  source bytes
     * @param offset source offset
     * @param length byte count
     */
    private void cache(byte[] bytes, int offset, int length) {
        if (this.passthrough) {
            return;
        }
        if (length > this.limit - this.cachedBody.size()) {
            switchToPassthrough();
        } else {
            this.cachedBody.write(bytes, offset, length);
        }
    }

    /**
     * Permanently disables caching and releases retained diagnostic bytes.
     */
    private void switchToPassthrough() {
        this.passthrough = true;
        this.cachedBody.reset();
    }

    /**
     * Pass-through Servlet stream that mirrors writes into the bounded cache.
     */
    private final class CachingServletOutputStream extends ServletOutputStream {

        /**
         * Target response stream.
         */
        private final ServletOutputStream delegate;

        /**
         * Creates a caching pass-through stream.
         *
         * @param delegate target response stream
         */
        private CachingServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        /**
         * Delegates nonblocking readiness to the target response stream.
         *
         * @return target stream readiness
         */
        @Override
        public boolean isReady() {
            return this.delegate.isReady();
        }

        /**
         * Installs the nonblocking write listener on the target response stream.
         *
         * @param listener nonblocking write listener
         */
        @Override
        public void setWriteListener(WriteListener listener) {
            this.delegate.setWriteListener(listener);
        }

        /**
         * Writes and conditionally caches one response byte.
         *
         * @param value byte value
         * @throws IOException when target output fails
         */
        @Override
        public void write(int value) throws IOException {
            this.delegate.write(value);
            cache(value);
        }

        /**
         * Writes and conditionally caches a response byte range.
         *
         * @param bytes  source bytes
         * @param offset source offset
         * @param length byte count
         * @throws IOException when target output fails
         */
        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            this.delegate.write(bytes, offset, length);
            cache(bytes, offset, length);
        }

        /**
         * Flushes the target response stream.
         *
         * @throws IOException when target output fails
         */
        @Override
        public void flush() throws IOException {
            this.delegate.flush();
        }

        /**
         * Closes the target response stream.
         *
         * @throws IOException when target output fails
         */
        @Override
        public void close() throws IOException {
            this.delegate.close();
        }
    }

    /**
     * Bounded byte sink used by the character encoder.
     */
    private final class CacheOutputStream extends OutputStream {

        /**
         * Creates the bounded byte sink.
         */
        private CacheOutputStream() {
            // No initialization required.
        }

        /**
         * Retains one encoded byte when the diagnostic limit permits it.
         *
         * @param value encoded byte
         */
        @Override
        public void write(int value) {
            cache(value);
        }

        /**
         * Retains an encoded byte range when the diagnostic limit permits it.
         *
         * @param bytes  encoded bytes
         * @param offset source offset
         * @param length byte count
         */
        @Override
        public void write(byte[] bytes, int offset, int length) {
            cache(bytes, offset, length);
        }
    }

    /**
     * Pass-through character writer that mirrors complete writes into the encoder cache.
     */
    private final class CachingWriter extends Writer {

        /**
         * Target response writer.
         */
        private final Writer delegate;
        /**
         * Bounded-cache character encoder.
         */
        private final Writer cache;

        /**
         * Creates a caching pass-through writer.
         *
         * @param delegate target response writer
         * @param cache    bounded-cache encoder
         */
        private CachingWriter(Writer delegate, Writer cache) {
            this.delegate = delegate;
            this.cache = cache;
        }

        /**
         * Writes characters to the response and bounded diagnostic encoder.
         *
         * @param characters source characters
         * @param offset     source offset
         * @param length     character count
         * @throws IOException when target or cache output fails
         */
        @Override
        public void write(char[] characters, int offset, int length) throws IOException {
            this.delegate.write(characters, offset, length);
            if (!passthrough) {
                this.cache.write(characters, offset, length);
                this.cache.flush();
                enforceLimit();
            }
        }

        /**
         * Flushes the target writer and active diagnostic encoder.
         *
         * @throws IOException when either writer fails
         */
        @Override
        public void flush() throws IOException {
            this.delegate.flush();
            if (!passthrough)
                this.cache.flush();
        }

        /**
         * Closes the target writer and active diagnostic encoder.
         *
         * @throws IOException when either writer fails
         */
        @Override
        public void close() throws IOException {
            this.delegate.close();
            if (!passthrough)
                this.cache.close();
        }
    }

}
