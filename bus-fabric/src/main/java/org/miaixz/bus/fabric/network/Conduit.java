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

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

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
     * @param target target buffer
     * @return read future
     */
    CompletableFuture<Integer> read(ByteBuffer target);

    /**
     * Reads bytes with a completion handler.
     *
     * @param target  target buffer
     * @param handler completion handler
     */
    void read(ByteBuffer target, CompletionHandler<Integer, ByteBuffer> handler);

    /**
     * Writes bytes from a source buffer.
     *
     * @param source source buffer
     * @return write future
     */
    CompletableFuture<Integer> write(ByteBuffer source);

    /**
     * Writes bytes with a completion handler.
     *
     * @param source  source buffer
     * @param handler completion handler
     */
    void write(ByteBuffer source, CompletionHandler<Integer, ByteBuffer> handler);

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
