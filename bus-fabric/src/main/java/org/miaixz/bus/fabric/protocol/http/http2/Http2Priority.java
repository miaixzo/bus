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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.nio.ByteBuffer;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * HTTP/2 stream priority metadata.
 *
 * @param dependencyStreamId stream dependency id, or 0 for the root dependency
 * @param weight             priority weight in the HTTP/2 public range 1..256
 * @param exclusive          true when the dependency is exclusive
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2Priority(int dependencyStreamId, int weight, boolean exclusive) {

    /**
     * Encoded priority payload size.
     */
    static final int LENGTH = Integer.BYTES + Byte.BYTES;

    /**
     * Maximum HTTP/2 stream id.
     */
    private static final int MAX_STREAM_ID = 0x7fffffff;

    /**
     * Creates priority metadata.
     */
    public Http2Priority {
        if (dependencyStreamId < 0 || dependencyStreamId > MAX_STREAM_ID || weight < 1 || weight > 256) {
            throw new ValidateException("Invalid HTTP/2 priority metadata");
        }
    }

    /**
     * Creates priority metadata.
     *
     * @param dependencyStreamId dependency stream id
     * @param weight             priority weight
     * @param exclusive          exclusive flag
     * @return priority
     */
    public static Http2Priority of(final int dependencyStreamId, final int weight, final boolean exclusive) {
        return new Http2Priority(dependencyStreamId, weight, exclusive);
    }

    /**
     * Decodes priority metadata from a frame payload.
     *
     * @param payload  priority payload
     * @param streamId owning stream id
     * @return priority
     */
    static Http2Priority decode(final ByteBuffer payload, final int streamId) {
        if (payload == null || payload.remaining() < LENGTH) {
            throw new ProtocolException("Invalid HTTP/2 PRIORITY payload");
        }
        final ByteBuffer view = payload.asReadOnlyBuffer();
        final int dependency = view.getInt();
        final boolean exclusive = (dependency & 0x80000000) != 0;
        final int dependencyStreamId = dependency & MAX_STREAM_ID;
        if (streamId > 0 && dependencyStreamId == streamId) {
            throw new ProtocolException("HTTP/2 stream cannot depend on itself");
        }
        return new Http2Priority(dependencyStreamId, (view.get() & 0xff) + 1, exclusive);
    }

    /**
     * Encodes priority metadata.
     *
     * @return payload
     */
    public ByteBuffer encode() {
        final ByteBuffer payload = ByteBuffer.allocate(LENGTH);
        payload.putInt(exclusive ? dependencyStreamId | 0x80000000 : dependencyStreamId);
        payload.put((byte) (weight - 1));
        payload.flip();
        return payload.asReadOnlyBuffer();
    }

}
