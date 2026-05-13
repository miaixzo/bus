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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Multipart payload abstraction.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Payload {

    /**
     * Executes the size operation.
     *
     * @return the operation result.
     */
    long size();

    /**
     * Executes the new input stream operation.
     *
     * @return the operation result.
     */
    InputStream newInputStream();

    /**
     * Executes the of bytes operation.
     *
     * @param data the data.
     * @return the operation result.
     */
    static Payload ofBytes(byte[] data) {
        Objects.requireNonNull(data, "Data cannot be null");
        return new ByteArrayPayload(data.clone());
    }

    /**
     * Executes the of path operation.
     *
     * @param path the path.
     * @return the operation result.
     */
    static Payload ofPath(Path path) {
        Objects.requireNonNull(path, "Path cannot be null");
        return new FilePayload(path);
    }

    /**
     * Executes the empty operation.
     *
     * @return the operation result.
     */
    static Payload empty() {
        return EmptyPayload.INSTANCE;
    }

    /**
     * Represents the ByteArrayPayload record.
     *
     * @param data the data.
     * @author Kimi Liu
     * @since Java 21+
     */
    record ByteArrayPayload(byte[] data) implements Payload {

        /**
         * Creates a new instance.
         *
         * @param data the data.
         */
        public ByteArrayPayload {
            Objects.requireNonNull(data, "Data cannot be null");
        }

        /**
         * Executes the size operation.
         *
         * @return the operation result.
         */
        @Override
        public long size() {
            return data.length;
        }

        /**
         * Executes the new input stream operation.
         *
         * @return the operation result.
         */
        @Override
        public InputStream newInputStream() {
            return new ByteArrayInputStream(data);
        }

    }

    /**
     * Represents the FilePayload record.
     *
     * @param path the path.
     * @author Kimi Liu
     * @since Java 21+
     */
    record FilePayload(Path path) implements Payload {

        /**
         * Creates a new instance.
         *
         * @param path the path.
         */
        public FilePayload {
            Objects.requireNonNull(path, "Path cannot be null");
        }

        /**
         * Executes the size operation.
         *
         * @return the operation result.
         */
        @Override
        public long size() {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Executes the new input stream operation.
         *
         * @return the operation result.
         */
        @Override
        public InputStream newInputStream() {
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

    /**
     * Defines the EmptyPayload values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum EmptyPayload implements Payload {

        /**
         * The instance value.
         */
        INSTANCE;

        /**
         * Executes the size operation.
         *
         * @return the operation result.
         */
        @Override
        public long size() {
            return 0;
        }

        /**
         * Executes the new input stream operation.
         *
         * @return the operation result.
         */
        @Override
        public InputStream newInputStream() {
            return InputStream.nullInputStream();
        }

    }

}
