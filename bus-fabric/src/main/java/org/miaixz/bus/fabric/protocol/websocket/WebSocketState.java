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
package org.miaixz.bus.fabric.protocol.websocket;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Network-I/O-free WebSocket protocol state shared by reader and writer coordination.
 *
 * @author Kimi Liu
 */
final class WebSocketState {

    /**
     * Endpoint role defining masking direction.
     */
    private final WebSocketRole role;

    /**
     * Whether the single close frame has been admitted to the writer queue.
     */
    private final AtomicBoolean closeQueued = new AtomicBoolean();

    /**
     * Whether the admitted close frame was physically flushed.
     */
    private final AtomicBoolean closeWritten = new AtomicBoolean();

    /**
     * Whether the peer close frame was received.
     */
    private final AtomicBoolean peerCloseReceived = new AtomicBoolean();

    /**
     * Failure delivered after a best-effort close frame.
     */
    private final AtomicReference<Throwable> failureAfterClose = new AtomicReference<>();

    /**
     * Creates protocol state.
     *
     * @param role endpoint role
     */
    WebSocketState(final WebSocketRole role) {
        this.role = role;
    }

    /**
     * Returns whether inbound frames must be masked.
     *
     * @return inbound mask requirement
     */
    boolean readerExpectMasked() {
        return role.readerExpectMasked();
    }

    /**
     * Returns whether outbound frames must be masked.
     *
     * @return outbound mask requirement
     */
    boolean writerMask() {
        return role.writerMask();
    }

    /**
     * Admits the unique close frame.
     *
     * @return true only for the first close enqueue
     */
    boolean queueClose() {
        return closeQueued.compareAndSet(false, true);
    }

    /**
     * Rolls back close admission when enqueueing fails before ownership transfers to the writer.
     */
    void closeEnqueueFailed() {
        closeQueued.set(false);
    }

    /**
     * Marks the local close frame as flushed.
     */
    void closeWritten() {
        closeWritten.set(true);
    }

    /**
     * Returns whether the local close frame was flushed.
     *
     * @return true when flushed
     */
    boolean closeWasWritten() {
        return closeWritten.get();
    }

    /**
     * Marks a received peer close frame.
     */
    void peerCloseReceived() {
        peerCloseReceived.set(true);
    }

    /**
     * Returns whether a peer close frame was received.
     *
     * @return true when received
     */
    boolean peerCloseWasReceived() {
        return peerCloseReceived.get();
    }

    /**
     * Records the first failure that should be delivered after close.
     *
     * @param cause failure cause
     */
    void failureAfterClose(final Throwable cause) {
        failureAfterClose.compareAndSet(null, cause);
    }

    /**
     * Returns the first deferred close failure.
     *
     * @return failure or {@code null}
     */
    Throwable failureAfterClose() {
        return failureAfterClose.get();
    }

}
