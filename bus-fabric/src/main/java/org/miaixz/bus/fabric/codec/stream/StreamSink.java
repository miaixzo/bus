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
package org.miaixz.bus.fabric.codec.stream;

import java.nio.ByteBuffer;

import org.miaixz.bus.fabric.Payload;

/**
 * Stream sink contract for incremental writes and payload transfer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface StreamSink extends AutoCloseable {

    /**
     * Writes source bytes.
     *
     * @param source source buffer
     */
    void write(ByteBuffer source);

    /**
     * Writes a payload.
     *
     * @param payload payload
     */
    void write(Payload payload);

    /**
     * Returns the written byte count.
     *
     * @return written byte count
     */
    long written();

    /**
     * Flushes pending data.
     */
    void flush();

    /**
     * Closes the sink.
     */
    @Override
    void close();

}
