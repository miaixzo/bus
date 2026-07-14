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
package org.miaixz.bus.fabric.cache;

import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;

/**
 * Streaming cache writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface CacheWriter extends AutoCloseable {

    /**
     * Returns the writable sink.
     *
     * @return sink
     */
    Sink body();

    /**
     * Writes a bus-core buffer to the cache body.
     *
     * @param source    source buffer
     * @param byteCount byte count
     * @throws IOException when writing fails
     */
    void write(Buffer source, long byteCount) throws IOException;

    /**
     * Commits the written body.
     */
    void commit();

    /**
     * Aborts the written body.
     */
    void abort();

    /**
     * Aborts unfinished writes by default.
     */
    @Override
    default void close() {
        abort();
    }

}
