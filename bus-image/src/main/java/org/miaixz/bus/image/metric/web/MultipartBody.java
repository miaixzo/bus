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
package org.miaixz.bus.image.metric.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.MediaType;

/**
 * Builder for multipart/related HTTP request bodies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MultipartBody {

    /**
     * The boundary value.
     */
    private final String boundary;

    /**
     * The content type value.
     */
    private final String contentType;

    /**
     * The parts value.
     */
    private final List<MultipartPart> parts = new ArrayList<>();

    /**
     * The closed value.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Creates a new instance.
     *
     * @param contentType the content type.
     * @param boundary    the boundary.
     */
    public MultipartBody(String contentType, String boundary) {
        this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        this.boundary = Objects.requireNonNull(boundary, "Boundary cannot be null");
    }

    /**
     * Adds the part.
     *
     * @param contentType the content type.
     * @param data        the data.
     * @param location    the location.
     * @return the operation result.
     */
    public MultipartBody addPart(String contentType, byte[] data, String location) {
        return addPart(contentType, Payload.ofBytes(data), location);
    }

    /**
     * Adds the part.
     *
     * @param contentType the content type.
     * @param path        the path.
     * @param location    the location.
     * @return the operation result.
     */
    public MultipartBody addPart(String contentType, Path path, String location) {
        return addPart(contentType, Payload.ofPath(path), location);
    }

    /**
     * Adds the part.
     *
     * @param contentType the content type.
     * @param payload     the payload.
     * @param location    the location.
     * @return the operation result.
     */
    public MultipartBody addPart(String contentType, Payload payload, String location) {
        Objects.requireNonNull(contentType, "Content type cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
        if (closed.get()) {
            throw new IllegalStateException("MultipartBody is closed");
        }
        parts.add(new MultipartPart(contentType, location, payload));
        return this;
    }

    /**
     * Creates the body publisher.
     *
     * @param streamSupplier the stream supplier.
     * @return the operation result.
     */
    public HttpRequest.BodyPublisher createBodyPublisher(Supplier<? extends InputStream> streamSupplier) {
        return HttpRequest.BodyPublishers.ofInputStream(Objects.requireNonNull(streamSupplier));
    }

    /**
     * Creates the body publisher.
     *
     * @return the operation result.
     */
    public HttpRequest.BodyPublisher createBodyPublisher() {
        return HttpRequest.BodyPublishers.ofInputStream(this::newInputStream);
    }

    /**
     * Executes the new input stream operation.
     *
     * @return the operation result.
     */
    public InputStream newInputStream() {
        return new SequenceInputStream(createStreamEnumeration());
    }

    /**
     * Gets the content type header.
     *
     * @return the content type header.
     */
    public String getContentTypeHeader() {
        return MediaType.MULTIPART_RELATED + "; type=\"%s\";boundary=%s".formatted(contentType, boundary);
    }

    /**
     * Gets the boundary.
     *
     * @return the boundary.
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Gets the parts.
     *
     * @return the parts.
     */
    public List<MultipartPart> getParts() {
        return Collections.unmodifiableList(parts);
    }

    /**
     * Executes the reset operation.
     */
    public void reset() {
        parts.clear();
        closed.set(false);
    }

    /**
     * Creates the stream enumeration.
     *
     * @return the operation result.
     */
    private Enumeration<InputStream> createStreamEnumeration() {
        return new MultipartStreamEnumeration();
    }

    /**
     * Creates the part header stream.
     *
     * @param part the part.
     * @return the operation result.
     */
    private InputStream createPartHeaderStream(MultipartPart part) {
        return new ByteArrayInputStream(part.generateHeader(boundary).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates the closing stream.
     *
     * @return the operation result.
     */
    private InputStream createClosingStream() {
        closed.set(true);
        return new ByteArrayInputStream(("\r\n--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Represents the MultipartStreamEnumeration type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private final class MultipartStreamEnumeration implements Enumeration<InputStream> {

        /**
         * The part iterator value.
         */
        private final Iterator<MultipartPart> partIterator = parts.iterator();

        /**
         * The current part value.
         */
        private MultipartPart currentPart;

        /**
         * The stream closed value.
         */
        private boolean streamClosed;

        /**
         * The needs part data value.
         */
        private boolean needsPartData;

        /**
         * Determines whether more elements.
         *
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public boolean hasMoreElements() {
            return !streamClosed;
        }

        /**
         * Executes the next element operation.
         *
         * @return the operation result.
         */
        @Override
        public InputStream nextElement() {
            if (streamClosed) {
                throw new NoSuchElementException("No more streams available");
            }
            if (needsPartData) {
                needsPartData = false;
                return currentPart.newInputStream();
            }
            if (partIterator.hasNext()) {
                currentPart = partIterator.next();
                needsPartData = true;
                return createPartHeaderStream(currentPart);
            }
            streamClosed = true;
            return createClosingStream();
        }

    }

}
