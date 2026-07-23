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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

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
     * Creates priority metadata.
     *
     * @param dependencyStreamId dependency stream identifier
     * @param weight             priority weight from 1 through 256
     * @param exclusive          whether the dependency is exclusive
     */
    public Http2Priority {
        if (dependencyStreamId < Normal._0 || dependencyStreamId > Integer.MAX_VALUE || weight < Normal._1
                || weight > Normal._256) {
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
     * Decodes priority metadata from immutable payload bytes.
     *
     * @param payload  priority payload
     * @param streamId owning stream id
     * @return priority
     */
    static Http2Priority decode(final ByteString payload, final int streamId) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ProtocolException("Invalid HTTP/2 PRIORITY payload"));
        if (checkedPayload.size() < Normal._5) {
            throw new ProtocolException("Invalid HTTP/2 PRIORITY payload");
        }
        final Buffer view = new Buffer().write(checkedPayload);
        return decodeView(view, streamId);
    }

    /**
     * Decodes priority metadata from a readable view.
     *
     * @param view     priority view
     * @param streamId owning stream id
     * @return priority
     */
    private static Http2Priority decodeView(final Buffer view, final int streamId) {
        final int dependency = view.readInt();
        final boolean exclusive = (dependency & Builder.HTTP2_PRIORITY_EXCLUSIVE_MASK) != Normal._0;
        final int dependencyStreamId = dependency & Integer.MAX_VALUE;
        if (streamId > Normal._0 && dependencyStreamId == streamId) {
            throw new ProtocolException("HTTP/2 stream cannot depend on itself");
        }
        return new Http2Priority(dependencyStreamId, (view.readByte() & Builder.UNSIGNED_BYTE_MASK) + Normal._1,
                exclusive);
    }

    /**
     * Decodes priority metadata from a core buffer.
     *
     * @param payload  priority payload
     * @param streamId owning stream id
     * @return priority
     */
    static Http2Priority decode(final Buffer payload, final int streamId) {
        final Buffer checkedPayload = Assert
                .notNull(payload, () -> new ProtocolException("Invalid HTTP/2 PRIORITY payload"));
        if (checkedPayload.size() < Normal._5) {
            throw new ProtocolException("Invalid HTTP/2 PRIORITY payload");
        }
        final Buffer view = new Buffer();
        checkedPayload.copyTo(view, Normal._0, Normal._5);
        return decodeView(view, streamId);
    }

    /**
     * Encodes priority metadata as immutable payload bytes.
     *
     * @return payload
     */
    public ByteString encodeBytes() {
        final Buffer payload = new Buffer();
        payload.writeInt(exclusive ? dependencyStreamId | Builder.HTTP2_PRIORITY_EXCLUSIVE_MASK : dependencyStreamId);
        payload.writeByte(weight - Normal._1);
        return payload.readByteString();
    }

}
