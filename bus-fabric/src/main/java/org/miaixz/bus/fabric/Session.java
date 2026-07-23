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
package org.miaixz.bus.fabric;

import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Long-lived fabric session contract for opened protocol connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Session extends Lifecycle {

    /**
     * Returns the session address.
     *
     * @return protocol address associated with the opened connection
     */
    Address address();

    /**
     * Closes the session normally.
     *
     * @return true when this invocation changed the session to closed
     */
    boolean close();

    /**
     * Cancels the session forcefully.
     *
     * @return true when this invocation changed the session to cancelled
     */
    boolean cancel();

    /**
     * Returns session attributes.
     *
     * @return immutable snapshot of protocol-specific session attributes
     */
    Map<String, Object> attributes();

    /**
     * Sends a payload through this session when the protocol supports outbound messages.
     *
     * @param payload non-null outbound payload transferred to a protocol-specific implementation
     * @return single-use send call, or a failing unsupported-operation call from the default implementation
     */
    default Call<Void> send(final Payload payload) {
        if (payload == null) {
            throw new ValidateException("Payload must not be null");
        }
        return Call.unsupported("Session does not support send");
    }

}
