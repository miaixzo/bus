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
package org.miaixz.bus.socket;

import java.nio.ByteBuffer;

/**
 * Enumerates the various status events of interest in the session lifecycle.
 * <p>
 * Each of these state machine events will trigger the {@link Handler#stateEvent(Session, Status, Throwable)} method.
 * Users can handle the events they are interested in by implementing the {@link Handler} interface.
 * </p>
 *
 * @author Kimi Liu
 * @see Handler
 * @since Java 21+
 */
public enum Status {
    /**
     * A new connection has been established and a session object has been built.
     */
    NEW_SESSION,
    /**
     * The read channel has been closed. This is usually triggered by:
     * <ol>
     * <li>The remote peer actively closing its write channel, causing an EOF condition.</li>
     * <li>The current session detects it is in the {@link Status#SESSION_CLOSING} state after completing a read
     * operation.</li>
     * </ol>
     */
    INPUT_SHUTDOWN,
    /**
     * An uncaught exception occurred during business logic processing in {@link Handler#process(Session, Object)}.
     */
    PROCESS_EXCEPTION,
    /**
     * An uncaught exception occurred during protocol decoding in {@link Message#decode(ByteBuffer, Session)}.
     */
    DECODE_EXCEPTION,
    /**
     * An exception occurred during a low-level read operation, triggering
     * {@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}.
     */
    INPUT_EXCEPTION,
    /**
     * An exception occurred during a low-level write operation, triggering
     * {@link java.nio.channels.CompletionHandler#failed(Throwable, Object)}.
     */
    OUTPUT_EXCEPTION,
    /**
     * The session is in the process of closing, typically because {@link Session#close(boolean)} was called while there
     * was still data pending to be written.
     */
    SESSION_CLOSING,
    /**
     * The session has been successfully closed.
     */
    SESSION_CLOSED,
    /**
     * A new connection was rejected by the server (Server-side only).
     */
    REJECT_ACCEPT,
    /**
     * An exception occurred on the server while accepting a new connection.
     */
    ACCEPT_EXCEPTION,
    /**
     * An internal exception occurred within the framework.
     */
    INTERNAL_EXCEPTION
}
