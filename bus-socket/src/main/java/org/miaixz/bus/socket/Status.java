/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
