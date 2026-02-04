/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.bodys;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;

import java.io.File;
import java.io.IOException;

/**
 * The body of an HTTP request.
 * <p>
 * This class represents the content of an HTTP request and supports creating request bodies from strings, byte arrays,
 * files, and other sources. It provides functionality for specifying the media type, content length, and writing the
 * content. It also supports special cases for duplex and one-shot transmission.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RequestBody {

    /**
     * Creates a new request body from a string.
     * <p>
     * If {@code contentType} is non-null and lacks a charset, UTF-8 will be used.
     * </p>
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content string.
     * @return A new {@link RequestBody} instance.
     */
    public static RequestBody of(MediaType contentType, String content) {
        java.nio.charset.Charset charset = Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return of(contentType, bytes);
    }

    /**
     * Creates a new request body from a {@link ByteString}.
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content as a {@link ByteString}.
     * @return A new {@link RequestBody} instance.
     */
    public static RequestBody of(final MediaType contentType, final ByteString content) {
        return new RequestBody() {

            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return content.size();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * Creates a new request body from a byte array.
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content as a byte array.
     * @return A new {@link RequestBody} instance.
     * @throws NullPointerException if content is null.
     */
    public static RequestBody of(final MediaType contentType, final byte[] content) {
        return of(contentType, content, 0, content.length);
    }

    /**
     * Creates a new request body from a portion of a byte array.
     *
     * @param contentType The media type of the content, which may be null.
     * @param content     The content as a byte array.
     * @param offset      The starting offset in the byte array.
     * @param byteCount   The number of bytes to use.
     * @return A new {@link RequestBody} instance.
     * @throws NullPointerException           if content is null.
     * @throws ArrayIndexOutOfBoundsException if the offset or byteCount are invalid.
     */
    public static RequestBody of(
            final MediaType contentType,
            final byte[] content,
            final int offset,
            final int byteCount) {
        if (null == content) {
            throw new NullPointerException("content == null");
        }
        Builder.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {

            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return byteCount;
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * Creates a new request body from a file.
     *
     * @param contentType The media type of the content, which may be null.
     * @param file        The file to use as the content.
     * @return A new {@link RequestBody} instance.
     * @throws NullPointerException if file is null.
     */
    public static RequestBody of(final MediaType contentType, final File file) {
        if (null == file) {
            throw new NullPointerException("file == null");
        }

        return new RequestBody() {

            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                try (Source source = IoKit.source(file)) {
                    sink.writeAll(source);
                }
            }
        };
    }

    /**
     * Returns the media type of this request body.
     *
     * @return The media type, which may be null.
     */
    public abstract MediaType contentType();

    /**
     * Returns the number of bytes that will be written to {@code sink} when this request body is transmitted, or -1 if
     * that count is unknown.
     *
     * @return The content length.
     * @throws IOException if the length cannot be determined.
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * Writes the content of this request body to the given sink.
     *
     * @param sink The sink to write to.
     * @throws IOException if an I/O error occurs during writing.
     */
    public abstract void writeTo(BufferSink sink) throws IOException;

    /**
     * Returns whether this request body is a duplex body.
     * <p>
     * A duplex request body allows for interleaved transmission of request and response data, which is only supported
     * for HTTP/2. This returns false by default unless overridden by a subclass.
     * </p>
     *
     * @return {@code true} if this is a duplex request body.
     */
    public boolean isDuplex() {
        return false;
    }

    /**
     * Returns whether this request body is a one-shot body.
     * <p>
     * A one-shot request body can only be transmitted once, typically used for destructively-written scenarios. This
     * returns false by default unless overridden by a subclass.
     * </p>
     *
     * @return {@code true} if this is a one-shot request body.
     */
    public boolean isOneShot() {
        return false;
    }

}
