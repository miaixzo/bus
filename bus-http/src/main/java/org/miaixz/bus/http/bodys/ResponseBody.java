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
package org.miaixz.bus.http.bodys;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;

import java.io.*;

/**
 * The body of an HTTP response.
 * <p>
 * This class represents the content of a response from the origin server to the client. It is a one-shot stream that
 * can only be read once. The response body relies on limited resources (like network sockets or cached files) and must
 * be closed to release them. It supports reading content as a byte stream, character stream, or a complete byte
 * array/string, making it suitable for handling large responses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ResponseBody implements Closeable {

    /**
     * The character stream reader.
     */
    private Reader reader;

    /**
     * Creates a new response body from a string.
     * <p>
     * If {@code contentType} is non-null and lacks a charset, UTF-8 will be used.
     * </p>
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content string.
     * @return A new {@link ResponseBody} instance.
     */
    public static ResponseBody create(MediaType contentType, String content) {
        java.nio.charset.Charset charset = Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    /**
     * Creates a new response body from a byte array.
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content as a byte array.
     * @return A new {@link ResponseBody} instance.
     */
    public static ResponseBody create(final MediaType contentType, byte[] content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.length, buffer);
    }

    /**
     * Creates a new response body from a {@link ByteString}.
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content as a {@link ByteString}.
     * @return A new {@link ResponseBody} instance.
     */
    public static ResponseBody create(MediaType contentType, ByteString content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.size(), buffer);
    }

    /**
     * Creates a new response body from a data source.
     *
     * @param contentType The media type of the content, which may be null.
     * @param length      The content length.
     * @param content     The data source.
     * @return A new {@link ResponseBody} instance.
     * @throws NullPointerException if content is null.
     */
    public static ResponseBody create(final MediaType contentType, final long length, final BufferSource content) {
        if (null == content) {
            throw new NullPointerException("source == null");
        }

        return new ResponseBody() {

            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return length;
            }

            @Override
            public BufferSource source() {
                return content;
            }
        };
    }

    /**
     * Returns the media type of this response body.
     *
     * @return The media type, which may be null.
     */
    public abstract MediaType contentType();

    /**
     * Returns the number of bytes in the response. This will be -1 if the size is unknown.
     *
     * @return The content length.
     */
    public abstract long contentLength();

    /**
     * Returns the response body as a byte stream.
     *
     * @return The input stream.
     */
    public final InputStream byteStream() {
        return source().inputStream();
    }

    /**
     * Returns the data source for this response body.
     *
     * @return The data source.
     */
    public abstract BufferSource source();

    /**
     * Returns the response body as a byte array.
     * <p>
     * This method loads the entire response body into memory and is suitable for small responses. For large responses,
     * this may cause an {@link OutOfMemoryError}, and stream-based reading is recommended.
     * </p>
     *
     * @return The byte array.
     * @throws IOException if reading fails or the length does not match.
     */
    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength > Integer.MAX_VALUE) {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        }

        byte[] bytes;
        try (BufferSource source = source()) {
            bytes = source.readByteArray();
        }
        if (contentLength != -1 && contentLength != bytes.length) {
            throw new IOException(
                    "Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
        }
        return bytes;
    }

    /**
     * Returns the response body as a character stream.
     * <p>
     * This method automatically handles the Byte Order Mark (BOM) or the charset specified in the Content-Type,
     * defaulting to UTF-8. Multiple calls will return the same instance.
     * </p>
     *
     * @return The character stream reader.
     */
    public final Reader charStream() {
        Reader r = reader;
        return null != r ? r : (reader = new BomAwareReader(source(), charset()));
    }

    /**
     * Returns the response body as a string.
     * <p>
     * This method loads the entire response body into memory and is suitable for small responses. It automatically
     * handles the Byte Order Mark (BOM) or the charset specified in the Content-Type, defaulting to UTF-8. For large
     * responses, this may cause an {@link OutOfMemoryError}, and stream-based reading is recommended.
     * </p>
     *
     * @return The string.
     * @throws IOException if reading fails.
     */
    public final String string() throws IOException {
        try (BufferSource source = source()) {
            java.nio.charset.Charset charset = Builder.bomAwareCharset(source, charset());
            return source.readString(charset);
        }
    }

    /**
     * Returns the character set of this response body.
     *
     * @return The character set, defaulting to UTF-8.
     */
    private java.nio.charset.Charset charset() {
        MediaType contentType = contentType();
        return null != contentType ? contentType.charset(Charset.UTF_8) : Charset.UTF_8;
    }

    /**
     * Closes the response body, releasing any associated resources (like network sockets or cached files).
     */
    @Override
    public void close() {
        IoKit.close(source());
    }

    /**
     * A character stream reader that is aware of the Byte Order Mark (BOM).
     */
    static final class BomAwareReader extends Reader {

        /**
         * The data source.
         */
        private final BufferSource source;
        /**
         * The character set.
         */
        private final java.nio.charset.Charset charset;
        /**
         * Whether the reader is closed.
         */
        private boolean closed;
        /**
         * The delegate reader.
         */
        private Reader delegate;

        /**
         * Constructs a new {@code BomAwareReader}.
         *
         * @param source  The data source.
         * @param charset The character set.
         */
        BomAwareReader(BufferSource source, java.nio.charset.Charset charset) {
            this.source = source;
            this.charset = charset;
        }

        /**
         * Reads characters into a portion of an array.
         *
         * @param cbuf The character buffer.
         * @param off  The starting offset.
         * @param len  The number of characters to read.
         * @return The number of characters read.
         * @throws IOException if the stream is closed or an I/O error occurs.
         */
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (closed)
                throw new IOException("Stream closed");

            Reader delegate = this.delegate;
            if (null == delegate) {
                java.nio.charset.Charset charset = Builder.bomAwareCharset(source, this.charset);
                delegate = this.delegate = new InputStreamReader(source.inputStream(), charset);
            }
            return delegate.read(cbuf, off, len);
        }

        /**
         * Closes the reader.
         *
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public void close() throws IOException {
            closed = true;
            if (null != delegate) {
                delegate.close();
            } else {
                source.close();
            }
        }
    }

}
