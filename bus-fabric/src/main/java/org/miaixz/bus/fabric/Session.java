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

import java.nio.ByteBuffer;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Long-lived fabric session contract for opened protocol connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Session {

    /**
     * Returns the session address.
     *
     * @return session address
     */
    Address address();

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    Status state();

    /**
     * Returns whether the session is opened.
     *
     * @return true when opened
     */
    boolean opened();

    /**
     * Sends a payload through the session.
     *
     * @param payload payload
     * @return send call
     */
    Call<Void> send(Payload payload);

    /**
     * Sends a binary payload through the JDK byte buffer compatibility boundary.
     *
     * @param bytes binary payload
     * @return send call
     * @deprecated use {@link #send(Payload)} with {@link Payload#of(org.miaixz.bus.core.io.ByteString)}
     */
    @Deprecated(since = "8.8.3")
    default Call<Void> send(final ByteBuffer bytes) {
        if (bytes == null) {
            throw new ValidateException("Session binary payload must not be null");
        }
        final ByteBuffer duplicate = bytes.duplicate();
        final byte[] snapshot = new byte[duplicate.remaining()];
        duplicate.get(snapshot);
        return send(Payload.of(snapshot));
    }

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
     * @return immutable attributes snapshot
     */
    Map<String, Object> attributes();

}
