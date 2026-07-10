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
package org.miaixz.bus.fabric.protocol.http.body;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * HTTP multipart/form-data body.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MultipartBody implements RequestBody {

    /**
     * CRLF bytes.
     */
    private static final byte[] CRLF = { Symbol.C_CR, Symbol.C_LF };

    /**
     * Multipart boundary.
     */
    private final String boundary;

    /**
     * Multipart parts.
     */
    private final List<Part> parts;

    /**
     * Multipart media.
     */
    private final MediaType media;

    /**
     * Multipart payload.
     */
    private final Payload payload;

    /**
     * Creates body.
     *
     * @param boundary boundary
     * @param parts    parts
     * @param media    media
     * @param payload  payload
     */
    private MultipartBody(final String boundary, final List<Part> parts, final MediaType media, final Payload payload) {
        this.boundary = validateBoundary(boundary);
        this.parts = List.copyOf(parts);
        if (media == null || payload == null) {
            throw new ValidateException("Multipart media and payload must not be null");
        }
        this.media = media;
        this.payload = payload;
    }

    /**
     * Creates builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns boundary.
     *
     * @return boundary
     */
    public String boundary() {
        return boundary;
    }

    /**
     * Returns parts.
     *
     * @return immutable parts
     */
    public List<Part> parts() {
        return parts;
    }

    /**
     * Returns media.
     *
     * @return media
     */
    @Override
    public MediaType media() {
        return media;
    }

    /**
     * Returns payload.
     *
     * @return payload
     */
    @Override
    public Payload payload() {
        return payload;
    }

    /**
     * Multipart body part.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Part {

        /**
         * Part field name.
         */
        private final String name;

        /**
         * Optional file name.
         */
        private final String filename;

        /**
         * Part headers.
         */
        private final Headers headers;

        /**
         * Part payload.
         */
        private final Payload payload;

        /**
         * Creates a part.
         *
         * @param name     part name
         * @param filename file name
         * @param headers  headers
         * @param payload  payload
         */
        private Part(final String name, final String filename, final Headers headers, final Payload payload) {
            this.name = validatePartName(name, "Part name");
            this.filename = filename == null ? null : validatePartName(filename, "Part filename");
            if (headers == null) {
                throw new ValidateException("Part headers must not be null");
            }
            validatePartPayload(payload);
            this.headers = headers;
            this.payload = payload;
        }

        /**
         * Creates a normal payload part.
         *
         * @param name    part name
         * @param payload payload
         * @return part
         */
        public static Part of(final String name, final Payload payload) {
            final String validName = validatePartName(name, "Part name");
            validatePartPayload(payload);
            return new Part(validName, null, partHeaders(validName, null, null, payload.length()), payload);
        }

        /**
         * Creates a file part.
         *
         * @param name  part name
         * @param path  file path
         * @param media media type
         * @return part
         */
        public static Part file(final String name, final Path path, final MediaType media) {
            final String validName = validatePartName(name, "Part name");
            if (path == null) {
                throw new ValidateException("Part file path must not be null");
            }
            if (media == null) {
                throw new ValidateException("Part file media must not be null");
            }
            final Path filenamePath = path.getFileName();
            if (filenamePath == null) {
                throw new ValidateException("Part file name must not be null");
            }
            return file(validName, filenamePath.toString(), path, media);
        }

        /**
         * Creates a file part with an explicit filename.
         *
         * @param name     part name
         * @param filename file name
         * @param path     file path
         * @param media    media type
         * @return part
         */
        public static Part file(final String name, final String filename, final Path path, final MediaType media) {
            if (path == null) {
                throw new ValidateException("Part file path must not be null");
            }
            if (media == null) {
                throw new ValidateException("Part file media must not be null");
            }
            final FileBody body = FileBody.of(path, media);
            return file(name, filename, body.payload(), body.media());
        }

        /**
         * Creates a file part from an arbitrary payload.
         *
         * @param name     part name
         * @param filename file name
         * @param payload  payload
         * @param media    media type
         * @return part
         */
        public static Part file(
                final String name,
                final String filename,
                final Payload payload,
                final MediaType media) {
            final String validName = validatePartName(name, "Part name");
            final String validFilename = validatePartName(filename, "Part filename");
            if (media == null) {
                throw new ValidateException("Part file media must not be null");
            }
            validatePartPayload(payload);
            return new Part(validName, validFilename, partHeaders(validName, validFilename, media, payload.length()),
                    payload);
        }

        /**
         * Returns the part name.
         *
         * @return part name
         */
        public String name() {
            return name;
        }

        /**
         * Returns the file name.
         *
         * @return file name or null
         */
        public String filename() {
            return filename;
        }

        /**
         * Returns part headers.
         *
         * @return headers
         */
        public Headers headers() {
            return headers;
        }

        /**
         * Returns part payload.
         *
         * @return payload
         */
        public Payload payload() {
            return payload;
        }

    }

    /**
     * Builder for multipart bodies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Boundary.
         */
        private String boundary;

        /**
         * Parts.
         */
        private final ArrayList<Part> parts;

        /**
         * Creates builder.
         */
        private Builder() {
            this.boundary = ID.fastSimpleUUID();
            this.parts = new ArrayList<>();
        }

        /**
         * Adds a part.
         *
         * @param part part
         * @return this builder
         */
        public Builder part(final Part part) {
            if (part == null) {
                throw new ValidateException("Multipart part must not be null");
            }
            parts.add(part);
            return this;
        }

        /**
         * Sets boundary.
         *
         * @param boundary boundary
         * @return this builder
         */
        public Builder boundary(final String boundary) {
            this.boundary = validateBoundary(boundary);
            return this;
        }

        /**
         * Builds body.
         *
         * @return body
         */
        public MultipartBody build() {
            final String current = validateBoundary(boundary == null ? ID.fastSimpleUUID() : boundary);
            final List<Part> snapshot = List.copyOf(parts);
            final MediaType media = MediaType.parse("multipart/form-data; boundary=" + current);
            return new MultipartBody(current, snapshot, media, new MultipartPayload(current, snapshot));
        }

    }

    /**
     * Multipart payload.
     */
    private static final class MultipartPayload implements Payload {

        /**
         * Boundary.
         */
        private final String boundary;

        /**
         * Parts.
         */
        private final List<Part> parts;

        /**
         * Creates payload.
         *
         * @param boundary boundary
         * @param parts    parts
         */
        private MultipartPayload(final String boundary, final List<Part> parts) {
            this.boundary = boundary;
            this.parts = parts;
        }

        @Override
        public long length() {
            long length = closingBytes(boundary).length;
            for (final Part part : parts) {
                final long payloadLength = part.payload().length();
                if (payloadLength < 0) {
                    return -1;
                }
                length += headerBytes(boundary, part).length;
                length += payloadLength;
                length += CRLF.length;
            }
            return length;
        }

        @Override
        public InputStream stream() {
            return new SequenceInputStream(new PartStreams(boundary, parts));
        }

        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public byte[] bytes(final long maxBytes) {
            return Payload.materialize(this, maxBytes, "MultipartBody.MultipartPayload.bytes(long)");
        }

        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            if (charset == null) {
                throw new ValidateException("Charset must not be null");
            }
            return new String(bytes(maxBytes), charset);
        }

        @Override
        public boolean repeatable() {
            for (final Part part : parts) {
                if (!part.payload().repeatable()) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * Lazy multipart stream enumeration.
     */
    private static final class PartStreams implements Enumeration<InputStream> {

        /**
         * Boundary.
         */
        private final String boundary;

        /**
         * Parts.
         */
        private final List<Part> parts;

        /**
         * Current part index.
         */
        private int index;

        /**
         * Phase inside the current part.
         */
        private int phase;

        /**
         * Closing marker flag.
         */
        private boolean closing;

        /**
         * Creates a lazy stream enumeration.
         *
         * @param boundary boundary
         * @param parts    parts
         */
        private PartStreams(final String boundary, final List<Part> parts) {
            this.boundary = boundary;
            this.parts = parts;
        }

        @Override
        public boolean hasMoreElements() {
            return index < parts.size() || !closing;
        }

        @Override
        public InputStream nextElement() {
            if (index < parts.size()) {
                final Part part = parts.get(index);
                if (phase == 0) {
                    phase = 1;
                    return new ByteArrayInputStream(headerBytes(boundary, part));
                }
                if (phase == 1) {
                    phase = 2;
                    return part.payload().stream();
                }
                phase = 0;
                index++;
                return new ByteArrayInputStream(CRLF);
            }
            if (!closing) {
                closing = true;
                return new ByteArrayInputStream(closingBytes(boundary));
            }
            throw new NoSuchElementException("Multipart stream is exhausted");
        }

    }

    /**
     * Builds a part header segment.
     *
     * @param boundary boundary
     * @param part     part
     * @return header bytes
     */
    private static byte[] headerBytes(final String boundary, final Part part) {
        final StringBuilder builder = new StringBuilder("--").append(boundary).append(Symbol.CRLF);
        for (final Map.Entry<String, List<String>> entry : part.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.append(entry.getKey()).append(": ").append(value).append(Symbol.CRLF);
            }
        }
        builder.append(Symbol.CRLF);
        return builder.toString().getBytes(org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Builds part headers.
     *
     * @param name     part name
     * @param filename file name
     * @param media    media type
     * @param length   payload length
     * @return headers
     */
    private static Headers partHeaders(
            final String name,
            final String filename,
            final MediaType media,
            final long length) {
        if (length < -1) {
            throw new ProtocolException("Part content length must be -1 or greater");
        }
        final Headers.Builder builder = Headers.builder().add("Content-Disposition", disposition(name, filename));
        if (media != null) {
            builder.add("Content-Type", media.value());
        }
        if (length >= 0) {
            builder.add("Content-Length", Long.toString(length));
        }
        return builder.build();
    }

    /**
     * Builds a Content-Disposition header value.
     *
     * @param name     part name
     * @param filename file name
     * @return header value
     */
    private static String disposition(final String name, final String filename) {
        final StringBuilder builder = new StringBuilder("form-data; name=").append(Symbol.DOUBLE_QUOTES)
                .append(quote(name)).append(Symbol.DOUBLE_QUOTES);
        if (filename != null) {
            builder.append(Symbol.SEMICOLON).append(Symbol.SPACE).append("filename=").append(Symbol.DOUBLE_QUOTES)
                    .append(quote(filename)).append(Symbol.DOUBLE_QUOTES);
        }
        return builder.toString();
    }

    /**
     * Escapes a quoted header parameter.
     *
     * @param value parameter value
     * @return escaped value
     */
    private static String quote(final String value) {
        return value.replace(Symbol.BACKSLASH, Symbol.BACKSLASH + Symbol.BACKSLASH)
                .replace(Symbol.DOUBLE_QUOTES, Symbol.BACKSLASH + Symbol.DOUBLE_QUOTES);
    }

    /**
     * Validates a part name value.
     *
     * @param value value
     * @param name  value name
     * @return validated value
     */
    private static String validatePartName(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Validates a part payload.
     *
     * @param payload payload
     */
    private static void validatePartPayload(final Payload payload) {
        if (payload == null) {
            throw new ValidateException("Part payload must not be null");
        }
    }

    /**
     * Builds the closing boundary segment.
     *
     * @param boundary boundary
     * @return closing bytes
     */
    private static byte[] closingBytes(final String boundary) {
        return ("--" + boundary + "--\r\n").getBytes(org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Validates boundary.
     *
     * @param boundary boundary
     * @return boundary
     */
    private static String validateBoundary(final String boundary) {
        if (StringKit.isBlank(boundary) || StringKit.containsAny(boundary, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Multipart boundary must be non-blank and single-line");
        }
        if (boundary.length() > 70) {
            throw new ProtocolException("Multipart boundary is too long");
        }
        return boundary;
    }

}
