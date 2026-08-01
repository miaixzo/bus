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
package org.miaixz.bus.spring.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.miaixz.bus.core.net.MediaType;

/**
 * Response wrapper that passes content directly to the client while retaining a repeatable-read copy for diagnostics.
 * <p>
 * Writer and output-stream access follow the Servlet API's mutual-exclusion contract. Streaming responses such as
 * Server-Sent Events are always passed through without caching.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachedBodyResponseWrapper extends HttpServletResponseWrapper {

    /** Cached response bytes. */
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();

    /** Lazily created output stream returned consistently for this response. */
    private ServletOutputStream outputStream;

    /** Lazily created writer returned consistently for this response. */
    private PrintWriter writer;

    /** Character writer used only for the cached copy. */
    private Writer cachedWriter;

    /** Whether character access has already been selected. */
    private boolean writerAccessed;

    /** Whether byte-stream access has already been selected. */
    private boolean outputStreamAccessed;

    /** Whether the response content type represents a streaming response. */
    private boolean streaming;

    /**
     * Creates a pass-through response wrapper with diagnostic body caching.
     *
     * @param response original servlet response
     */
    public CachedBodyResponseWrapper(HttpServletResponse response) {
        super(response);
        refreshStreamingState();
    }

    /**
     * Returns the single writer associated with this wrapper.
     *
     * @return pass-through and caching writer
     * @throws IOException           if the underlying response writer cannot be obtained
     * @throws IllegalStateException if byte-stream access was already selected
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.outputStreamAccessed) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        this.writerAccessed = true;
        if (this.writer == null) {
            PrintWriter delegate = super.getWriter();
            this.cachedWriter = new OutputStreamWriter(this.cachedBody, responseCharset());
            this.writer = new PrintWriter(new CachingWriter(delegate, this.cachedWriter));
        }
        return this.writer;
    }

    /**
     * Returns the single output stream associated with this wrapper.
     *
     * @return pass-through and caching servlet output stream
     * @throws IOException           if the underlying response stream cannot be obtained
     * @throws IllegalStateException if character-writer access was already selected
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
     * Updates the response content type and streaming state.
     *
     * @param type response content type
     */
    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        refreshStreamingState();
    }

    /** Clears both the client response buffer and the diagnostic copy. */
    @Override
    public void resetBuffer() {
        flushCachedWriter();
        super.resetBuffer();
        this.cachedBody.reset();
    }

    /** Clears response state and the diagnostic copy. */
    @Override
    public void reset() {
        flushCachedWriter();
        super.reset();
        this.cachedBody.reset();
        this.streaming = false;
    }

    /**
     * Returns a copy of the cached response content.
     *
     * @return cached bytes, or an empty array for streaming responses
     */
    public byte[] getBody() {
        refreshStreamingState();
        if (this.streaming) {
            return new byte[0];
        }
        flushCachedWriter();
        return this.cachedBody.toByteArray();
    }

    /** Refreshes and returns whether the current response is streaming. */
    public void streaming() {
        refreshStreamingState();
    }

    /**
     * Returns whether the current response is streaming.
     *
     * @return {@code true} for streaming response content types
     */
    public boolean isStreaming() {
        refreshStreamingState();
        return this.streaming;
    }

    /** Resolves the response character encoding with a UTF-8 fallback. */
    private Charset responseCharset() {
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

    /** Updates the streaming flag from the current response content type. */
    private void refreshStreamingState() {
        String contentType = getContentType();
        this.streaming = contentType != null
                && contentType.toLowerCase(java.util.Locale.ROOT).contains(MediaType.SERVER_SENT_EVENTS);
    }

    /** Flushes only the cached character writer without committing the client response. */
    private void flushCachedWriter() {
        if (this.cachedWriter == null) {
            return;
        }
        try {
            this.cachedWriter.flush();
        } catch (IOException ignored) {
            // ByteArrayOutputStream-backed writers do not normally fail while flushing.
        }
    }

    /** Writes bytes to the client and, for non-streaming responses, to the diagnostic cache. */
    private final class CachingServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream delegate;

        private CachingServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isReady() {
            return this.delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            this.delegate.setWriteListener(writeListener);
        }

        @Override
        public void write(int value) throws IOException {
            this.delegate.write(value);
            if (!isStreaming()) {
                cachedBody.write(value);
            }
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            this.delegate.write(bytes, offset, length);
            if (!isStreaming()) {
                cachedBody.write(bytes, offset, length);
            }
        }

        @Override
        public void flush() throws IOException {
            this.delegate.flush();
        }

        @Override
        public void close() throws IOException {
            this.delegate.close();
        }
    }

    /** Writes characters to the client and, for non-streaming responses, to the diagnostic cache. */
    private final class CachingWriter extends Writer {

        private final Writer delegate;

        private final Writer cache;

        private CachingWriter(Writer delegate, Writer cache) {
            this.delegate = delegate;
            this.cache = cache;
        }

        @Override
        public void write(char[] characters, int offset, int length) throws IOException {
            this.delegate.write(characters, offset, length);
            if (!isStreaming()) {
                this.cache.write(characters, offset, length);
            }
        }

        @Override
        public void flush() throws IOException {
            this.delegate.flush();
            this.cache.flush();
        }

        @Override
        public void close() throws IOException {
            this.delegate.close();
            this.cache.close();
        }
    }

}
