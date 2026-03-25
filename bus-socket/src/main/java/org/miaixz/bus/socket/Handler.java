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

/**
 * Handles the processing of decoded messages.
 * <p>
 * Implementations of this interface define the business logic to be applied to messages after they have been
 * successfully decoded by the {@link Message} codec.
 * </p>
 *
 * @param <T> the type of the message object entity
 * @author Kimi Liu
 * @since Java 21+
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
