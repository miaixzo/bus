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

/**
 * Handles the processing of decoded messages.
 * <p>
 * Implementations of this interface define the business logic to be applied to messages after they have been
 * successfully decoded by the {@link Message} codec.
 * </p>
 *
 * @param <T> the type of the message object entity
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler<T> {

    /**
     * Processes the received message.
     *
     * @param session the communication session associated with the message
     * @param data    the business message to be processed
     */
    void process(Session session, T data);

    /**
     * Handles state machine events triggered by the framework.
     * <p>
     * This method is invoked by the framework when a specific {@link Status} event occurs.
     * </p>
     *
     * @param session   the {@link Session} object that triggered the state event
     * @param status    the {@link Status} enumeration indicating the type of event
     * @param throwable an optional {@link Throwable} object if an exception is associated with the event, otherwise
     *                  {@code null}
     */
    default void stateEvent(Session session, Status status, Throwable throwable) {
        if (status == Status.DECODE_EXCEPTION || status == Status.PROCESS_EXCEPTION) {
            throwable.printStackTrace();
        }
    }

}
