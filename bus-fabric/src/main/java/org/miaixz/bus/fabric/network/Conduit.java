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
package org.miaixz.bus.fabric.network;

import java.util.concurrent.CompletableFuture;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;

/**
 * Asynchronous network conduit contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Conduit extends AutoCloseable {

    /**
     * Reads bytes into a target buffer.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count to read
     * @return future containing the read byte count, or {@code Normal.__1} at EOF
     */
    CompletableFuture<Long> read(Buffer target, long byteCount);

    /**
     * Writes bytes from a source buffer and consumes the written bytes.
     *
     * @param source    source buffer
     * @param byteCount byte count to write
     * @return future containing the written byte count
     */
    CompletableFuture<Long> write(Buffer source, long byteCount);

    /**
     * Returns the core.io read view.
     *
     * @return source view
     */
    Source source();

    /**
     * Returns the core.io write view.
     *
     * @return sink view
     */
    Sink sink();

    /**
     * Returns whether this conduit is open.
     *
     * @return true when opened
     */
    boolean opened();

    /**
     * Closes the conduit.
     */
    @Override
    void close();

}
