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

import java.nio.file.Path;

import org.miaixz.bus.core.lang.exception.IllegalPathException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.BodyCodec;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * HTTP file body that opens file streams lazily.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FileBody implements RequestBody {

    /**
     * File path.
     */
    private final Path path;

    /**
     * Media type.
     */
    private final MediaType media;

    /**
     * File length snapshot.
     */
    private final long length;

    /**
     * File payload.
     */
    private final Payload payload;

    /**
     * Creates a file body.
     *
     * @param path    file path
     * @param media   media type
     * @param length  file length
     * @param payload file payload
     */
    private FileBody(final Path path, final MediaType media, final long length, final Payload payload) {
        this.path = path;
        this.media = media;
        this.length = length;
        this.payload = payload;
    }

    /**
     * Creates a file body.
     *
     * @param path  file path
     * @param media media type
     * @return file body
     */
    public static FileBody of(final Path path, final MediaType media) {
        if (path == null) {
            throw new ValidateException("File path must not be null");
        }
        if (media == null) {
            throw new ValidateException("File media must not be null");
        }
        try {
            final Payload payload = BodyCodec.create().file(path, media);
            return new FileBody(path, media, payload.length(), payload);
        } catch (final IllegalPathException e) {
            throw new ValidateException("File path must point to a regular file", e);
        }
    }

    /**
     * Returns the file path.
     *
     * @return path
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the media type.
     *
     * @return media type
     */
    public MediaType media() {
        return media;
    }

    /**
     * Returns the saved file length.
     *
     * @return length
     */
    public long length() {
        return length;
    }

    /**
     * Returns the lazy file payload.
     *
     * @return payload
     */
    public Payload payload() {
        return payload;
    }

}
